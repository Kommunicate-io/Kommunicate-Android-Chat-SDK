package com.applozic.mobicomkit.usecase

interface UseCase<T> {
    suspend fun execute(): T
}
