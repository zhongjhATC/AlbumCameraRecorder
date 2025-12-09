package com.zhongjh.demo.configuration;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.zhongjh.common.engine.ImageEngine;
import com.zhongjh.common.utils.ActivityUtils;
import com.zhongjh.demo.R;

import org.jetbrains.annotations.NotNull;


/**
 * {@link ImageEngine} implementation using Glide.
 *
 * @author zhongjh
 */
public class Glide4Engine implements ImageEngine {

    @Override
    public void loadThumbnail(@NotNull Context context, int resize, @NotNull Drawable placeholder, @NotNull ImageView imageView, @NotNull String path) {
        Glide.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(path)
                .apply(new RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Bitmap> target, boolean isFirstResource) {
                        Log.e("Glide", "图片加载失败: $imageUrl " + path, e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Bitmap resource, @NonNull Object model, Target<Bitmap> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imageView);
    }

    @Override
    public void loadImage(@NotNull Context context, int resizeX, int resizeY, @NotNull ImageView imageView, @NotNull Uri uri) {
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions()
                        .override(resizeX, resizeY)
                        .error(R.drawable.ic_failed)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public void loadUrlImage(@NotNull Context context, int resizeX, int resizeY, @NotNull ImageView imageView, @NotNull String url) {
        Glide.with(context).load(url).override(resizeX, resizeY).into(imageView);
    }

    @Override
    public void loadUriImage(@NotNull Context context, @NotNull ImageView imageView, @NonNull String path) {
        Glide.with(context)
                .load(path)
                .apply(new RequestOptions()
                        .error(R.drawable.ic_failed)
                        .fitCenter()
                        // 仅传一个参数：让 Glide 按 View 尺寸适配，且不限制最大尺寸
                        .override(Target.SIZE_ORIGINAL)
                        // 禁用 RGB_565 降质格式
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        // 禁用硬件位图（部分设备导致模糊）
                        .disallowHardwareConfig()
                        // 保证图片尺寸 ≥ View 尺寸（避免缩放模糊）
                        .downsample(DownsampleStrategy.AT_LEAST)
                        // 缓存高清缩放图，避免重复压缩
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                .into(imageView);
    }

    @Override
    public void loadDrawableImage(@NotNull Context context, @NotNull ImageView imageView, int resourceId) {
        Glide.with(context)
                .load(resourceId)
                .apply(new RequestOptions()
                        // TODO 看看这个怎么解决
                        .error(R.drawable.ic_failed)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public void loadGifImage(@NotNull Context context, int resizeX, int resizeY, @NotNull ImageView imageView, @NotNull Uri uri) {
        Glide.with(context)
                .asGif()
                .load(uri)
                .apply(new RequestOptions()
                        .override(resizeX, resizeY)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public boolean supportAnimatedGif() {
        return true;
    }


    @Override
    public void pauseRequests(@NonNull Context context) {
        if (!ActivityUtils.INSTANCE.assertValidRequest(context.getApplicationContext())) {
            return;
        }
        Glide.with(context.getApplicationContext()).pauseRequests();
        Log.d("Glide4Engine", "pauseRequests");
    }

    @Override
    public void resumeRequests(@NonNull Context context) {
        if (!ActivityUtils.INSTANCE.assertValidRequest(context.getApplicationContext())) {
            return;
        }
        Glide.with(context.getApplicationContext()).resumeRequests();
        Log.d("Glide4Engine", "resumeRequests");
    }
}
