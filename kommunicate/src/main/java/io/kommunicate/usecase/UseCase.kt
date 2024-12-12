package io.kommunicate.usecase

interface UseCase<T> {
    suspend fun execute(): T
}
