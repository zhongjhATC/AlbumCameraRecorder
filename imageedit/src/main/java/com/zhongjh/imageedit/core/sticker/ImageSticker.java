package com.zhongjh.imageedit.core.sticker;


import com.zhongjh.imageedit.core.ImageViewPortrait;

/**
 * 图像贴纸接口，定义了贴纸组件的基本行为
 * 扩展了图像贴纸方位接口和图像视图方位接口的功能
 * 
 * 该接口作为贴纸功能的核心抽象层，结合了贴纸的显示/隐藏/移除等生命周期管理
 * 和图像视图的缩放/旋转/平移等交互操作能力
 * 所有具体的贴纸实现类（如图片贴纸、文字贴纸等）都应实现此接口
 * 
 * @author felix
 * @date 2017/11/14 下午7:31
 */
public interface ImageSticker extends ImageStickerPortrait, ImageViewPortrait {

}
