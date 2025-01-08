package io.kommunicate.models

import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

object SessionCache {
    private val sessionData: MutableMap<String, Any> = ConcurrentHashMap()
    private val expireTimeData: MutableMap<String, Long> = ConcurrentHashMap()

    /**
     * Add or update a value in the session cache.
     * @param key The key for the value.
     * @param value The value to cache.
     */
    fun <T> put(key: String, value: T, expireDuration: Long) {
        sessionData[key] = value as Any
        expireTimeData[key] = Calendar.getInstance().timeInMillis + expireDuration
    }

    /**
     * Retrieve a value from the session cache if it doesn't expire.
     * @param key The key for the value.
     * @return The cached value, or null if not found.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        val data: T = sessionData[key] as? T ?: return null
        val expireTimeEpoch = expireTimeData.getOrElse(key) { 0L }
        val currTimeEpoch = Calendar.getInstance().timeInMillis
        return if (expireTimeEpoch > currTimeEpoch) {
            data
        }else {
            remove(key)
            null
        }
    }

    /**
     * Check if a key exists in the session cache.
     * @param key The key to check.
     * @return True if the key exists, false otherwise.
     */
    fun containsKey(key: String): Boolean {
        return sessionData.containsKey(key) && expireTimeData.containsKey(key)
    }

    /**
     * Remove a value from the session cache.
     * @param key The key to remove.
     */
    fun remove(key: String) {
        sessionData.remove(key)
        expireTimeData.remove(key)
    }

    /**
     * Clear all session data.
     */
    fun clear() {
        sessionData.clear()
        expireTimeData.clear()
    }
}