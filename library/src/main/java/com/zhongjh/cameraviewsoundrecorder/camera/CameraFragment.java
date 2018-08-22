package com.zhongjh.cameraviewsoundrecorder.camera;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhongjh.cameraviewsoundrecorder.R;

/**
 * Created by zhongjh on 2018/8/22.
 */
public class CameraFragment extends Fragment {

    private String title;
    private int page;

    public static CameraFragment newInstance(int page, String title) {
        CameraFragment cameraFragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        cameraFragment.setArguments(args);
        return cameraFragment;
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
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        return view;
    }
}
