package com.example.haven.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injectable dispatcher provider for testability and flexibility.
 *
 * Best Practice: Inject dispatchers instead of hardcoding them.
 * This allows for easy testing with TestDispatcher and flexibility
 * to change dispatchers without modifying business logic.
 */
@Singleton
class CoroutineDispatchers @Inject constructor() {
    val main: CoroutineDispatcher = Dispatchers.Main
    val io: CoroutineDispatcher = Dispatchers.IO
    val default: CoroutineDispatcher = Dispatchers.Default
    val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}

/**
 * Interface for providing coroutine dispatchers.
 * Use this for testing with TestDispatcher implementations.
 */
interface DispatcherProvider {
    fun main(): CoroutineDispatcher
    fun io(): CoroutineDispatcher
    fun default(): CoroutineDispatcher
    fun unconfined(): CoroutineDispatcher
}

/**
 * Production dispatcher provider.
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override fun main(): CoroutineDispatcher = Dispatchers.Main
    override fun io(): CoroutineDispatcher = Dispatchers.IO
    override fun default(): CoroutineDispatcher = Dispatchers.Default
    override fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}
