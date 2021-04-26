package com.zhongjh.albumcamerarecorder.settings;
import androidx.annotation.StyleRes;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.engine.ImageEngine;
import com.zhongjh.albumcamerarecorder.album.engine.impl.GlideEngine;

import gaode.zhongjh.com.common.entity.SaveStrategy;
import gaode.zhongjh.com.common.enums.MimeType;
import com.zhongjh.albumcamerarecorder.listener.OnMainListener;
import com.zhongjh.albumcamerarecorder.utils.constants.ModuleTypes;

import java.util.Set;

/**
 * 设置的一些属性,别的界面也根据这个来进行动态改变
 * Created by zhongjh on 2018/8/23.
 */
public class GlobalSpec {

    public AlbumSetting albumSetting;   // 相册的设置
    public CameraSetting cameraSetting; // 拍摄的设置
    public RecorderSetting recorderSetting;// 录音的设置
    private Set<MimeType> mimeTypeSet; // 选择 mime 的类型，MimeType.allOf()
    public boolean hasInited; // 是否通过正规方式进来
    public int defaultPosition;    // 默认从第几个开始
    @StyleRes
    public int themeId;         // 样式
    public int maxImageSelectable = -1;  // 最大图片选择数量
    public int maxVideoSelectable = -1;  // 最大视频选择数量
    public int maxAudioSelectable = -1;  // 最大音频选择数量
    public SaveStrategy saveStrategy; // 拍照\录像\存储的保存路径 参数1 true表示在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
    public SaveStrategy pictureStrategy; // 图片保存路径 参数1 true表示在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
    public SaveStrategy videoStrategy; // 视频保存路径 参数1 true表示在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
    public SaveStrategy audioStrategy; // 音频保存路径 参数1 true表示在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
    public ImageEngine imageEngine;
    public boolean isCutscenes; // 是否启动过场动画，只包括开始打开界面和关闭界面的过场动画
    public boolean isImageEdit; // 图片是否开启编辑功能，涉及功能：预览、拍照
    public OnMainListener onMainListener;// 主界面的有关事件


    private GlobalSpec() {
    }

    public static GlobalSpec getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static GlobalSpec getCleanInstance() {
        GlobalSpec globalSpec = getInstance();
        globalSpec.reset();
        return globalSpec;
    }

    /**
     * 重置
     */
    private void reset() {
        albumSetting = null;
        cameraSetting = null;
        recorderSetting = null;
        mimeTypeSet = null;
        defaultPosition = 0;
        themeId = R.style.AppTheme_Blue;
        maxImageSelectable = -1;
        maxVideoSelectable = -1;
        maxAudioSelectable = -1;
        saveStrategy = null;
        pictureStrategy = null;
        videoStrategy = null;
        audioStrategy = null;
        hasInited = true;
        imageEngine = new GlideEngine();
        isCutscenes = true;
        isImageEdit = true;
    }

    private static final class InstanceHolder {
        private static final GlobalSpec INSTANCE = new GlobalSpec();
    }

    /**
     * @return 返回 mime 的类型，MimeType.allOf()
     */
    public Set<MimeType> getMimeTypeSet(@ModuleTypes int moduleTypes) {
        // 优先取各自的类型，如果没设置则取公共的
        switch (moduleTypes) {
            case ModuleTypes.ALBUM:
                if (AlbumSpec.getInstance().mimeTypeSet != null) {
                    return AlbumSpec.getInstance().mimeTypeSet;
                } else {
                    return GlobalSpec.getInstance().mimeTypeSet;
                }
            case ModuleTypes.CAMERA:
                if (CameraSpec.getInstance().mimeTypeSet != null) {
                    return CameraSpec.getInstance().mimeTypeSet;
                } else {
                    return GlobalSpec.getInstance().mimeTypeSet;
                }

        }
        return mimeTypeSet;
    }

    public void setMimeTypeSet(Set<MimeType> mimeTypeSet) {
        this.mimeTypeSet = mimeTypeSet;
    }

}
