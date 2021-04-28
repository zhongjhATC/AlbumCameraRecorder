package com.zhongjh.progresslibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.api.MaskProgressApi;
import com.zhongjh.progresslibrary.engine.ImageEngine;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.entity.RecordingItem;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gaode.zhongjh.com.common.entity.SaveStrategy;
import gaode.zhongjh.com.common.enums.MultimediaTypes;
import gaode.zhongjh.com.common.utils.MediaStoreCompat;

/**
 * 这是返回（图片、视频、录音）等文件后，显示的Layout
 *
 * @author zhongjh
 * @date 2018/10/17
 * https://www.jianshu.com/p/191c41f63dc7
 */
public class MaskProgressLayout extends FrameLayout implements MaskProgressApi {

    /**
     * 文件配置路径
     */
    private MediaStoreCompat mMediaStoreCompat;
    /**
     * 是否允许操作
     */
    private boolean isOperation = true;

    /**
     * 控件集合
     */
    public ViewHolder mViewHolder;
    /**
     * 图片加载方式
     */
    private ImageEngine mImageEngine;

    /**
     * 音频数据
     */
    public ArrayList<MultiMediaView> audioList = new ArrayList<>();
    /**
     * 音频 文件的进度条颜色
     */
    private int audioProgressColor;
    /**
     * 点击事件(这里只针对音频)
     */
    private MaskProgressLayoutListener listener;

    public void setMaskProgressLayoutListener(MaskProgressLayoutListener listener) {
        mViewHolder.alfMedia.setListener(listener);
        mViewHolder.playView.setListener(listener);
        this.listener = listener;
    }

    public MaskProgressLayout(@NonNull Context context) {
        this(context, null);
    }

    public MaskProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    /**
     * 初始化view
     */
    private void initView(AttributeSet attrs) {
        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false);

        mMediaStoreCompat = new MediaStoreCompat(getContext());
        mViewHolder = new ViewHolder(View.inflate(getContext(), R.layout.layout_mask_progress, this));

        // 获取系统颜色
        int defaultColor = 0xFF000000;
        int[] attrsArray = {R.attr.colorPrimary, R.attr.colorPrimaryDark, R.attr.colorAccent};
        TypedArray typedArray = getContext().obtainStyledAttributes(attrsArray);
        int colorPrimary = typedArray.getColor(0, defaultColor);

