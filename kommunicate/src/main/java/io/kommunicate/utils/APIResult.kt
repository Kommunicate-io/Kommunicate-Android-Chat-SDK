package io.kommunicate.utils

sealed class APIResult<T> {
    data class Success<T>(val data: T): APIResult<T>()
    data class Failed<T>(val exception: Exception): APIResult<T>()
    data class Loading<T>(val isLoading: Boolean): APIResult<T>()

    companion object {
        fun <U> success(data: U) = Success(data)
        fun <U> failed(message: String) = Failed<U>(Exception(message))
        fun <U> failedWithException(exception: Exception) = Failed<U>(exception)
        fun <U> loading(isLoading: Boolean) = Loading<U>(isLoading)
    }
}

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