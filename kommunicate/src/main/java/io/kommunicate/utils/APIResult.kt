package io.kommunicate.utils

/**
 * A sealed class representing the result of an API operation.
 *
 * @param T The type of data or response returned by the API operation.
 */
sealed class APIResult<T> {

    /**
     * Represents a successful API operation with the corresponding result data.
     *
     * @param T The type of the data returned.
     * @property data The data returned by the API.
     */
    data class Success<T>(val data: T): APIResult<T>()

    /**
     * Represents a failed API operation with the corresponding exception.
     *
     * @param T The type of the result, which is unused in this case.
     * @property exception The exception describing the failure.
     */
    data class Failed<T>(val exception: Exception): APIResult<T>()

    /**
     * Represents the loading state of an API operation.
     *
     * @param T The type of the result, which is unused in this case.
     * @property isLoading A boolean flag indicating whether the operation is in progress.
     */
    data class Loading<T>(val isLoading: Boolean): APIResult<T>()

    companion object {

        /**
         * Creates a [Success] instance.
         *
         * @param U The type of the data.
         * @param data The data returned by the successful API operation.
         * @return An [APIResult.Success] instance containing the given data.
         */
        fun <U> success(data: U) = Success(data)

        /**
         * Creates a [Failed] instance using a message string.
         *
         * @param U The type of the result, which is unused.
         * @param message The error message describing the failure.
         * @return An [APIResult.Failed] instance with the exception containing the error message.
         */
        fun <U> failed(message: String) = Failed<U>(Exception(message))

        /**
         * Creates a [Failed] instance using an exception.
         *
         * @param U The type of the result, which is unused.
         * @param exception The exception describing the failure.
         * @return An [APIResult.Failed] instance with the given exception.
         */
        fun <U> failedWithException(exception: Exception) = Failed<U>(exception)

        /**
         * Creates a [Loading] instance.
         *
         * @param U The type of the result, which is unused.
         * @param isLoading A boolean flag indicating whether the operation is in progress.
         * @return An [APIResult.Loading] instance with the loading state.
         */
        fun <U> loading(isLoading: Boolean) = Loading<U>(isLoading)
    }
}

/**
 * Extension function to handle the success case of an [APIResult].
 *
 * @param T The type of the data returned by the API operation.
 * @param onSuccess A lambda function to handle the success case, receiving the result data as input.
 * @return The original [APIResult] instance, allowing for method chaining.
 */
fun <T> APIResult<T>.onSuccess(
    onSuccess: (T) -> Unit
): APIResult<T> {
    return when(this) {
        is APIResult.Failed -> {
            this
        }
        is APIResult.Loading -> {
            this
        }
        is APIResult.Success -> {
            onSuccess(this.data)
            this
        }
    }
}

/**
 * Extension function to handle the failure case of an [APIResult].
 *
 * @param T The type of the data returned by the API operation.
 * @param onFailure A lambda function to handle the failure case, receiving the exception as input.
 * @return The original [APIResult] instance, allowing for method chaining.
 */
fun <T> APIResult<T>.onFailure(
    onFailure: (Exception) -> Unit
): APIResult<T> {
    return when(this) {
        is APIResult.Failed -> {
            onFailure(this.exception)
            this
        }
        is APIResult.Loading -> {
            this
        }
        is APIResult.Success -> {
            this
        }
    }
}