package com.zhongjh.albumcamerarecorder.camera.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * 权限工具
 *
 * @author zhongjh
 */
public class PermissionUtil {

    public static final int STATE_RECORDING = -1;
    public static final int STATE_NO_PERMISSION = -2;
    public static final int STATE_SUCCESS = 1;

    /**
     * 用于检测是否具有录音权限
     *
     * @return 状态：是否具有
     */
    public static int getRecordState() {
        int minBuffer = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat
                .ENCODING_PCM_16BIT);
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 44100, AudioFormat
                .CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, (minBuffer * 100));
        short[] point = new short[minBuffer];
        int readSize;
        try {

            audioRecord.startRecording();//检测是否可以进入初始化状态
        } catch (Exception e) {
            audioRecord.release();
            return STATE_NO_PERMISSION;
        }
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            //6.0以下机型都会返回此状态，故使用时需要判断bulid版本
            //检测是否在录音中
            audioRecord.stop();
            audioRecord.release();
            Log.d("CheckAudioPermission", "录音机被占用");
            return STATE_RECORDING;
        } else {
            //检测是否可以获取录音结果

            readSize = audioRecord.read(point, 0, point.length);


            if (readSize <= 0) {
                audioRecord.stop();
                audioRecord.release();

                Log.d("CheckAudioPermission", "录音的结果为空");
                return STATE_NO_PERMISSION;

            } else {
                audioRecord.stop();
                audioRecord.release();

                return STATE_SUCCESS;
            }
        }
    }

}