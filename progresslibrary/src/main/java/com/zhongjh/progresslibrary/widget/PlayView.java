package com.zhongjh.progresslibrary.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.zhongjh.progresslibrary.entity.RecordingItem;

import java.util.concurrent.TimeUnit;

/**
 * 一个播放的view
 * Created by zhongjh on 2019/2/1.
 */
public class PlayView extends FrameLayout {

    private static final String LOG_TAG = "PlayView";
    private static final String ARG_ITEM = "recording_item";
    private RecordingItem mRecordingItem;   // 当前音频数据

    // stores minutes and seconds of the length of the file.
    long minutes = 0;
    long seconds = 0;

    public PlayView(@NonNull Context context) {
        super(context);
    }

    public PlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化相关数据
     */
    public void init(RecordingItem recordingItem){
        this.mRecordingItem = recordingItem;
        long itemDuration = mRecordingItem.getLength();
        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);
    }

}
