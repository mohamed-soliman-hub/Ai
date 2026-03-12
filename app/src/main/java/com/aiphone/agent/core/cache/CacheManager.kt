package com.aiphone.agent.core.cache

import android.util.LruCache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheManager @Inject constructor() {
    private val cache = LruCache<String, Pair<String, Long>>(100)

    fun put(key: String, value: String, ttlMs: Long = 60_000L) {
        cache.put(key, Pair(value, System.currentTimeMillis() + ttlMs))
    }
    fun get(key: String): String? {
        val entry = cache.get(key) ?: return null
        return if (System.currentTimeMillis() < entry.second) entry.first else { cache.remove(key); null }
    }
    fun invalidate(key: String) { cache.remove(key) }
    fun invalidatePrefix(prefix: String) {
        cache.snapshot().keys.filter { it.startsWith(prefix) }.forEach { cache.remove(it) }
    }
    fun clear() { cache.evictAll() }
    fun fileListKey(directory: String, pattern: String) = "files:$directory:$pattern"
    fun ocrKey(imagePath: String, size: Long) = "ocr:$imagePath:$size"
}
