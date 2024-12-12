package io.kommunicate.utils

import io.kommunicate.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A utility class for executing use cases asynchronously with proper handling of success and failure callbacks.
 * This makes the UseCase execute in the synchronous environment.
 *
 * Internally this function creates the coroutine scope to execute the UseCase asynchronously.
 *
 * @param T The type of the use case, which must implement [UseCase].
 * @param R The type of result produced by the use case upon execution.
 * @property useCase The use case instance to be executed.
 * @property onComplete A callback invoked when the use case completes successfully with the result.
 * @property onFailed A callback invoked when the use case execution fails with an exception.
 * @property dispatcher The [CoroutineDispatcher] to be used for executing the use case. Defaults to [Dispatchers.IO].
 */
class UseCaseExecutor<T: UseCase<R>, R>(
    private val useCase: T,
    private val onComplete: (R) -> Unit,
    private val onFailed: (Exception) -> Unit,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(dispatcher)
    private var job: Job? = null

    fun invoke() {
        job = coroutineScope.launch {
            try {
                val result = withContext(dispatcher) {
                    useCase.execute()
                }
                withContext(Dispatchers.Main) { onComplete(result) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onFailed(e) }
            }
        }
    }

    fun cancel() {
        job?.cancel()
    }
}