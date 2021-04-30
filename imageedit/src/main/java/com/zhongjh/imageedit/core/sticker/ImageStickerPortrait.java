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
     * @return 是否成功
     */
    boolean show();

    /**
     * 删除
     * @return 是否成功
     */
    boolean remove();

    /**
     * 关闭
     * @return 是否成功
     */
    boolean dismiss();

    /**
     * 是否显示中
     * @return 是否成功
     */
    boolean isShowing();

    /**
     * 返回Frame
     * @return Frame
     */
    RectF getFrame();

    /**
     * 黏贴
     * @param canvas canvas
     */
    void onSticker(Canvas canvas);

    /**
     * 注册回调
     * @param callback 回调
     */
    void registerCallback(Callback callback);

    /**
     * 注销回调
     * @param callback 回调
     */
    void unregisterCallback(Callback callback);

    interface Callback {

        /**
         * 关闭
         * @param stickerView 标签的view
         */
        <V extends View & ImageSticker> void onDismiss(V stickerView);

        /**
         * 显示
         * @param stickerView 标签的view
         */
        <V extends View & ImageSticker> void onShowing(V stickerView);

         /**
         * 删除
         * @param stickerView 标签的view
         * @return 是否删除成功
         */
        <V extends View & ImageSticker> boolean onRemove(V stickerView);
    }
}
