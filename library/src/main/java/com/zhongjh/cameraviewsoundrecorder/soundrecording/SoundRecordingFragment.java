package com.zhongjh.cameraviewsoundrecorder.soundrecording;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.CameraSuccessListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.ErrorListener;
import com.zhongjh.cameraviewsoundrecorder.camera.util.DeviceUtil;
import com.zhongjh.cameraviewsoundrecorder.camera.widget.CameraLayout;

import java.io.File;

import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.BUTTON_STATE_BOTH;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.MEDIA_QUALITY_MIDDLE;

/**
 * 录音
 * Created by zhongjh on 2018/8/22.
 */
public class SoundRecordingFragment extends Fragment {

    private String title;
    private int page;

    public static SoundRecordingFragment newInstance(int page, String title) {
        SoundRecordingFragment soundRecordingFragment = new SoundRecordingFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        soundRecordingFragment.setArguments(args);
        return soundRecordingFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_soundrecording_zjh, container, false);
        return view;
    }

}
