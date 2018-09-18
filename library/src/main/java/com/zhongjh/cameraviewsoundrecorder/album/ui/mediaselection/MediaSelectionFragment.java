package com.zhongjh.cameraviewsoundrecorder.album.ui.mediaselection;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.zhongjh.cameraviewsoundrecorder.album.entity.Album;
import com.zhongjh.cameraviewsoundrecorder.album.ui.mediaselection.adapter.AlbumMediaAdapter;

/**
 * Created by zhongjh on 2018/8/30.
 */
public class MediaSelectionFragment extends Fragment {

    public static final String EXTRA_ALBUM = "extra_album";     // 专辑数据

    private AlbumMediaAdapter mAdapter;

    /**
     * 实例化
     * @param album 专辑
     */
    public static MediaSelectionFragment newInstance(Album album) {
        MediaSelectionFragment fragment = new MediaSelectionFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_ALBUM, album);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 刷新数据源
     */
    public void refreshMediaGrid() {
        mAdapter.notifyDataSetChanged();
    }

}
