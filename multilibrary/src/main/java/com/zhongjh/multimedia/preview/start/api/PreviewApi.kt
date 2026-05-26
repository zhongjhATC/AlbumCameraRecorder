package com.zhongjh.multimedia.preview.start.api

import android.content.Intent
import android.os.Bundle
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.multimedia.preview.start.PreviewSetting

/**
 * 因为打开预览界面的方式很多种，该类单独抽取出来做处理
 * 主要是将intent封装成链式代码
 */
interface PreviewApi {

    /**
     * 数据源
     * @param localMediaArrayList 数据源
     * @return [PreviewSetting] this
     */
    fun setLocalMediaArrayList(localMediaArrayList: ArrayList<LocalMedia>): PreviewSetting

    /**
     * 这个配置是搭配相册界面含有相机按钮使用的，如果相册界面含有相机按钮，需要设置为true
     * 相册界面是否显示相机按钮,默认不显示
     * @param isDisplayCamera 相册界面是否显示相机按钮
     * @return [PreviewSetting] this
     */
    fun isDisplayCamera(isDisplayCamera: Boolean): PreviewSetting

    /**
     * 当前选择的索引
     * @param currentPosition 当前选择的索引
     * @return [PreviewSetting] this
     */
    fun setCurrentPosition(currentPosition: Int): PreviewSetting

    /**
     * 设置是否开启原图
     * @param isOriginal 设置是否开启原图
     * @return [PreviewSetting] this
     */
    fun isOriginal(isOriginal: Boolean): PreviewSetting

    /**
     * 设置是否开启 选择时验证是否满足可以被选中的条件
     * @param isSelectedCheck 设置是否开启原图
     * @return [PreviewSetting] this
     */
    fun isSelectedCheck(isSelectedCheck: Boolean): PreviewSetting

    /**
     * 设置是否开启编辑功能
     * @param isEdit 设置是否开启原图
     * @return [PreviewSetting] this
     */
    fun isEdit(isEdit: Boolean): PreviewSetting

    /**
     * 设置是否启动 '确定' 功能
     * @param isApply 设置是否开启原图
     * @return [PreviewSetting] this
     */
    fun isApply(isApply: Boolean): PreviewSetting

    /**
     * 最终将配置设置到intent
     * @param intent intent
     */
    fun setIntent(intent: Intent)

    /**
     * 最终将配置设置到bundle
     * @param bundle bundle
     */
    fun setBundle(bundle: Bundle)
}