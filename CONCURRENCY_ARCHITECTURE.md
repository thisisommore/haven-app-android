# Haven Android - Concurrency Architecture

This document describes the threading and concurrency architecture of the Haven Android app, following Kotlin coroutines best practices.

## Overview

The app uses **Kotlin Coroutines** with **Structured Concurrency** for all asynchronous operations. The architecture is designed to:

1. **Avoid blocking threads** - Never use `runBlocking` in production code
2. **Support testability** - Inject dispatchers for testing with `TestDispatcher`
3. **Ensure cancellation** - All coroutines are properly scoped and cancellable
4. **Handle errors gracefully** - Failures are isolated and don't crash the app
5. **Maintain thread safety** - Proper synchronization for shared mutable state

## Key Components

### 1. Dispatcher Injection (`di/CoroutineDispatchers.kt`)

**Best Practice: Inject dispatchers instead of hardcoding them.**

```kotlin
@Singleton
class CoroutineDispatchers @Inject constructor() {
    val main: CoroutineDispatcher = Dispatchers.Main
    val io: CoroutineDispatcher = Dispatchers.IO
    val default: CoroutineDispatcher = Dispatchers.Default
    val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
```

This allows:
- **Testing**: Substitute `TestDispatcher` for controlled execution
- **Flexibility**: Change dispatchers without modifying business logic
- **Consistency**: All components use the same dispatchers

### 2. Callback Coroutine Scope (`xxdk/callbacks/CallbackCoroutineScope.kt`)

**Purpose**: Handle callbacks from the XXDK native layer (Java/JNI).

**Architecture**:
```
┌─────────────────┐     ┌──────────────────────┐     ┌─────────────────┐
│   XXDK Native   │────▶│  Native Callback     │────▶│  Coroutine      │
│   (Go/JNI)      │     │  Thread              │     │  (IO Dispatcher)│
└─────────────────┘     └──────────────────────┘     └─────────────────┘
                                                              │
                                                              ▼
                                                       ┌─────────────────┐
                                                       │  Database       │
                                                       │  Operation      │
                                                       └─────────────────┘
```

**Key Features**:
- **SupervisorJob**: Child failures don't cancel siblings
- **IO Dispatcher**: For database and network operations
- **Exception Handler**: Centralized error handling
- **Lifecycle Management**: Proper cleanup with `shutdown()`

**Usage**:
```kotlin
class DmReceiver : DMReceiver {
    private val callbackScope = CallbackScopeProvider.getInstance()

    override fun receive(...): Long {
        // Launch async processing without blocking
        callbackScope.launchCallback {
            persistIncomingMessage(...)
        }
        return messageId // Return immediately
    }
}
```

**Why not `runBlocking`?**

❌ **BAD** (blocks the native callback thread):
```kotlin
override fun receive(...): Long {
    return runBlocking {
        database.insertMessage(...) // Blocks native thread!
    }
}
```

✅ **GOOD** (non-blocking):
```kotlin
override fun receive(...): Long {
    callbackScope.launchCallback {
        database.insertMessage(...) // Runs on IO dispatcher
    }
    return messageId // Returns immediately
}
```

### 3. ViewModel Scopes

**Pattern**: Use `viewModelScope` for UI-related coroutines.

```kotlin
class HomePageController(private val repository: DatabaseRepository) : ViewModel() {

    fun clearUnreadCount(chatId: String) {
        viewModelScope.launch {
            repository.clearUnreadCount(chatId)
        }
    }
}
```

**Benefits**:
- Automatically cancels when ViewModel is cleared
- Uses Main dispatcher for UI updates
- Survives configuration changes

### 4. Repository Pattern

**Pattern**: Expose `suspend` functions for one-shot operations, `Flow` for streams.

```kotlin
class DatabaseRepository(context: Context) {
    // One-shot operation
    suspend fun getChatById(id: String): ChatModel? = chatDao.getById(id)

    // Stream of data
    fun getAllChats(): Flow<List<ChatModel>> = chatDao.getAll()
}
```

**Best Practices**:
- Mark functions `suspend` when they perform I/O
- Return `Flow` for observable data streams
- Don't expose blocking operations

## Threading Rules

### DO ✅

1. **Inject dispatchers** for testability
   ```kotlin
   class MyClass(private val dispatchers: CoroutineDispatchers)
   ```

2. **Use `withContext` to switch dispatchers**
   ```kotlin
   suspend fun loadData() = withContext(dispatchers.io) {
       // blocking I/O operation
   }
   ```

