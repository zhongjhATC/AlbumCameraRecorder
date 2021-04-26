package com.zhongjh.albumcamerarecorder.recorder.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;

import java.io.File;
import java.io.IOException;

import gaode.zhongjh.com.common.utils.MediaStoreCompat;

/**
 * 录音的service
 * @author zhongjh
 */
public class RecordingService extends Service {

    private static final String TAG = "RecordingService";

    private File mFile= null;

    private MediaRecorder mRecorder = null;

    private long mStartingTimeMillis = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording(false);
        }

        super.onDestroy();
    }

    /**
     * 开始录音
     */
    private void startRecording() {
        // 根据配置创建文件配置
        GlobalSpec globalSpec = GlobalSpec.getInstance();
        // 音频文件配置路径
        MediaStoreCompat mAudioMediaStoreCompat = new MediaStoreCompat(this);
        mAudioMediaStoreCompat.setSaveStrategy(globalSpec.audioStrategy == null ? globalSpec.saveStrategy : globalSpec.audioStrategy);

        mFile = mAudioMediaStoreCompat.getFilePath(2);

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFile.getPath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    /**
     * 停止录音
     *
     * @param isShort 短时结束不算
     */
    private void stopRecording(boolean isShort) {
        if (isShort) {
            // 如果是短时间的，删除该文件
            if (mFile.exists()) {
                mFile.delete();
            }
        } else {
            long mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
            // 存储到缓存的文件地址
            getSharedPreferences("sp_name_audio", MODE_PRIVATE)
                    .edit()
                    .putString("audio_path", mFile.getPath())
                    .putLong("elpased", mElapsedMillis)
                    .apply();
        }


        Log.d(TAG, "停止录音");
        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch (RuntimeException ignored) {
                // 防止立即录音完成
            }
            mRecorder.release();
            mRecorder = null;
        }
    }


}
