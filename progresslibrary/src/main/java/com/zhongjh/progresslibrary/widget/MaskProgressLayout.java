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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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

    private final Context mContext;
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
     * 删除颜色
     */
    private int audioDeleteColor;
    /**
     * 播放按钮的颜色
     */
    private int audioPlayColor;

    /**
     * 点击事件(这里只针对音频)
     */
    private MaskProgressLayoutListener listener;

    /**
     * @return 最多显示多少个图片/视频/语音
     */
    public int getMaxMediaCount() {
        return mViewHolder.alfMedia.getMaxMediaCount();
    }

    /**
     * 设置最多显示多少个图片/视频/语音
     */
    public void setMaxMediaCount(Integer maxMediaCount, Integer maxImageSelectable, Integer maxVideoSelectable, Integer maxAudioSelectable) {
        // 计算最终呈现的总数，这个总数决定是否还能点击添加
        if (maxMediaCount != null) {
            mViewHolder.alfMedia.setMaxMediaCount(maxMediaCount);
        } else {
            mViewHolder.alfMedia.setMaxMediaCount(maxImageSelectable + maxVideoSelectable + maxAudioSelectable);
        }
    }

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
        mContext = context;
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
        // 一行多少列
        int columnNumber = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayout_columnNumber, 4);
        // 列与列之间多少间隔px单位
        int columnSpace = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayout_columnSpace, 10);
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
        audioDeleteColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_audioDeleteColor, colorPrimary);
        // 音频 文件的进度条颜色
        audioProgressColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_audioProgressColor, colorPrimary);
        // 音频 播放按钮的颜色
        audioPlayColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_audioPlayColor, colorPrimary);
        // endregion 音频

        // region 遮罩层相关属性

        int maskingColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_maskingColor, colorPrimary);
        int maskingTextSize = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayout_maskingTextSize, 12);

        int maskingTextColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayout_maskingTextColor, ContextCompat.getColor(getContext(), R.color.thumbnail_placeholder));
        String maskingTextContent = maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayout_maskingTextContent);

        // endregion 遮罩层相关属性

        if (imageEngineStr == null) {
            // 必须定义image_engine属性，指定某个显示图片类
            throw new RuntimeException("The image_engine attribute must be defined to specify a class for displaying images");
        } else {
            Class<?> imageEngineClass;//完整类名
            try {
                imageEngineClass = Class.forName(imageEngineStr);
                mImageEngine = (ImageEngine) imageEngineClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mImageEngine == null) {
                // image_engine找不到相关类
                throw new RuntimeException("Image_engine could not find the related class");
            }
        }

        SaveStrategy saveStrategy = new SaveStrategy(true, authority, "");
        mMediaStoreCompat.setSaveStrategy(saveStrategy);

        if (drawable == null) {
            drawable = ContextCompat.getDrawable(getContext(), R.color.thumbnail_placeholder);
        }
        // 初始化九宫格的控件
        mViewHolder.alfMedia.initConfig(this, mImageEngine, isOperation, drawable,
                maxCount, maskingColor, maskingTextSize, maskingTextColor, maskingTextContent,
                imageDeleteColor, imageDeleteDrawable, imageAddDrawable,
                columnNumber, columnSpace);


        maskProgressLayoutStyle.recycle();
        typedArray.recycle();
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
        addAudioData(multiMediaView, filePath, length);

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

                if (this.audioList == null) {
                    this.audioList = new ArrayList<>();
                }
                audioList.add(multiMediaView);

                PlayProgressView playProgressView = newPlayProgressView(multiMediaView);
                // 显示音频播放控件，当点击播放的时候，才正式下载并且进行播放
                playProgressView.mViewHolder.playView.setVisibility(View.VISIBLE);
                // 隐藏上传进度
                playProgressView.mViewHolder.groupRecorderProgress.setVisibility(View.GONE);
                isShowRemoveRecorder();
                RecordingItem recordingItem = new RecordingItem();
                recordingItem.setUrl(audioUrl);
                recordingItem.setLength(duration);
                playProgressView.setData(recordingItem, audioProgressColor);
                //记得释放资源
                mediaPlayer.release();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAudioCover(View view, String file) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(file);

        // ms,时长
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.AUDIO);
        multiMediaView.setPath(file);

        // 显示音频播放控件，当点击播放的时候，才正式下载并且进行播放
        view.setVisibility(View.VISIBLE);
        isShowRemoveRecorder();
        RecordingItem recordingItem = new RecordingItem();
        recordingItem.setFilePath(file);
        recordingItem.setLength(Integer.parseInt(duration));
        ((PlayView) view).setData(recordingItem, audioProgressColor);
    }

    @Override
    public void reset() {
        // 清空数据
        mViewHolder.alfMedia.imageList.clear();
        mViewHolder.alfMedia.videoList.clear();
        this.audioList.clear();
        // 清空view
        mViewHolder.llContent.removeAllViews();
        // 从倒数第二个删除，最后一个是ADD
        for (int i = mViewHolder.alfMedia.getChildCount() - 2; i >= 0; i--) {
            mViewHolder.alfMedia.removeViewAt(i);
        }
        mViewHolder.alfMedia.checkLastImages();
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
    public void onAudioClick(View view) {
        ((PlayView) view).mViewHolder.imgPlay.performClick();
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
        // 添加音频后重置所有当前播放中的音频
        for (int i = 0; i < mViewHolder.llContent.getChildCount(); i++) {
            PlayProgressView item = (PlayProgressView) mViewHolder.llContent.getChildAt(i);
            item.setOperation(isOperation);
        }
        isShowRemoveRecorder();
    }

    @Override
    public void onDestroy() {
        mViewHolder.alfMedia.removeListener();

        for (int i = 0; i < mViewHolder.llContent.getChildCount(); i++) {
            PlayProgressView item = (PlayProgressView) mViewHolder.llContent.getChildAt(i);
            item.mViewHolder.playView.onDestroy();
            item.mViewHolder.playView.removeListener();
        }
        this.listener = null;
    }

    /**
     * 设置是否显示删除音频按钮
     */
    private void isShowRemoveRecorder() {
        for (int i = 0; i < mViewHolder.llContent.getChildCount(); i++) {
            PlayProgressView item = (PlayProgressView) mViewHolder.llContent.getChildAt(i);
            item.isShowRemoveRecorder();
        }
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
     * @param filePath       音频文件地址
     * @param length         音频文件长度
     */
    private void addAudioData(MultiMediaView multiMediaView, String filePath, int length) {
        if (this.audioList == null) {
            this.audioList = new ArrayList<>();
        }
        this.audioList.add(multiMediaView);
        if (audioList != null && audioList.size() > 0) {
            // 显示音频的进度条
            this.listener.onItemStartUploading(multiMediaView);
        }
        PlayProgressView playProgressView = newPlayProgressView(multiMediaView);
        // 初始化播放控件
        RecordingItem recordingItem = new RecordingItem();
        recordingItem.setFilePath(filePath);
        recordingItem.setLength(length);
        playProgressView.setData(recordingItem, audioProgressColor);
        // 添加音频后重置所有当前播放中的音频
        for (int i = 0; i < mViewHolder.llContent.getChildCount(); i++) {
            PlayProgressView item = (PlayProgressView) mViewHolder.llContent.getChildAt(i);
            item.reset();
        }
    }

    /**
     * 创建一个新的playProgressView
     *
     * @param multiMediaView 这是携带view的实体控件
     * @return playProgressView
     */
    private PlayProgressView newPlayProgressView(MultiMediaView multiMediaView) {
        PlayProgressView playProgressView = new PlayProgressView(mContext);
        playProgressView.setCallback(() -> {
            if (audioList.size() > 0) {
                // 需要判断，防止是网址状态未提供实体数据的
                listener.onItemClose(MaskProgressLayout.this, multiMediaView);
            }
            audioList.remove(multiMediaView);
            mViewHolder.alfMedia.checkLastImages();
        });
        playProgressView.initStyle(audioDeleteColor, audioProgressColor, audioPlayColor);
        multiMediaView.setPlayProgressView(playProgressView);
        playProgressView.setListener(listener);
        // 添加入view
        mViewHolder.llContent.addView(playProgressView);
        return playProgressView;
    }

    /**
     * 检测属性
     */
    private void isAuthority() {
        if (mMediaStoreCompat.getSaveStrategy() == null || mMediaStoreCompat.getSaveStrategy().authority == null) {
            // 必须定义authority属性，指定provider的authorities,用于提供给外部的file,否则Android7.0以上报错。也可以代码设置setAuthority
            throw new RuntimeException("You must define the authority attribute, which specifies the provider's authorities, to serve to external files. Otherwise, Android7.0 will report an error.You can also set setAuthority in code");
        }
    }

    public static class ViewHolder {
        View rootView;
        AutoLineFeedLayout alfMedia;
        LinearLayout llContent;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.alfMedia = rootView.findViewById(R.id.alfMedia);
            this.llContent = rootView.findViewById(R.id.llContent);
        }
    }
}
