package com.zhongjh.cameraviewsoundrecorder.soundrecording;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhongjh.cameraviewsoundrecorder.R;

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
