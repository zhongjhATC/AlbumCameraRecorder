package com.zhongjh.imageedit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.zhongjh.imageedit.core.ImageMode;
import com.zhongjh.imageedit.core.ImageText;
import com.zhongjh.imageedit.core.file.BaseImageDecoder;
import com.zhongjh.imageedit.core.file.ImageAssetFileDecoder;
import com.zhongjh.imageedit.core.file.ImageContentDecoder;
import com.zhongjh.imageedit.core.file.ImageFileDecoder;
import com.zhongjh.imageedit.core.util.BitmapLoadUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图像编辑活动类，提供图像编辑功能的主要交互界面
 * 支持图像裁剪、涂鸦、马赛克、文本添加等操作
 * 
 * @author felix
 * @date 2017/11/14 下午2:26
 */
public class ImageEditActivity extends BaseImageEditActivity {

    /**
     * 日志标签，用于调试和错误日志输出
     */
    private static final String TAG = "ImageEditActivity";

    /**
     * 图像的最大宽度限制
     */
    private static final int MAX_WIDTH = 1024;

    /**
     * 图像的最大高度限制
     */
    private static final int MAX_HEIGHT = 1024;

    /**
     * 意图额外参数键：图像URI
     * 用于从外部传入需要编辑的图像URI
     */
    public static final String EXTRA_IMAGE_URI = "IMAGE_URI";

    /**
     * 意图额外参数键：图像保存路径
     * 用于指定编辑后图像的保存路径
     */
    public static final String EXTRA_IMAGE_SAVE_PATH = "IMAGE_SAVE_PATH";

    /**
     * 意图额外参数键：屏幕方向
     * 用于指定Activity的屏幕方向
     */
    public static final String EXTRA_IMAGE_SCREEN_ORIENTATION = "EXTRA_SCREEN_ORIENTATION";

    /**
     * 意图额外参数键：宽度
     * 用于返回编辑后图像的宽度
     */
    public static final String EXTRA_WIDTH = "EXTRA_WIDTH";

    /**
     * 意图额外参数键：高度
     * 用于返回编辑后图像的高度
     */
    public static final String EXTRA_HEIGHT = "EXTRA_HEIGHT";

