package com.even.zining.inherit.sound.tool.data

import com.tencent.mmkv.MMKV

object MMKVUtils {
    private val mmkv: MMKV by lazy {
        MMKV.defaultMMKV()
    }

    // 存储 Int 值
    fun put(key: String, value: Int) {
        mmkv.encode(key, value)
    }

    // 读取 Int 值
    fun getInt(key: String, default: Int = 0): Int {
        return mmkv.decodeInt(key, default)
    }

    // 存储 String 值
    fun put(key: String, value: String) {
        mmkv.encode(key, value)
    }

    // 读取 String 值
    fun getString(key: String, default: String = ""): String {
        return mmkv.decodeString(key, default) ?: default
    }

    // 存储 Boolean 值
    fun put(key: String, value: Boolean) {
        mmkv.encode(key, value)
    }

    // 读取 Boolean 值
    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return mmkv.decodeBool(key, default)
    }

    // 删除单个键值
    fun removeKey(key: String) {
        mmkv.removeValueForKey(key)
    }

    // 清空所有数据
    fun clearAll() {
        mmkv.clearAll()
    }
}
