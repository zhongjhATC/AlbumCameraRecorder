package com.zhongjh.progresslibrary.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.adapter.PhotoAdapter;
import com.zhongjh.progresslibrary.api.MaskProgressApi;
import com.zhongjh.progresslibrary.engine.ImageEngine;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.entity.RecordingItem;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.zhongjh.common.entity.SaveStrategy;
import com.zhongjh.common.enums.MultimediaTypes;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.ThreadUtils;

/**
 * 这是返回（图片、视频、录音）等文件后，显示的Layout
 *
 * @author zhongjh
 * @date 2018/10/17
 * https://www.jianshu.com/p/191c41f63dc7
 */
public class MaskProgressLayout extends FrameLayout implements MaskProgressApi {

    private final Context mContext;
    private PhotoAdapter mPhotoAdapter;
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
        return mPhotoAdapter.getMaxMediaCount();
    }

    /**
     * 设置最多显示多少个图片/视频/语音
     */
    public void setMaxMediaCount(Integer maxMediaCount, Integer maxImageSelectable, Integer maxVideoSelectable, Integer maxAudioSelectable) {
        // 计算最终呈现的总数，这个总数决定是否还能点击添加
        boolean isMaxMediaCount = maxMediaCount != null &&
                (maxImageSelectable == null || maxVideoSelectable == null || maxAudioSelectable == null);
        if (isMaxMediaCount) {
            mPhotoAdapter.setMaxMediaCount(maxMediaCount);
        } else {
            mPhotoAdapter.setMaxMediaCount(maxImageSelectable + maxVideoSelectable + maxAudioSelectable);
        }
    }

    public void setMaskProgressLayoutListener(MaskProgressLayoutListener listener) {
        mPhotoAdapter.setListener(listener);
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
        SaveStrategy saveStrategy = new SaveStrategy(true, authority, "");
        mMediaStoreCompat = new MediaStoreCompat(getContext(), saveStrategy);
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


        if (drawable == null) {
            drawable = ContextCompat.getDrawable(getContext(), R.color.thumbnail_placeholder);
        }
        // 初始化九宫格的控件
        mViewHolder.rlGrid.setLayoutManager(new GridLayoutManager(getContext(), columnNumber));
        mPhotoAdapter = new PhotoAdapter(mContext, (GridLayoutManager) mViewHolder.rlGrid.getLayoutManager(), this,
                mImageEngine, drawable, isOperation, maxCount,
                maskingColor, maskingTextSize, maskingTextColor, maskingTextContent,
                imageDeleteColor, imageDeleteDrawable, imageAddDrawable);
        mViewHolder.rlGrid.setAdapter(mPhotoAdapter);

        maskProgressLayoutStyle.recycle();
        typedArray.recycle();
    }

    @Override
    public void setAuthority(String authority) {
        SaveStrategy saveStrategy = new SaveStrategy(true, authority, "");
        mMediaStoreCompat.setSaveStrategy(saveStrategy);
    }

    @Override
    public void addImagesUriStartUpload(List<Uri> uris) {
        isAuthority();
        ArrayList<MultiMediaView> multiMediaViews = new ArrayList<>();
        for (Uri uri : uris) {
            MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.PICTURE);
            multiMediaView.setUri(uri);
            multiMediaView.setUploading(true);
            multiMediaViews.add(multiMediaView);
        }
        mPhotoAdapter.addImageData(multiMediaViews);
    }

    @Override
    public void addImagesPathStartUpload(List<String> imagePaths) {
        isAuthority();
        ArrayList<MultiMediaView> multiMediaViews = new ArrayList<>();
        for (String string : imagePaths) {
            MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.PICTURE);
            multiMediaView.setPath(string);
            multiMediaView.setUri(mMediaStoreCompat.getUri(string));
            multiMediaView.setUploading(true);
            multiMediaViews.add(multiMediaView);
        }
        mPhotoAdapter.addImageData(multiMediaViews);
    }

    @Override
    public void setImageUrls(List<String> imagesUrls) {
        ArrayList<MultiMediaView> multiMediaViews = new ArrayList<>();
        for (String string : imagesUrls) {
            MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.PICTURE);
            multiMediaView.setUrl(string);
            multiMediaViews.add(multiMediaView);
        }
        mPhotoAdapter.setImageData(multiMediaViews);
    }

    @Override
    public void addVideoStartUpload(List<Uri> videoUris) {
        addVideo(videoUris, false, true);
    }

    @Override
    public void setVideoCover(MultiMediaView multiMediaView, String videoPath) {
        multiMediaView.setPath(videoPath);
    }

    @Override
    public void setVideoUrls(List<String> videoUrls) {
        ArrayList<MultiMediaView> multiMediaViews = new ArrayList<>();
        for (int i = 0; i < videoUrls.size(); i++) {
            MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.VIDEO);
            multiMediaView.setUploading(false);
            multiMediaView.setUrl(videoUrls.get(i));
            multiMediaViews.add(multiMediaView);
        }
        mPhotoAdapter.setVideoData(multiMediaViews);
    }

    @Override
    public void addAudioStartUpload(String filePath, int length) {
        isAuthority();
        MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.AUDIO);
        multiMediaView.setPath(filePath);
        multiMediaView.setUri(mMediaStoreCompat.getUri(filePath));
        addAudioData(multiMediaView, filePath, length);

        // 检测添加多媒体上限
        mPhotoAdapter.notifyDataSetChanged();
    }

    @Override
    public void setAudioUrls(List<String> audioUrls) {
        List<MultiMediaView> multiMediaViews = new ArrayList<>();
        for (String item : audioUrls) {
            MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.AUDIO);
            multiMediaView.setUrl(item);

            if (MaskProgressLayout.this.audioList == null) {
                MaskProgressLayout.this.audioList = new ArrayList<>();
            }
            audioList.add(multiMediaView);
            multiMediaViews.add(multiMediaView);
        }
        createPlayProgressView(multiMediaViews, 0);
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
        this.audioList.clear();
        // 清空view
        mViewHolder.llContent.removeAllViews();

        // 清空数据和view
        mPhotoAdapter.clearAll();
    }

    @Override
    public ArrayList<MultiMediaView> getImagesAndVideos() {
        return mPhotoAdapter.getData();
    }

    @Override
    public ArrayList<MultiMediaView> getImages() {
        return mPhotoAdapter.getImageData();
    }

    @Override
    public ArrayList<MultiMediaView> getVideos() {
        return mPhotoAdapter.getVideoData();
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
    public void removePosition(int position) {
        mPhotoAdapter.removePosition(position);
    }

    @Override
    public void setOperation(boolean isOperation) {
        this.isOperation = isOperation;
        mPhotoAdapter.setOperation(isOperation);
        // 添加音频后重置所有当前播放中的音频
        for (int i = 0; i < mViewHolder.llContent.getChildCount(); i++) {
            PlayProgressView item = (PlayProgressView) mViewHolder.llContent.getChildAt(i);
            item.setOperation(isOperation);
        }
        isShowRemoveRecorder();
    }

    @Override
    public void onDestroy() {
        mPhotoAdapter.removeListener();

        for (int i = 0; i < mViewHolder.llContent.getChildCount(); i++) {
            PlayProgressView item = (PlayProgressView) mViewHolder.llContent.getChildAt(i);
            item.mViewHolder.playView.onDestroy();
            item.mViewHolder.playView.removeListener();
        }
        this.listener = null;
    }

    /**
     * 递归、有序的创建并且加入音频控件
     */
    private void createPlayProgressView(List<MultiMediaView> audioMultiMediaViews, final int position) {
        if (position >= audioMultiMediaViews.size()) {
            return;
        }
        ThreadUtils.executeByIo(new ThreadUtils.BaseSimpleBaseTask<PlayProgressView>() {

            @Override
            public PlayProgressView doInBackground() {
                PlayProgressView playProgressView;

                playProgressView = newPlayProgressView(audioMultiMediaViews.get(position));
                // 显示音频播放控件，当点击播放的时候，才正式下载并且进行播放
                playProgressView.mViewHolder.playView.setVisibility(View.VISIBLE);
                // 隐藏上传进度
                playProgressView.mViewHolder.groupRecorderProgress.setVisibility(View.GONE);
                isShowRemoveRecorder();

                // 设置数据源
                RecordingItem recordingItem = new RecordingItem();
                recordingItem.setUrl(audioMultiMediaViews.get(position).getUrl());
                playProgressView.setData(recordingItem, audioProgressColor);

                return playProgressView;
            }

            @Override
            public void onSuccess(PlayProgressView playProgressView) {
                // 添加入view
                if (playProgressView != null) {
                    mViewHolder.llContent.addView(playProgressView);
                    int newPosition = position + 1;
                    createPlayProgressView(audioMultiMediaViews, newPosition);
                }
            }
        });
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
     * 添加视频地址
     *
     * @param videoUris   视频列表
     * @param icClean     是否清除
     * @param isUploading 是否触发上传事件
     */
    private void addVideo(List<Uri> videoUris, boolean icClean, boolean isUploading) {
        isAuthority();
        ArrayList<MultiMediaView> multiMediaViews = new ArrayList<>();
        for (int i = 0; i < videoUris.size(); i++) {
            MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.VIDEO);
            multiMediaView.setUri(videoUris.get(i));
            multiMediaView.setUploading(isUploading);
            multiMediaViews.add(multiMediaView);
        }
        if (icClean) {
            mPhotoAdapter.setVideoData(multiMediaViews);
        } else {
            mPhotoAdapter.addVideoData(multiMediaViews);
        }
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
        mViewHolder.llContent.addView(playProgressView);
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
            mPhotoAdapter.notifyDataSetChanged();
        });
        playProgressView.initStyle(audioDeleteColor, audioProgressColor, audioPlayColor);
        multiMediaView.setPlayProgressView(playProgressView);
        playProgressView.setListener(listener);
        return playProgressView;
    }

    /**
     * 检测属性
     */
    private void isAuthority() {
        if (mMediaStoreCompat.getSaveStrategy().getAuthority() == null) {
            // 必须定义authority属性，指定provider的authorities,用于提供给外部的file,否则Android7.0以上报错。也可以代码设置setAuthority
            throw new RuntimeException("You must define the authority attribute, which specifies the provider's authorities, to serve to external files. Otherwise, Android7.0 will report an error.You can also set setAuthority in code");
        }
    }

    public static class ViewHolder {
        View rootView;
        RecyclerView rlGrid;
        LinearLayout llContent;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.rlGrid = rootView.findViewById(R.id.rlGrid);
            this.llContent = rootView.findViewById(R.id.llContent);
        }
    }
}
