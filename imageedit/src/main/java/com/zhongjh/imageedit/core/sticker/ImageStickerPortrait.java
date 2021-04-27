package com.zhongjh.imageedit.core.sticker;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;

/**
 *
 * @author felix
 * @date 2017/11/16 下午5:54
 */

public interface ImageStickerPortrait {

    /**
     * 显示
     */
    boolean show();

    /**
     * 删除
     */
    boolean remove();

    /**
     * 关闭
     */
    boolean dismiss();

    /**
     * 是否显示中
     */
    boolean isShowing();

    /**
     * @return 返回Frame
     */
    RectF getFrame();

    /**
     * 黏贴
     */
    void onSticker(Canvas canvas);

    /**
     * 注册回调
     */
    void registerCallback(Callback callback);

    /**
     * 注销回调
     */
    void unregisterCallback(Callback callback);

    interface Callback {

        /**
         * 关闭
         */
        <V extends View & ImageSticker> void onDismiss(V stickerView);

        /**
         * 显示
         */
        <V extends View & ImageSticker> void onShowing(V stickerView);

        /**
         * 删除
         */
        <V extends View & ImageSticker> boolean onRemove(V stickerView);
    }
}
