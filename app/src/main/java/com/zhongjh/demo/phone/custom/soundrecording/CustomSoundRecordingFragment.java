package com.zhongjh.demo.phone.custom.soundrecording;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.zhongjh.demo.R;
import com.zhongjh.demo.databinding.FragmentSoundrecordingSmallBinding;
import com.zhongjh.demo.phone.custom.camera4.CameraSmallFragment;
import com.zhongjh.multimedia.recorder.BaseSoundRecordingFragment;
import com.zhongjh.multimedia.recorder.widget.SoundRecordingLayout;

public class CustomSoundRecordingFragment extends BaseSoundRecordingFragment {

    FragmentSoundrecordingSmallBinding mBinding;

    public static CustomSoundRecordingFragment newInstance() {
        return new CustomSoundRecordingFragment();
    }

    @NonNull
    @Override
    public View setContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_soundrecording_small, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void initView(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    @NonNull
    @Override
    public SoundRecordingLayout getSoundRecordingLayout() {
        return mBinding.pvLayout;
    }

    @NonNull
    @Override
    public Chronometer getChronometer() {
        return mBinding.chronometer;
    }
}
