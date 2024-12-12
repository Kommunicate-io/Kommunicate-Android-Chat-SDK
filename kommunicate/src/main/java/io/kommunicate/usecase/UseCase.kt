package io.kommunicate.usecase

/**
 * It represents a use case in the application.
 *
 * @param T The type of result produced by the use case upon execution.
 */
interface UseCase<T> {

    /**
     * Executes the use case and returns the result.
     *
     * This method is designed to be executed asynchronously using coroutines.
     *
     * @return The result of type [T] produced by the use case.
     * @throws Exception If an error occurs during execution, the exception should be propagated or handled.
     */
    suspend fun execute(): T
}
