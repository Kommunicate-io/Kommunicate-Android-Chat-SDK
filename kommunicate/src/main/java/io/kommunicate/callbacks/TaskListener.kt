package io.kommunicate.callbacks

/**
 * This interface provides callbacks for handling success and error cases in asynchronous operations.
 *
 * @param T The type of data that will be returned on successful completion of the task
 *
 * Example usage:
 * ```kotlin
 * class MyClass : TaskListener<String> {
 *     override fun onSuccess(status: String) {
 *         // Handle successful result
 *     }
 *
 *     override fun onFailure(error: Exception) {
 *         // Handle error case
 *     }
 * }
 * ```
 */
interface TaskListener<T> {
    /**
     * Called when the task completes successfully.
     *
     * @param status The result data of type T from the successful operation
     */
    fun onSuccess(status: T)

    /**
     * Called when the task encounters an error.
     *
     * @param error The exception that occurred during task execution
     */
    fun onFailure(error: Exception)
}