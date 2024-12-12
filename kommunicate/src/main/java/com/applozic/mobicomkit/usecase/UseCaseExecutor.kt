package com.applozic.mobicomkit.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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