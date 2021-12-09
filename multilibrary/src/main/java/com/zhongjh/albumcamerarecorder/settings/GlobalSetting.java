package com.zhongjh.albumcamerarecorder.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;

import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.engine.ImageEngine;

import com.zhongjh.common.entity.SaveStrategy;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.common.utils.MediaStoreCompat;

import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;
import com.zhongjh.albumcamerarecorder.listener.CompressionInterface;
import com.zhongjh.albumcamerarecorder.listener.OnMainListener;
import com.zhongjh.albumcamerarecorder.settings.api.GlobalSettingApi;
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils;

import java.io.File;
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
 * 用于构建媒体具体公共设置 API。
 *
 * @author zhongjh
 * @date 2018/9/28
 */
public final class GlobalSetting implements GlobalSettingApi {

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

    @Override
    public void onDestroy() {
        mGlobalSpec.onMainListener = null;
        if (mGlobalSpec.albumSetting != null) {
            mGlobalSpec.albumSetting.onDestroy();
        }
        if (mGlobalSpec.cameraSetting != null) {
            mGlobalSpec.cameraSetting.onDestroy();
        }
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

    @Override
    public GlobalSetting albumSetting(AlbumSetting albumSetting) {
        mGlobalSpec.albumSetting = albumSetting;
        return this;
    }

    @Override
    public GlobalSetting cameraSetting(CameraSetting cameraSetting) {
        mGlobalSpec.cameraSetting = cameraSetting;
        return this;
    }

    @Override
    public GlobalSetting recorderSetting(RecorderSetting recorderSetting) {
        mGlobalSpec.recorderSetting = recorderSetting;
        return this;
    }

    @Override
    public GlobalSetting theme(@StyleRes int themeId) {
        mGlobalSpec.themeId = themeId;
        return this;
    }

    @Override
    public GlobalSetting defaultPosition(int defaultPosition) {
        mGlobalSpec.defaultPosition = defaultPosition;
        return null;
    }


    @Override
    public GlobalSetting maxSelectablePerMediaType(Integer maxSelectable, Integer maxImageSelectable, Integer maxVideoSelectable, Integer maxAudioSelectable,
                                                   int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        if (maxSelectable == null && maxImageSelectable == null) {
            throw new IllegalStateException("maxSelectablePerMediaType 方法中如果 maxSelectable 为null，那么 maxImageSelectable 必须是0或者0以上数值");
        }
        if (maxSelectable == null && maxVideoSelectable == null) {
            throw new IllegalStateException("maxSelectablePerMediaType 方法中如果 maxSelectable 为null，那么 maxVideoSelectable 必须是0或者0以上数值");
        }
        if (maxSelectable == null && maxAudioSelectable == null) {
            throw new IllegalStateException("maxSelectablePerMediaType 方法中如果 maxSelectable 为null，那么 maxAudioSelectable 必须是0或者0以上数值");
        }
        if (maxSelectable != null && maxImageSelectable != null && maxImageSelectable > maxSelectable) {
            throw new IllegalStateException("maxSelectable 必须比 maxImageSelectable 大");
        }
        if (maxSelectable != null && maxVideoSelectable != null && maxVideoSelectable > maxSelectable) {
            throw new IllegalStateException("maxSelectable 必须比 maxVideoSelectable 大");
        }
        if (maxSelectable != null && maxAudioSelectable != null && maxAudioSelectable > maxSelectable) {
            throw new IllegalStateException("maxSelectable 必须比 maxAudioSelectable 大");
        }

        // 计算
        if (maxSelectable != null) {
            mGlobalSpec.maxSelectable = maxSelectable - (alreadyImageCount + alreadyVideoCount + alreadyAudioCount);
        }
        if (maxImageSelectable != null) {
            mGlobalSpec.maxImageSelectable = maxImageSelectable - alreadyImageCount;
        } else {
            mGlobalSpec.maxImageSelectable = null;
        }
        if (maxVideoSelectable != null) {
            mGlobalSpec.maxVideoSelectable = maxVideoSelectable - alreadyVideoCount;
        } else {
            mGlobalSpec.maxVideoSelectable = null;
        }
        if (maxAudioSelectable != null) {
            mGlobalSpec.maxAudioSelectable = maxAudioSelectable - alreadyAudioCount;
        } else {
            mGlobalSpec.maxAudioSelectable = null;
        }
        return this;
    }

    @Override
    public GlobalSetting alreadyCount(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        // 计算
        if (mGlobalSpec.maxSelectable != null) {
            mGlobalSpec.maxSelectable = mGlobalSpec.maxSelectable - (alreadyImageCount + alreadyVideoCount + alreadyAudioCount);
        }
        if (mGlobalSpec.maxImageSelectable != null) {
            mGlobalSpec.maxImageSelectable = mGlobalSpec.maxImageSelectable - alreadyImageCount;
        }
        if (mGlobalSpec.maxVideoSelectable != null) {
            mGlobalSpec.maxVideoSelectable = mGlobalSpec.maxVideoSelectable - alreadyVideoCount;
        }
        if (mGlobalSpec.maxAudioSelectable != null) {
            mGlobalSpec.maxAudioSelectable = mGlobalSpec.maxAudioSelectable - alreadyAudioCount;
        }
        return this;
    }

    @Override
    public GlobalSetting allStrategy(SaveStrategy saveStrategy) {
        mGlobalSpec.saveStrategy = saveStrategy;
        return this;
    }

    @Override
    public GlobalSetting pictureStrategy(SaveStrategy saveStrategy) {
        mGlobalSpec.pictureStrategy = saveStrategy;
        return this;
    }

    @Override
    public GlobalSetting videoStrategy(SaveStrategy saveStrategy) {
        mGlobalSpec.videoStrategy = saveStrategy;
        return this;
    }

    @Override
    public GlobalSetting audioStrategy(SaveStrategy saveStrategy) {
        mGlobalSpec.audioStrategy = saveStrategy;
        return this;
    }

    @Override
    public GlobalSetting imageEngine(ImageEngine imageEngine) {
        mGlobalSpec.imageEngine = imageEngine;
        return this;
    }

    @Override
    public GlobalSetting isCutscenes(boolean isCutscenes) {
        mGlobalSpec.isCutscenes = isCutscenes;
        return this;
    }

    @Override
    public GlobalSetting isImageEdit(boolean isImageEdit) {
        mGlobalSpec.isImageEdit = isImageEdit;
        return this;
    }

    @Override
    public GlobalSetting setOnCompressionInterface(@Nullable CompressionInterface listener) {
        mGlobalSpec.compressionInterface = listener;
        return this;
    }

    @NonNull
    @Override
    public GlobalSetting setOnMainListener(@Nullable OnMainListener listener) {
        mGlobalSpec.onMainListener = listener;
        return this;
    }

    @Override
    public void forResult(int requestCode) {
        mGlobalSpec.requestCode = requestCode;
        Activity activity = mMultiMediaSetting.getActivity();
        if (activity == null) {
            return;
        }
        // 数量
        int numItems = 0;
        // 根据相关配置做相应的初始化
        if (mGlobalSpec.albumSetting != null) {
            numItems++;
        }
        if (mGlobalSpec.cameraSetting != null) {
            numItems++;
        }
        if (mGlobalSpec.recorderSetting != null && numItems <= 0) {
            if (SelectableUtils.getAudioMaxCount() > 0) {
                numItems++;
            } else {
                if (mGlobalSpec.onMainListener != null) {
                    mGlobalSpec.onMainListener.onOpenFail(activity.getResources().getString(R.string.z_multi_library_the_recording_limit_has_been_reached));
                } else {
                    Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.z_multi_library_the_recording_limit_has_been_reached), Toast.LENGTH_LONG).show();
                }
            }
        }
        if (numItems <= 0) {
            throw new IllegalStateException(activity.getResources().getString(R.string.z_one_of_these_three_albumSetting_camerasSetting_and_recordDerSetting_must_be_set));
        }

        Intent intent = new Intent(activity, MainActivity.class);

        Fragment fragment = mMultiMediaSetting.getFragment();
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
            if (mGlobalSpec.isCutscenes) {
                activity.overridePendingTransition(R.anim.activity_open, 0);
            }
        }

    }

}
