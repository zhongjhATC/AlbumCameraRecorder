package com.zhongjh.imageedit.core.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Assets资源图像解码器，继承自BaseImageDecoder
 * 专门用于从Android应用的assets文件夹中解码图像文件
 * 支持加载应用内置的图像资源，常用于加载编辑器内置的贴纸、滤镜示例图等
 * 在图像编辑器中负责处理应用打包时内置的图像资源
 * 
 * @author felix
 * @date 2017/12/26 下午2:57
 */
public class ImageAssetFileDecoder extends BaseImageDecoder {

    /**
     * 应用上下文对象，用于访问应用的assets资源
     */
    private final Context mContext;

    /**
     * 构造函数，初始化Assets资源图像解码器
     * 
     * @param context 应用上下文对象，用于访问assets资源
     * @param uri 图像文件在assets中的Uri路径
     */
    public ImageAssetFileDecoder(Context context, Uri uri) {
        super(uri);
        mContext = context;
    }

    /**
     * 解码assets文件夹中的图像文件，使用指定的配置选项
     * 实现了BaseImageDecoder中定义的抽象方法，专门用于从应用的assets目录解码图像资源
     * 该方法首先验证URI和路径的有效性，处理assets路径的特殊格式要求，然后尝试打开assets文件并解码
     * 
     * @param options 解码配置选项，可以控制解码过程中的各种参数，如inSampleSize、inPreferredConfig等
     *                通过这些配置可以优化内存使用和图像质量
     * @return 解码后的Bitmap对象，如果解码失败或文件不存在则返回null
     */
    @Override
    public Bitmap decode(BitmapFactory.Options options) {
        // 第一步：获取图像文件的Uri
        // 通过调用父类BaseImageDecoder的getUri()方法获取存储在解码器中的URI
        Uri uri = getUri();
        if (uri == null) {
            // 如果Uri为空，表示没有设置要解码的图像源，直接返回null
            return null;
        }

        // 第二步：获取文件路径
        // 注意：对于assets资源，URI路径应该指向assets目录下的文件
        String path = uri.getPath();
        if (TextUtils.isEmpty(path)) {
            // 如果路径为空，表示URI无法转换为有效的assets路径，直接返回null
            return null;
        }

        // 第三步：处理assets路径格式
        // 移除路径前的斜杠，因为assets路径不需要前导斜杠
        // 例如：将"/images/sticker.png"转换为"images/sticker.png"
        // 这是因为AssetManager的open方法要求路径不以斜杠开头
        path = path.substring(1);

        try {
            // 第四步：使用应用上下文打开assets中的文件输入流
            // AssetManager是Android提供的访问应用资源的标准方式
            InputStream iStream = mContext.getAssets().open(path);
            // 第五步：使用BitmapFactory从输入流中解码图像
            // BitmapFactory.decodeStream是从输入流解码图像的标准方法
            // 传入options参数可以控制解码过程，如采样率、图像格式等，有助于优化内存使用
            return BitmapFactory.decodeStream(iStream, null, options);
        } catch (IOException ignore) {
            // 如果发生IO异常（如文件不存在、路径错误或无法访问），忽略异常并返回null
            // 在实际应用中，可能需要根据具体需求记录异常日志或进行其他处理
        }

        // 解码失败，返回null
        return null;
    }
}
