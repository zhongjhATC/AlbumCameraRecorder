package com.zhongjh.albumcamerarecorder.camera.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * 保存信息配置类
 * Created by zhongjh on 2018/8/7.
 */
public class SharedPreferencesUtil {

    private SharedPreferences sharedPreferences;
    /*
     * 保存手机里面的名字
     */private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public SharedPreferencesUtil(Context context, String fileName) {
        sharedPreferences = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void putString(String key, String object) {
        editor.putString(key, object);
        editor.commit();
    }

    public void putInt(String key, Integer object) {
        editor.putInt(key, object);
        editor.commit();
    }

    public void putBoolean(String key, Boolean object) {
        editor.putBoolean(key, object);
        editor.commit();
    }

    public void putFloat(String key, Float object) {
        editor.putFloat(key, object);
        editor.commit();
    }

    public void putLong(String key, Long object) {
        editor.putLong(key, object);
        editor.commit();
    }

    public String getString(String key, String defaultObject) {
        return sharedPreferences.getString(key, defaultObject);
    }

    public Integer getInt(String key, Integer defaultObject) {
        return sharedPreferences.getInt(key, defaultObject);
    }

    public Boolean getBoolean(String key, Boolean defaultObject) {
        return sharedPreferences.getBoolean(key, defaultObject);
    }

    public Float getFloat(String key, Float defaultObject) {
        return sharedPreferences.getFloat(key, defaultObject);
    }

    public Long getLong(String key, Long defaultObject) {
        return sharedPreferences.getLong(key, defaultObject);
    }

    /**
     * 移除某个key值已经对应的值
     */
    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    /**
     * 清除所有数据
     */
    public void clear() {
        editor.clear();
        editor.commit();
    }

    /**
     * 查询某个key是否存在
     */
    public Boolean contain(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * 返回所有的键值对
     */
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }


}