3. **Use `viewModelScope` in ViewModels**
   ```kotlin
   viewModelScope.launch { /* UI-related work */ }
   ```

4. **Use `CallbackCoroutineScope` for native callbacks**
   ```kotlin
   callbackScope.launchCallback { /* callback handling */ }
   ```

5. **Make suspend functions main-safe**
   ```kotlin
   // Callers don't need to worry about dispatchers
   suspend fun fetchData(): Data
   ```

6. **Use `supervisorScope` for independent child jobs**
   ```kotlin
   supervisorScope {
       val job1 = launch { /* failure doesn't affect job2 */ }
       val job2 = launch { /* failure doesn't affect job1 */ }
   }
   ```

### DON'T ❌

1. **Never use `runBlocking` in production**
   ```kotlin
   // ❌ Blocks the calling thread
   runBlocking { database.operation() }
   ```

2. **Never hardcode dispatchers**
   ```kotlin
   // ❌ Hard to test
   withContext(Dispatchers.IO) { }
   ```

3. **Never use `GlobalScope`**
   ```kotlin
   // ❌ Uncontrolled lifetime
   GlobalScope.launch { }
   ```

4. **Never expose blocking operations**
   ```kotlin
   // ❌ Forces callers to deal with threading
   fun getData(): Data = database.blockingGet()
   ```

## Lifecycle Management

### Initialization

When user logs in:
```kotlin
// In XXDK+Clients.kt
internal suspend fun XXDK.performSetupClients(...) {
    // 1. Initialize callback scope
    CallbackScopeProvider.getInstance()

    // 2. Create native clients
    // 3. Initialize messaging
}
```

### Cleanup

When user logs out:
```kotlin
// In XXDK+Logout.kt
internal suspend fun XXDK.performLogout() {
    // 1. Stop native operations
    // 2. Clear caches
    // 3. Shutdown callback scope
    CallbackScopeProvider.shutdown()
    // 4. Clear database
}
```

## Testing

### With TestDispatcher

```kotlin
class MyTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun testAsyncOperation() = runTest(testDispatcher) {
        val dispatchers = TestCoroutineDispatchers(testDispatcher)
        val repository = MyRepository(dispatchers)

        // Control coroutine execution
        repository.doAsyncWork()
        advanceUntilIdle()

        // Assert results
    }
}
```

### Test Dispatcher Provider

```kotlin
class TestCoroutineDispatchers(
    private val testDispatcher: TestDispatcher
) : CoroutineDispatchers() {
    override val main: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
    override val unconfined: CoroutineDispatcher = testDispatcher
}
```

## Error Handling

### In Callbacks

```kotlin
callbackScope.launchCallback {
    try {
        database.operation()
    } catch (e: Exception) {
        // Log error but don't crash
        Log.e(TAG, "Operation failed", e)
    }
}
```

### Exception Handler

```kotlin
private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
    Log.e(TAG, "Unhandled exception in callback", throwable)
}
```

### Cancellation

```kotlin
viewModelScope.launch {
    try {
        longRunningOperation()
    } catch (e: CancellationException) {
        // Clean up and rethrow
        cleanup()
        throw e
    }
}
```

## Performance Considerations

1. **Use IO dispatcher for database operations**
   - Room uses its own thread pool
   - Prevents blocking main thread

2. **Use Default dispatcher for CPU work**
   - Computations, parsing, encoding
   - Limited by number of CPU cores

3. **Avoid context switching**
   - Batch operations when possible
   - Use `async`/`await` for parallel work

4. **Cache coroutine scopes**
   - Don't create new scopes for every operation
   - Reuse application-wide scopes

## Migration Guide

### From `runBlocking` to Coroutines

**Before**:
```kotlin
fun callbackMethod(): Result {
    return runBlocking {
        database.insert(data)
    }
}
```

**After**:
```kotlin
fun callbackMethod(): Long {
    val id = generateId()
    callbackScope.launchCallback {
        database.insert(data.copy(id = id))
    }
    return id
}
```

### From Hardcoded Dispatchers

**Before**:
```kotlin
suspend fun loadData() = withContext(Dispatchers.IO) { }
```

**After**:
```kotlin
class MyClass(private val dispatchers: CoroutineDispatchers) {
    suspend fun loadData() = withContext(dispatchers.io) { }
}
```

## References

- [Kotlin Coroutines Best Practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
- [Coroutines on Android](https://developer.android.com/topic/libraries/architecture/coroutines)
- [Coroutine Context and Dispatchers](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html)
- [Structured Concurrency](https://kotlinlang.org/docs/composing-suspending-functions.html#structured-concurrency-with-async)
