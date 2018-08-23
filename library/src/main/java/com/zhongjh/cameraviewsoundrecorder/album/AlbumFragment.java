package com.zhongjh.cameraviewsoundrecorder.album;

/**
 * Created by zhongjh on 2018/8/23.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.album.entity.SelectionSpec;
import com.zhongjh.cameraviewsoundrecorder.album.utils.MediaStoreCompat;

/**
 * Created by zhongjh on 2018/8/22.
 */
public class AlbumFragment extends Fragment {

    private SelectionSpec mSpec;

    public static AlbumFragment newInstance(int page, String title) {
        AlbumFragment albumFragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        albumFragment.setArguments(args);
        return albumFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mSpec = SelectionSpec.getInstance();
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_zjh, container, false);
        return view;
    }
}
