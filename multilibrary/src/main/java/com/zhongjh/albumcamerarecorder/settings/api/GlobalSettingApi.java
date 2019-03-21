package com.zhongjh.albumcamerarecorder.settings.api;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;

import com.zhongjh.albumcamerarecorder.album.engine.ImageEngine;
import com.zhongjh.albumcamerarecorder.album.engine.impl.GlideEngine;
import com.zhongjh.albumcamerarecorder.album.engine.impl.PicassoEngine;
import com.zhongjh.albumcamerarecorder.listener.OnMainListener;
import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.albumcamerarecorder.settings.RecorderSetting;

import gaode.zhongjh.com.common.entity.SaveStrategy;

/**
 * 用于构建媒体具体公共设置 API。
 * Created by zhongjh on 2019/3/21.
 */
public interface GlobalSettingApi {

    /**
     * 设置相册配置，如果不设置则不启用相册
     * @param albumSetting 相册
     * @return this
     */
    GlobalSetting albumSetting(AlbumSetting albumSetting);

    /**
     * 设置录制配置，如果不设置则不启用录制
     * @param cameraSetting 录制
     * @return this
     */
    GlobalSetting cameraSetting(CameraSetting cameraSetting);

    /**
     * 设置录音配置，如果不设置则不启用录音
     * @param recorderSetting 录音
     * @return this
     */
    GlobalSetting recorderSetting(RecorderSetting recorderSetting);

    /**
     * 主题
     * <p>
     * 有两个内置主题：
     * 1. R.style.AppTheme_Blue
     * 2. R.style.AppTheme.Dracula
     * 你可以定义从上述主题或其他主题派生的自定义主题。
     *
     * @param themeId 样式id. 默认为R.style.AppTheme_Blue
     * @return {@link GlobalSetting} this
     */
    GlobalSetting theme(@StyleRes int themeId);

    /**
     * 仅当 {@link AlbumSpec#mediaTypeExclusive} 设置为true并且您希望为图像和视频媒体类型设置不同的最大可选文件时才有用。
     *
     * @param maxImageSelectable imga的最大可选计数.
     * @param maxVideoSelectable video的最大可选计数.
     * @param maxAudioSelectable audio的最大可选计数.
     * @return {@link GlobalSetting} this
     */
    GlobalSetting maxSelectablePerMediaType(int maxImageSelectable, int maxVideoSelectable, int maxAudioSelectable);

    /**
     * 为保存内部和外部文件的位置提供的捕获策略{@link android.support.v4.content.FileProvider}.
     *
     * @param saveStrategy {@link SaveStrategy}, 仅在启用捕获时需要
     * @return {@link GlobalSetting} this
     */
    GlobalSetting allStrategy(SaveStrategy saveStrategy);

    /**
     * 如果设置这个，有关图片的优先权比allStrategy高
     * 为保存内部和外部图片文件的位置提供的捕获策略{@link android.support.v4.content.FileProvider}.
     *
     * @param saveStrategy {@link SaveStrategy}, 仅在启用捕获时需要
     * @return {@link GlobalSetting} this
     */
    GlobalSetting pictureStrategy(SaveStrategy saveStrategy);

    /**
     * 如果设置这个，有关视频的优先权比allStrategy高
     * 为保存内部和外部视频文件的位置提供的捕获策略{@link android.support.v4.content.FileProvider}.
     *
     * @param saveStrategy {@link SaveStrategy}, 仅在启用捕获时需要
     * @return {@link GlobalSetting} this
     */
    GlobalSetting videoStrategy(SaveStrategy saveStrategy);

    /**
     * 如果设置这个，有关音频的优先权比allStrategy高
     * 为保存内部和外部音频文件的位置提供的捕获策略{@link android.support.v4.content.FileProvider}.
     *
     * @param saveStrategy {@link SaveStrategy}, 仅在启用捕获时需要
     * @return {@link GlobalSetting} this
     */
    GlobalSetting audioStrategy(SaveStrategy saveStrategy);

    /**
     * 提供图像引擎。
     * <p>
     * 有两个内置图像引擎：
     * 1. {@link GlideEngine}
     * 2. {@link PicassoEngine}
     * 你可以实现你自己的图像引擎。
     *
     * @param imageEngine {@link ImageEngine}
     * @return {@link GlobalSetting} this
     */
    GlobalSetting imageEngine(ImageEngine imageEngine);

    /**
     * 有关首页的一些事件
     * <p>
     * 这是一个冗余的api {@link MultiMediaSetting#obtainResult(Intent)},
     * 我们只建议您在需要立即执行某些操作时使用此API。
     *
     * @param listener {@link OnMainListener}
     * @return {@link GlobalSetting} for fluent API.
     */
    GlobalSetting setOnMainListener(@Nullable OnMainListener listener);

    /**
     * 开始进行多媒体操作并等待结果.
     *
     * @param requestCode 请求活动或片段的标识.
     */
    void forResult(int requestCode);

}
