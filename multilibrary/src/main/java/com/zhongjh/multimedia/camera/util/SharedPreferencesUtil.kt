package com.zhongjh.multimedia.camera.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

/**
 * 保存信息配置类
 * Created by zhongjh on 2018/8/7.
 */
class SharedPreferencesUtil @SuppressLint("CommitPrefEdits") constructor(context: Context, fileName: String) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)

    /*
     * 保存手机里面的名字
     */
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun putString(key: String, value: String) {
        editor.putString(key, value)
        editor.commit()
    }

    fun putInt(key: String, value: Int) {
        editor.putInt(key, value)
        editor.commit()
    }

    fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.commit()
    }

    fun putFloat(key: String, value: Float) {
        editor.putFloat(key, value)
        editor.commit()
    }

    fun putLong(key: String, value: Long) {
        editor.putLong(key, value)
        editor.commit()
    }

    fun getString(key: String, defaultObject: String): String {
        return sharedPreferences.getString(key, defaultObject) as String
    }

    fun getInt(key: String, defaultObject: Int): Int {
        return sharedPreferences.getInt(key, defaultObject)
    }

    fun getBoolean(key: String, defaultObject: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultObject)
    }

    fun getFloat(key: String, defaultObject: Float): Float {
        return sharedPreferences.getFloat(key, defaultObject)
    }

    fun getLong(key: String, defaultObject: Long): Long {
        return sharedPreferences.getLong(key, defaultObject)
    }

    /**
     * 移除某个key值已经对应的值
     */
    fun remove(key: String) {
        editor.remove(key)
        editor.commit()
    }

    /**
     * 清除所有数据
     */
    fun clear() {
        editor.clear()
        editor.commit()
    }

    /**
     * 查询某个key是否存在
     */
    fun contain(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    val all: Map<String, *>
        /**
         * 返回所有的键值对
         */
        get() = sharedPreferences.all
}