    /**
     * 获取需要编辑的图像位图
     * 从Intent中获取图像URI，并根据URI的不同类型选择合适的解码器进行解码
     * 支持asset、file和content三种URI类型
     * 
     * @return 解码后的位图对象，如果解码失败则返回null
     */
    @Override
    public Bitmap getBitmap() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }

        // 从Intent中获取图像URI路径
        String path = intent.getStringExtra(EXTRA_IMAGE_URI);
        Uri uri = Uri.parse(path);

        if (uri == null || uri.getScheme() == null) {
            return null;
        }

        // 根据URI的scheme选择合适的解码器
        BaseImageDecoder decoder = null;
        switch (uri.getScheme()) {
            case "asset":
                decoder = new ImageAssetFileDecoder(this, uri);
                break;
            case "file":
                decoder = new ImageFileDecoder(uri);
                break;
            case "content":
                decoder = new ImageContentDecoder(this, uri);
                break;
            default:
                break;
        }

        if (decoder == null) {
            return null;
        }

        // 设置位图解码选项
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 计算最大位图尺寸并设置采样率，避免OOM
        int maxBitmapSize = BitmapLoadUtils.calculateMaxBitmapSize(getApplicationContext());
        options.inSampleSize = BitmapLoadUtils.calculateInSampleSize(options, maxBitmapSize, maxBitmapSize);

        // 实际解码位图
        options.inJustDecodeBounds = false;

        return decoder.decode(options);
    }

    /**
     * 处理添加文本的操作
     * 将指定的文本对象添加到图像上作为贴纸文本
     * 
     * @param text 要添加的文本对象，包含文本内容、颜色、字体等信息
     */
    @Override
    public void onText(ImageText text) {
        mImageViewCustom.addStickerText(text);
    }

    /**
     * 处理模式切换点击事件
     * 如果当前模式与点击的模式相同，则切换到NONE模式
     * 更新图像视图的模式，并刷新UI显示
     * 当切换到裁剪模式时，显示裁剪操作界面
     * 
     * @param mode 被点击的图像编辑模式
     */
    @Override
    public void onModeClick(ImageMode mode) {
        ImageMode cm = mImageViewCustom.getMode();
        // 如果点击当前激活的模式，则切换到无操作模式
        if (cm == mode) {
            mode = ImageMode.NONE;
        }
        // 设置新的编辑模式
        mImageViewCustom.setMode(mode);
        // 更新模式UI显示
        updateModeUi();

        // 如果是裁剪模式，显示裁剪操作界面
        if (mode == ImageMode.CLIP) {
            setOpDisplay(OP_CLIP);
        }
    }

    /**
     * 处理撤销操作点击事件
     * 根据当前的编辑模式执行相应的撤销操作
     * - 涂鸦模式：撤销上一步涂鸦操作
     * - 马赛克模式：撤销上一步马赛克操作
     */
    @Override
    public void onUndoClick() {
        ImageMode mode = mImageViewCustom.getMode();
        if (mode == ImageMode.DOODLE) {
            // 撤销涂鸦操作
            mImageViewCustom.undoDoodle();
        } else if (mode == ImageMode.MOSAIC) {
            // 撤销马赛克操作
            mImageViewCustom.undoMosaic();
        }
    }

    /**
     * 处理取消操作点击事件
     * 直接结束当前活动，不保存任何更改
     */
    @Override
    public void onCancelClick() {
        finish();
    }

    /**
     * 处理完成操作点击事件
     * 尝试将编辑后的图像保存到指定路径
     * 如果保存成功，返回RESULT_OK并传递图像的宽度和高度；否则返回RESULT_CANCELED
     * 无论保存成功与否，最终都会结束当前活动
     */
    @Override
    public void onDoneClick() {
        String path = getIntent().getStringExtra(EXTRA_IMAGE_SAVE_PATH);
        if (!TextUtils.isEmpty(path)) {
            // 获取编辑后的位图
            Bitmap bitmap = mImageViewCustom.saveBitmap();
            if (bitmap != null) {
                FileOutputStream fileOutputStream = null;
                try {
                    // 打开文件输出流
                    fileOutputStream = new FileOutputStream(path);
                    // 以JPEG格式保存位图，质量为100
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                } catch (FileNotFoundException e) {
                    // 记录文件未找到异常
                    Log.e(TAG, "onDoneClick" + e.getMessage());
                } finally {
                    // 确保关闭文件输出流
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            // 记录IO异常
                            Log.e(TAG, "onDoneClick" + e.getMessage());
                        }
                    }
                }
                // 创建返回意图，设置结果和图像尺寸信息
                Intent intent = new Intent();
                intent.putExtra(EXTRA_WIDTH, bitmap.getWidth());
                intent.putExtra(EXTRA_HEIGHT, bitmap.getHeight());
                setResult(RESULT_OK, intent);
                finish();
                return;
            }
        }
        // 如果保存路径无效或保存失败，设置结果为取消
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * 处理取消裁剪操作点击事件
     * 取消当前的裁剪操作，并根据当前模式更新操作界面显示
     */
    @Override
    public void onCancelClipClick() {
        // 取消裁剪操作
        mImageViewCustom.cancelClip();
        // 根据当前模式设置操作界面显示
        setOpDisplay(mImageViewCustom.getMode() == ImageMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    /**
     * 处理完成裁剪操作点击事件
     * 执行裁剪操作，并根据当前模式更新操作界面显示
     */
    @Override
    public void onDoneClipClick() {
        // 执行裁剪操作
        mImageViewCustom.doClip();
        // 根据当前模式设置操作界面显示
        setOpDisplay(mImageViewCustom.getMode() == ImageMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    /**
     * 处理重置裁剪区域点击事件
     * 重置当前的裁剪区域到默认状态
     */
    @Override
    public void onResetClipClick() {
        // 重置裁剪区域
        mImageViewCustom.resetClip();
    }

    /**
     * 处理旋转裁剪区域点击事件
     * 将裁剪区域按一定角度旋转
     */
    @Override
    public void onRotateClipClick() {
        // 旋转裁剪区域
        mImageViewCustom.doRotate();
    }

    /**
     * 处理颜色选择变化事件
     * 如果当前不是涂鸦模式，则自动切换到涂鸦模式
     * 设置画笔颜色为选中的颜色
     * 
     * @param checkedColor 选中的颜色值
     */
    @Override
    public void onColorChanged(int checkedColor) {
        // 如果当前不是涂鸦模式，切换到涂鸦模式
        if (mImageViewCustom.getMode() != ImageMode.DOODLE) {
            onModeClick(ImageMode.DOODLE);
        }
        // 设置画笔颜色
        mImageViewCustom.setPenColor(checkedColor);
    }
}
