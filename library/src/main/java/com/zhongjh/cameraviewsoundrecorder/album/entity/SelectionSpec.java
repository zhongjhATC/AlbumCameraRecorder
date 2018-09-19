package com.zhongjh.cameraviewsoundrecorder.album.entity;

import android.content.pm.ActivityInfo;
import android.support.annotation.StyleRes;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.album.engine.ImageEngine;
import com.zhongjh.cameraviewsoundrecorder.album.filter.Filter;
import com.zhongjh.cameraviewsoundrecorder.album.listener.OnCheckedListener;
import com.zhongjh.cameraviewsoundrecorder.album.listener.OnSelectedListener;

import java.util.List;

/**
 * 选择规格
 * Created by zhongjh on 2018/8/23.
 */
public class SelectionSpec {

    public boolean mediaTypeExclusive; // 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
    public boolean hasInited; // 是否通过正规方式进来
    @StyleRes
    public int themeId;         // 样式
    public int orientation;     // 旋转模式
    public boolean countable;   // 是否多选时,显示数字
    public int maxSelectable;   // 最大选择数量
    public int maxImageSelectable;  // 最大图片选择数量
    public int maxVideoSelectable;  // 最大视频选择数量
    public boolean capture;         // 是否可以拍照
    public ImageEngine imageEngine;
    public OnSelectedListener onSelectedListener;
    public boolean originalable;    // 是否原图
    public int originalMaxSize;     // 最大原图size,仅当originalEnable为true的时候才有效
    public OnCheckedListener onCheckedListener;
    public List<Filter> filters;

    private SelectionSpec() {
    }

    public static SelectionSpec getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static SelectionSpec getCleanInstance() {
        SelectionSpec selectionSpec = getInstance();
        selectionSpec.reset();
        return selectionSpec;
    }

    /**
     * 重置
     */
    private void reset() {
        mediaTypeExclusive = true;
        themeId = R.style.AppTheme_Blue;
        orientation = 0;
        hasInited = true;
    }

    /**
     * 是否需要旋转约束
     * @return 是否
     */
    public boolean needOrientationRestriction() {
        // SCREEN_ORIENTATION_UNSPECIFIED:未指定，此为默认值。由Android系统自己选择合适的方向
        return orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    /**
     * 是否不显示多选数字和是否单选
     * @return 是否
     */
    public boolean singleSelectionModeEnabled() {
        return !countable && (maxSelectable == 1 || (maxImageSelectable == 1 && maxVideoSelectable == 1));
    }

    private static final class InstanceHolder {
        private static final SelectionSpec INSTANCE = new SelectionSpec();
    }


}
