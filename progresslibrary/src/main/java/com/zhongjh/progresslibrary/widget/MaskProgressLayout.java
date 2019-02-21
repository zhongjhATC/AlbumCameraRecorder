package com.zhongjh.progresslibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.engine.ImageEngine;
import com.zhongjh.progresslibrary.entity.MultiMedia;
import com.zhongjh.progresslibrary.entity.RecordingItem;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 这是返回（图片、视频、录音）等文件后，显示的Layout
 * Created by zhongjh on 2018/10/17.
 */
public class MaskProgressLayout extends FrameLayout {

    public ViewHolder mViewHolder;          // 控件集合
    private ImageEngine mImageEngine;       // 图片加载方式

    public List<MultiMedia> audioList = new ArrayList<>();     // 音频数据
    private int audioProgressColor;                 // 音频 文件的进度条颜色
    private MaskProgressLayoutListener listener;   // 点击事件(这里只针对音频)

    public void setMaskProgressLayoutListener(MaskProgressLayoutListener listener) {
        mViewHolder.alfMedia.setListener(listener);
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

        mViewHolder = new ViewHolder(View.inflate(getContext(), R.layout.layout_mask_progress, this));

        // 获取系统颜色
        int defaultColor = 0xFF000000;
        int[] attrsArray = {R.attr.colorPrimary, R.attr.colorPrimaryDark, R.attr.colorAccent};
        TypedArray typedArray = getContext().obtainStyledAttributes(attrsArray);
        int colorPrimary = typedArray.getColor(0, defaultColor);

        // 获取自定义属性。
        TypedArray maskProgressLayoutStyle = getContext().obtainStyledAttributes(attrs, R.styleable.MaskProgressLayoutStyle);
        // 获取默认图片
        Drawable drawable = maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayoutStyle_album_thumbnail_placeholder);
        // 获取添加图片
        Drawable imageAddDrawable = maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayoutStyle_imageAddDrawable);
        // 获取显示图片的类
        String imageEngineStr = maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayoutStyle_imageEngine);
        // 获取最多显示多少个方框
        int imageCount = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayoutStyle_maxCount, 5);
        int imageDeleteColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayoutStyle_imageDeleteColor, colorPrimary);
        Drawable imageDeleteDrawable = maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayoutStyle_imageDeleteDrawable);

        // region 音频
        // 音频，删除按钮的颜色
        int audioDeleteColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayoutStyle_audioDeleteColor, colorPrimary);
        // 音频 文件的进度条颜色
        audioProgressColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayoutStyle_audioProgressColor, colorPrimary);
        // 音频 播放按钮的颜色
        int audioPlayColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayoutStyle_audioPlayColor, colorPrimary);
        // endregion 音频

        // region 遮罩层相关属性

        int maskingColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayoutStyle_maskingColor, colorPrimary);
        int maskingTextSize = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayoutStyle_maskingTextSize, 12);
        int maskingTextColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayoutStyle_maskingTextColor, getContext().getResources().getColor(R.color.thumbnail_placeholder));
        String maskingTextContent = maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayoutStyle_maskingTextContent);

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
        if (drawable == null) {
            drawable = getResources().getDrawable(R.color.thumbnail_placeholder);
        }
        // 初始化九宫格的控件
        mViewHolder.alfMedia.initConfig(this, mImageEngine, drawable, imageCount, maskingColor, maskingTextSize, maskingTextColor, maskingTextContent, imageDeleteColor, imageDeleteDrawable, imageAddDrawable);
        // 设置上传音频等属性
        mViewHolder.imgRemoveRecorder.setColorFilter(audioDeleteColor);
        mViewHolder.numberProgressBar.setProgressTextColor(audioProgressColor);
        mViewHolder.numberProgressBar.setReachedBarColor(audioProgressColor);
        mViewHolder.tvRecorderTip.setTextColor(audioProgressColor);

        // 设置播放控件里面的播放按钮的颜色
        mViewHolder.playView.mViewHolder.imgPlay.setColorFilter(audioPlayColor);
        mViewHolder.playView.mViewHolder.tvCurrentProgress.setTextColor(audioProgressColor);

        maskProgressLayoutStyle.recycle();
        typedArray.recycle();

        initListener();
    }

    /**
     * 设置图片同时更新表格
     *
     * @param imagePaths 图片数据源
     *                   // @param isUpload 是否上传，如果为true,则显示过渡动画，并且
     */
    public void addImages(List<String> imagePaths) {
        ArrayList<MultiMedia> multiMedias = new ArrayList<>();
        for (String string : imagePaths) {
            MultiMedia multiMedia = new MultiMedia(string, 0);
            multiMedias.add(multiMedia);
        }
        mViewHolder.alfMedia.addImageData(multiMedias);
    }

    /**
     * 设置视频地址
     */
    public void addVideo(List<String> videoPath) {
        ArrayList<MultiMedia> multiMedias = new ArrayList<>();
        for (String string : videoPath) {
            MultiMedia multiMedia = new MultiMedia(string, 1);
            multiMedias.add(multiMedia);
        }
        mViewHolder.alfMedia.addVideoData(multiMedias);

    }

    /**
     * 设置音频数据
     *
     * @param filePath 音频文件地址
     * @
     */
    public void addAudio(String filePath, int length) {
        MultiMedia multiMedia = new MultiMedia(filePath, 2);
        multiMedia.setViewHolder(this);
        addAudioData(multiMedia);

        // 显示上传中的音频
        mViewHolder.imgRemoveRecorder.setVisibility(View.VISIBLE);
        mViewHolder.groupRecorderProgress.setVisibility(View.VISIBLE);
        mViewHolder.playView.setVisibility(View.GONE);

        // 初始化播放控件
        RecordingItem recordingItem = new RecordingItem();
        recordingItem.setFilePath(filePath);
        recordingItem.setLength(length);
        mViewHolder.playView.setData(recordingItem, audioProgressColor);
    }

    /**
     * 添加音频网址数据
     *
     * @param audioUrl 音频网址
     */
    public void addAudioUrl(String audioUrl) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            if (0 != duration) {
                MultiMedia multiMedia = new MultiMedia(2);
                multiMedia.setUrl(audioUrl);
                multiMedia.setViewHolder(this);

                // 显示音频播放控件，当点击播放的时候，才正式下载并且进行播放
                mViewHolder.playView.setVisibility(View.VISIBLE);
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

    /**
     * 添加音频实际的文件
     * @param file 文件路径
     */
    public void addVideoFile(String file){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(file);
//        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//        String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
//        String mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
//        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION); // ms,时长
//        String bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE); // bit/s api >= 14
//        String date = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);



        MultiMedia multiMedia = new MultiMedia(2);
        multiMedia.setPath(file);
        multiMedia.setViewHolder(this);

        // 显示音频播放控件，当点击播放的时候，才正式下载并且进行播放
        mViewHolder.playView.setVisibility(View.VISIBLE);
        RecordingItem recordingItem = new RecordingItem();
        recordingItem.setFilePath(file);
        recordingItem.setLength(Integer.valueOf(duration));
        mViewHolder.playView.setData(recordingItem, audioProgressColor);
    }

    /**
     * 添加音频数据
     *
     * @param multiMedia 数据
     */
    public void addAudioData(MultiMedia multiMedia) {
        if (this.audioList == null) {
            this.audioList = new ArrayList<>();
        }
        this.audioList.add(multiMedia);
        if (audioList != null && audioList.size() > 0) {
            // 显示音频的进度条
            this.listener.onItemStartUploading(multiMedia);
        }
    }

    /**
     * 音频上传完成后
     */
    public void audioUploadCompleted() {
        // 显示完成后的音频
        mViewHolder.groupRecorderProgress.setVisibility(View.GONE);
        mViewHolder.playView.setVisibility(View.VISIBLE);
    }

    /**
     * @return 返回当前图片数据
     */
    public List<MultiMedia> getImages() {
        return mViewHolder.alfMedia.imageList;
    }

    /**
     * @return 返回当前视频数据
     */
    public List<MultiMedia> getVideos() {
        return mViewHolder.alfMedia.videoList;
    }


    /**
     * 初始化所有事件
     */
    private void initListener() {
        // 音频删除事件
        this.mViewHolder.imgRemoveRecorder.setOnClickListener(v -> {
            listener.onItemClose(MaskProgressLayout.this, audioList.get(0));
            // 隐藏音频相关控件
            mViewHolder.groupRecorderProgress.setVisibility(View.GONE);
            mViewHolder.playView.setVisibility(View.GONE);
            mViewHolder.imgRemoveRecorder.setVisibility(View.GONE);
            audioList.clear();
        });
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