        // 获取自定义属性。
        TypedArray maskProgressLayoutStyle = getContext().obtainStyledAttributes(attrs, R.styleable.MaskProgressLayout);
        // 是否允许操作
        isOperation = maskProgressLayoutStyle.getBoolean(R.styleable.MaskProgressLayout_isOperation, true);
        // 获取默认图片
        Drawable drawable = maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayout_album_thumbnail_placeholder);
        // 获取添加图片
        Drawable imageAddDrawable = maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayout_imageAddDrawable);
        // 获取显示图片的类
        String imageEngineStr = maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayout_imageEngine);
        // provider的authorities,用于提供给外部的file
        String authority = maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayout_authority);
        // 获取最多显示多少个方框
        int maxCount = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayout_maxCount, 5);
        int imageDeleteColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_imageDeleteColor, colorPrimary);
        Drawable imageDeleteDrawable = maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayout_imageDeleteDrawable);

        // region 音频
        // 音频，删除按钮的颜色
        int audioDeleteColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_audioDeleteColor, colorPrimary);
        // 音频 文件的进度条颜色
        audioProgressColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_audioProgressColor, colorPrimary);
        // 音频 播放按钮的颜色
        int audioPlayColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_audioPlayColor, colorPrimary);
        // endregion 音频

        // region 遮罩层相关属性

        int maskingColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_maskingColor, colorPrimary);
        int maskingTextSize = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayout_maskingTextSize, 12);

        int maskingTextColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_maskingTextColor, ContextCompat.getColor(getContext(), R.color.thumbnail_placeholder));
        String maskingTextContent = maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayout_maskingTextContent);

        // endregion 遮罩层相关属性

        if (imageEngineStr == null) {
            throw new RuntimeException("必须定义image_engine属性，指定某个显示图片类");
        } else {
            Class<?> imageEngineClass;//完整类名
            try {
                imageEngineClass = Class.forName(imageEngineStr);
                mImageEngine = (ImageEngine) imageEngineClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mImageEngine == null) {
                throw new RuntimeException("image_engine找不到相关类");
            }
        }

        SaveStrategy saveStrategy = new SaveStrategy(true, authority, "");
        mMediaStoreCompat.setSaveStrategy(saveStrategy);

        if (drawable == null) {
            drawable = ContextCompat.getDrawable(getContext(), R.color.thumbnail_placeholder);
        }
        // 初始化九宫格的控件
        mViewHolder.alfMedia.initConfig(this, mImageEngine, isOperation, drawable, maxCount, maskingColor, maskingTextSize, maskingTextColor, maskingTextContent, imageDeleteColor, imageDeleteDrawable, imageAddDrawable);
        // 设置上传音频等属性
        mViewHolder.imgRemoveRecorder.setColorFilter(audioDeleteColor);
        isShowRemoveRecorder();
        mViewHolder.numberProgressBar.setProgressTextColor(audioProgressColor);
        mViewHolder.numberProgressBar.setReachedBarColor(audioProgressColor);
        mViewHolder.tvRecorderTip.setTextColor(audioProgressColor);

        // 设置播放控件里面的播放按钮的颜色
        mViewHolder.playView.mViewHolder.imgPlay.setColorFilter(audioPlayColor);
        mViewHolder.playView.mViewHolder.tvCurrentProgress.setTextColor(audioProgressColor);
        mViewHolder.playView.mViewHolder.tvTotalProgress.setTextColor(audioProgressColor);

        maskProgressLayoutStyle.recycle();
        typedArray.recycle();

        initListener();
    }

    @Override
    public void setAuthority(String authority) {
        SaveStrategy saveStrategy = new SaveStrategy(true, authority, "");
        mMediaStoreCompat.setSaveStrategy(saveStrategy);
    }

    @Override
    public void addUrisStartUpload(List<Uri> uris) {
        isAuthority();
        ArrayList<MultiMediaView> multiMediaViews = new ArrayList<>();
        for (Uri uri : uris) {
            MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.PICTURE);
            multiMediaView.setUri(uri);
            multiMediaViews.add(multiMediaView);
        }
        mViewHolder.alfMedia.addImageData(multiMediaViews);
    }

    @Override
    public void addImagesStartUpload(List<String> imagePaths) {
        isAuthority();
        ArrayList<MultiMediaView> multiMediaViews = new ArrayList<>();
        for (String string : imagePaths) {
            MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.PICTURE);
            multiMediaView.setPath(string);
            multiMediaView.setUri(mMediaStoreCompat.getUri(string));
            multiMediaViews.add(multiMediaView);
        }
        mViewHolder.alfMedia.addImageData(multiMediaViews);
    }

    @Override
    public void addImageUrls(List<String> imagesUrls) {
        ArrayList<MultiMediaView> multiMediaViews = new ArrayList<>();
        for (String string : imagesUrls) {
            MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.PICTURE);
            multiMediaView.setUrl(string);
            multiMediaViews.add(multiMediaView);
        }
        mViewHolder.alfMedia.addImageData(multiMediaViews);
    }

    @Override
    public void addVideoStartUpload(List<Uri> videoUris) {
        addVideo(videoUris, false, true);
    }

    @Override
    public void addVideoCover(List<String> videoPath) {
        List<Uri> uris = new ArrayList<>();
        for (String item : videoPath) {
            uris.add(mMediaStoreCompat.getUri(item));
        }
        addVideo(uris, true, false);
    }

    @Override
    public void addVideoUrl(String videoUrl) {
        ArrayList<MultiMediaView> multiMediaViews = new ArrayList<>();
        MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.VIDEO);
        multiMediaView.setUrl(videoUrl);
        multiMediaViews.add(multiMediaView);
        mViewHolder.alfMedia.addVideoData(multiMediaViews, false, false);
    }

    @Override
    public void addAudioStartUpload(String filePath, int length) {
        isAuthority();
        MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.AUDIO);
        multiMediaView.setPath(filePath);
        multiMediaView.setUri(mMediaStoreCompat.getUri(filePath));
        multiMediaView.setViewHolder(this);
        addAudioData(multiMediaView);

        // 显示上传中的音频
        mViewHolder.groupRecorderProgress.setVisibility(View.VISIBLE);
        mViewHolder.playView.setVisibility(View.GONE);
        isShowRemoveRecorder();

        // 初始化播放控件
        RecordingItem recordingItem = new RecordingItem();
        recordingItem.setFilePath(filePath);
        recordingItem.setLength(length);
        mViewHolder.playView.setData(recordingItem, audioProgressColor);

        // 检测添加多媒体上限
        mViewHolder.alfMedia.checkLastImages();
    }

    @Override
    public void addAudioUrl(String audioUrl) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            if (0 != duration) {
                MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.AUDIO);
                multiMediaView.setUrl(audioUrl);
                multiMediaView.setViewHolder(this);

                if (this.audioList == null) {
                    this.audioList = new ArrayList<>();
                }
                audioList.add(multiMediaView);

                // 显示音频播放控件，当点击播放的时候，才正式下载并且进行播放
                mViewHolder.playView.setVisibility(View.VISIBLE);
                isShowRemoveRecorder();
                RecordingItem recordingItem = new RecordingItem();
                recordingItem.setUrl(audioUrl);
                recordingItem.setLength(duration);
                mViewHolder.playView.setData(recordingItem, audioProgressColor);
                //记得释放资源
                mediaPlayer.release();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addAudioCover(String file) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(file);

        // ms,时长
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);


        MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.AUDIO);
        multiMediaView.setPath(file);
        multiMediaView.setViewHolder(this);

        // 显示音频播放控件，当点击播放的时候，才正式下载并且进行播放
        mViewHolder.playView.setVisibility(View.VISIBLE);
        isShowRemoveRecorder();
        RecordingItem recordingItem = new RecordingItem();
        recordingItem.setFilePath(file);
        recordingItem.setLength(Integer.parseInt(duration));
        mViewHolder.playView.setData(recordingItem, audioProgressColor);
    }

    @Override
    public ArrayList<MultiMediaView> getImages() {
        return mViewHolder.alfMedia.imageList;
    }

    @Override
    public ArrayList<MultiMediaView> getVideos() {
        return mViewHolder.alfMedia.videoList;
    }

    @Override
    public ArrayList<MultiMediaView> getAudios() {
        return this.audioList;
    }

    @Override
    public void onAudioClick() {
        mViewHolder.playView.mViewHolder.imgPlay.performClick();
    }

    @Override
    public void onVideoClick() {
        mViewHolder.alfMedia.getChildAt(0).performClick();
    }

    @Override
    public void onRemoveItemImage(int position) {
        mViewHolder.alfMedia.onRemoveItemImage(position);
    }

    @Override
    public void setOperation(boolean isOperation) {
        this.isOperation = isOperation;
        mViewHolder.alfMedia.setOperation(isOperation);
        isShowRemoveRecorder();
    }

    @Override
    public void destroy() {
        mViewHolder.playView.deStory();
    }

    /**
     * 音频上传完成后
     */
    public void audioUploadCompleted() {
        // 显示完成后的音频
        mViewHolder.groupRecorderProgress.setVisibility(View.GONE);
        mViewHolder.playView.setVisibility(View.VISIBLE);
        isShowRemoveRecorder();
    }

    /**
     * 初始化所有事件
     */
    private void initListener() {
        // 音频删除事件
        this.mViewHolder.imgRemoveRecorder.setOnClickListener(v -> {
            if (audioList.size() > 0)
            // 需要判断，防止是网址状态未提供实体数据的
            {
                listener.onItemClose(MaskProgressLayout.this, audioList.get(0));
            }
            // 隐藏音频相关控件
            mViewHolder.groupRecorderProgress.setVisibility(View.GONE);
            mViewHolder.playView.setVisibility(View.GONE);
            audioList.clear();
            mViewHolder.imgRemoveRecorder.setVisibility(View.GONE);
            mViewHolder.alfMedia.checkLastImages();
            isShowRemoveRecorder();
            mViewHolder.playView.reset();
        });
    }

    /**
     * 设置视频地址
     *
     * @param videoUris   视频列表
     * @param icClean     是否清除
     * @param isUploading 是否触发上传事件
     */
    private void addVideo(List<Uri> videoUris, boolean icClean, boolean isUploading) {
        isAuthority();
        ArrayList<MultiMediaView> multiMediaViews = new ArrayList<>();
        for (Uri uri : videoUris) {
            MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.VIDEO);
            multiMediaView.setUri(uri);
            multiMediaViews.add(multiMediaView);
        }
        mViewHolder.alfMedia.addVideoData(multiMediaViews, icClean, isUploading);
    }

    /**
     * 添加音频数据
     *
     * @param multiMediaView 数据
     */
    private void addAudioData(MultiMediaView multiMediaView) {
        if (this.audioList == null) {
            this.audioList = new ArrayList<>();
        }
        this.audioList.add(multiMediaView);
        if (audioList != null && audioList.size() > 0) {
            // 显示音频的进度条
            this.listener.onItemStartUploading(multiMediaView);
        }
    }

    /**
     * 设置是否显示删除音频按钮
     */
    private void isShowRemoveRecorder() {
        if (isOperation) {
            // 如果是可操作的，就判断是否有音频数据
            if (this.mViewHolder.playView.getVisibility() == View.VISIBLE || this.mViewHolder.groupRecorderProgress.getVisibility() == View.VISIBLE) {
                mViewHolder.imgRemoveRecorder.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.imgRemoveRecorder.setVisibility(View.GONE);
            }
        } else {
            mViewHolder.imgRemoveRecorder.setVisibility(View.GONE);
        }
    }

    /**
     * 检测属性
     */
    private void isAuthority() {
        if (mMediaStoreCompat.getSaveStrategy() == null || mMediaStoreCompat.getSaveStrategy().authority == null) {
            throw new RuntimeException("必须定义authority属性，指定provider的authorities,用于提供给外部的file,否则Android7.0以上报错。也可以代码设置setAuthority");
        }
    }

    public static class ViewHolder {
        View rootView;
        AutoLineFeedLayout alfMedia;
        public NumberProgressBar numberProgressBar;
        public ImageView imgRemoveRecorder;
        public Group groupRecorderProgress;
        public PlayView playView;
        public TextView tvRecorderTip;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.alfMedia = rootView.findViewById(R.id.alfMedia);
            this.numberProgressBar = rootView.findViewById(R.id.numberProgressBar);
            this.imgRemoveRecorder = rootView.findViewById(R.id.imgRemoveRecorder);
            this.playView = rootView.findViewById(R.id.playView);
            this.groupRecorderProgress = rootView.findViewById(R.id.groupRecorderProgress);
            this.tvRecorderTip = rootView.findViewById(R.id.tvRecorderTip);
        }
    }
}
