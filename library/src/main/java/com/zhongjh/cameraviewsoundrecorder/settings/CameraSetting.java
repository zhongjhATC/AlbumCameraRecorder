package com.zhongjh.cameraviewsoundrecorder.settings;

import android.support.annotation.NonNull;

import com.zhongjh.cameraviewsoundrecorder.album.enums.MimeType;

import java.util.Set;

/**
 * Created by zhongjh on 2018/12/26.
 */
public final class CameraSetting {

    private final CameraSpec mCameraSpec;

    CameraSetting(@NonNull Set<MimeType> mimeTypes) {
        mCameraSpec = CameraSpec.getInstance();
        mCameraSpec.mimeTypeSet = mimeTypes;
    }

}
