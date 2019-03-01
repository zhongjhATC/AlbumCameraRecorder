package com.zhongjh.albumcamerarecorder.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.album.engine.ImageEngine;
import com.zhongjh.albumcamerarecorder.album.engine.impl.GlideEngine;
import com.zhongjh.albumcamerarecorder.album.engine.impl.PicassoEngine;
import com.zhongjh.albumcamerarecorder.album.enums.MimeType;
import com.zhongjh.albumcamerarecorder.listener.OnMainListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_BEHIND;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;

/**
 * 用于构建媒体具体设置 API。
 * Created by zhongjh on 2018/9/28.
 */
public final class GlobalSetting {

    private final MultiMediaSetting mMultiMediaSetting;
    private final GlobalSpec mGlobalSpec;

    // www.代替枚举的@IntDef用法
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @IntDef({
            SCREEN_ORIENTATION_UNSPECIFIED,
            SCREEN_ORIENTATION_LANDSCAPE,
            SCREEN_ORIENTATION_PORTRAIT,
            SCREEN_ORIENTATION_USER,
            SCREEN_ORIENTATION_BEHIND,
            SCREEN_ORIENTATION_SENSOR,
            SCREEN_ORIENTATION_NOSENSOR,
            SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
            SCREEN_ORIENTATION_SENSOR_PORTRAIT,
            SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
            SCREEN_ORIENTATION_REVERSE_PORTRAIT,
            SCREEN_ORIENTATION_FULL_SENSOR,
            SCREEN_ORIENTATION_USER_LANDSCAPE,
            SCREEN_ORIENTATION_USER_PORTRAIT,
            SCREEN_ORIENTATION_FULL_USER,
            SCREEN_ORIENTATION_LOCKED
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface ScreenOrientation {
    }

    /**
     * 在上下文中构造新的规范生成器。
     *
     * @param multiMediaSetting 在 requester context wrapper.
     * @param mimeTypes         设置为选择的 {@link MimeType} 类型
     */
    GlobalSetting(MultiMediaSetting multiMediaSetting, @NonNull Set<MimeType> mimeTypes) {
        mMultiMediaSetting = multiMediaSetting;
        mGlobalSpec = GlobalSpec.getCleanInstance();
        mGlobalSpec.setMimeTypeSet(mimeTypes);
//        mGlobalSpec.orientation = SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public GlobalSetting albumSetting(AlbumSetting albumSetting) {
        mGlobalSpec.albumSetting = albumSetting;
        return this;
    }

    public GlobalSetting cameraSetting(CameraSetting cameraSetting) {
        mGlobalSpec.cameraSetting = cameraSetting;
        return this;
    }

    public GlobalSetting recorderSetting(RecorderSetting recorderSetting) {
        mGlobalSpec.recorderSetting = recorderSetting;
        return this;
    }

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
    public GlobalSetting theme(@StyleRes int themeId) {
        mGlobalSpec.themeId = themeId;
        return this;
    }

    /**
     * 仅当 {@link AlbumSpec#mediaTypeExclusive} 设置为true并且您希望为图像和视频媒体类型设置不同的最大可选文件时才有用。
     *
     * @param maxImageSelectable imga的最大可选计数.
     * @param maxVideoSelectable video的最大可选计数.
     * @param maxAudioSelectable audio的最大可选计数.
     * @return {@link GlobalSetting} this
     */
    public GlobalSetting maxSelectablePerMediaType(int maxImageSelectable, int maxVideoSelectable, int maxAudioSelectable) {
        mGlobalSpec.maxImageSelectable = maxImageSelectable;
        mGlobalSpec.maxVideoSelectable = maxVideoSelectable;
        mGlobalSpec.maxAudioSelectable = maxAudioSelectable;
        return this;
    }

    /**
     * 为保存内部和外部照片的位置提供的捕获策略{@link android.support.v4.content.FileProvider}.
     *
     * @param captureStrategy {@link CaptureStrategy}, 仅在启用捕获时需要
     * @return {@link GlobalSetting} this
     */
    public GlobalSetting captureStrategy(CaptureStrategy captureStrategy) {
        mGlobalSpec.captureStrategy = captureStrategy;
        return this;
    }

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
    public GlobalSetting imageEngine(ImageEngine imageEngine) {
        mGlobalSpec.imageEngine = imageEngine;
        return this;
    }

    /**
     * 有关首页的一些事件
     * <p>
     * 这是一个冗余的api {@link MultiMediaSetting#obtainResult(Intent)},
     * 我们只建议您在需要立即执行某些操作时使用此API。
     *
     * @param listener {@link OnMainListener}
     * @return {@link GlobalSetting} for fluent API.
     */
    @NonNull
    public GlobalSetting setOnMainListener(@Nullable OnMainListener listener) {
        mGlobalSpec.onMainListener = listener;
        return this;
    }

    /**
     * 开始进行多媒体操作并等待结果.
     *
     * @param requestCode 请求活动或片段的标识.
     */
    public void forResult(int requestCode) {
        Activity activity = mMultiMediaSetting.getActivity();
        if (activity == null) {
            return;
        }

        int numItems = 0;// 数量
        // 根据相关配置做相应的初始化
        if (mGlobalSpec.albumSetting != null) {
            numItems++;
        }
        if (mGlobalSpec.cameraSetting != null) {
            numItems++;
        }
        if (mGlobalSpec.recorderSetting != null && numItems <= 0) {
            if (mGlobalSpec.maxAudioSelectable > 0) {
                numItems++;
            } else {
                if (mGlobalSpec.onMainListener != null){
                    mGlobalSpec.onMainListener.onOpenFail("录音已经达到上限");
                }else{
                    Toast.makeText(activity.getApplicationContext(), "录音已经达到上限", Toast.LENGTH_LONG).show();
                }
            }
        }
        if (numItems <= 0) {
            throw new IllegalStateException("必须在这三项 albumSetting、cameraSetting、recorderSetting设置其中一项 ");
        }

        Intent intent = new Intent(activity, MainActivity.class);

        Fragment fragment = mMultiMediaSetting.getFragment();
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }


}
