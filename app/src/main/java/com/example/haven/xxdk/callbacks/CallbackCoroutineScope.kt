package com.example.haven.xxdk.callbacks

import android.util.Log
import com.example.haven.di.CoroutineDispatchers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Dedicated CoroutineScope for handling XXDK callbacks.
 *
 * Best Practices Applied:
 * 1. **SupervisorJob**: Child failures don't cancel siblings (one failed message
 *    doesn't stop processing other messages)
 * 2. **Dedicated Dispatcher**: Uses IO dispatcher for database operations
 * 3. **Exception Handling**: Centralized error handling via CoroutineExceptionHandler
 * 4. **Lifecycle Management**: Proper cleanup with cancel()
 * 5. **No runBlocking**: All operations are truly asynchronous
 *
 * This scope is designed for callbacks from the XXDK native layer (Java/JNI).
 * Native callbacks run on threads managed by the XXDK library, so we launch
 * coroutines to handle database operations without blocking those threads.
 */
class CallbackCoroutineScope(
    dispatchers: CoroutineDispatchers = CoroutineDispatchers()
) : CoroutineScope {

    companion object {
        private const val TAG = "CallbackScope"
    }

    /**
     * Exception handler that logs errors but doesn't crash the app.
     * Errors in message processing shouldn't break the messaging system.
     */
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Error in callback coroutine: ${throwable.message}", throwable)
    }

    /**
     * Coroutine context composition:
     * - SupervisorJob: Isolates failures between sibling coroutines
     * - IO dispatcher: For database and network operations
     * - ExceptionHandler: Centralized error handling
     */
    override val coroutineContext: CoroutineContext =
        SupervisorJob() +
            dispatchers.io +
            exceptionHandler

    /**
     * Launch a callback operation with proper error handling.
     *
     * @param block The suspend function to execute
     * @return The Job for this coroutine, can be used for cancellation
     */
    fun launchCallback(block: suspend CoroutineScope.() -> Unit) = launch {
        try {
            block()
        } catch (e: Exception) {
            // ExceptionHandler will also catch this, but we can add context here
            Log.e(TAG, "Callback operation failed: ${e.message}")
            throw e
        }
    }

    /**
     * Clean up the scope when it's no longer needed.
     * Call this during logout or when the messaging system is shut down.
     */
    fun shutdown() {
        Log.d(TAG, "Shutting down callback scope")
        cancel("Callback scope shutdown")
    }
}

/**
 * Singleton provider for the callback scope to ensure consistent
 * lifecycle management across the app.
 */
object CallbackScopeProvider {
    @Volatile
    private var instance: CallbackCoroutineScope? = null

    fun getInstance(): CallbackCoroutineScope {
        return instance ?: synchronized(this) {
            instance ?: CallbackCoroutineScope().also { instance = it }
        }
    }

    fun recreate(): CallbackCoroutineScope {
        synchronized(this) {
            instance?.shutdown()
            instance = CallbackCoroutineScope()
            return instance!!
        }
    }

    fun shutdown() {
        synchronized(this) {
            instance?.shutdown()
            instance = null
        }
    }
}
