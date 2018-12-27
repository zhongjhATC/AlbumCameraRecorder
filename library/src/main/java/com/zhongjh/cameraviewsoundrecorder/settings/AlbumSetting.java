package com.zhongjh.cameraviewsoundrecorder.settings;

import android.support.annotation.NonNull;

import com.zhongjh.cameraviewsoundrecorder.album.enums.MimeType;

import java.util.Set;

/**
 * Created by zhongjh on 2018/12/27.
 */

public class AlbumSetting {

    private final AlbumSpec mAlbumSpec;

    public AlbumSetting(@NonNull Set<MimeType> mimeTypes) {
        mCameraSpec = CameraSpec.getInstance();
        mCameraSpec.mimeTypeSet = mimeTypes;
    }



}
