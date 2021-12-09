package com.zhongjh.albumcamerarecorder.preview;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;

import java.util.List;

import com.zhongjh.common.entity.MultiMedia;

/**
 * 点击左下角的预览按钮进入的界面
 * @author zhongjh
 */
public class SelectedPreviewActivity extends BasePreviewActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!GlobalSpec.getInstance().hasInited) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        Bundle bundle = getIntent().getBundleExtra(EXTRA_DEFAULT_BUNDLE);
        List<MultiMedia> selected = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
        mAdapter.addAll(selected);
        mAdapter.notifyDataSetChanged();
        if (mAlbumSpec.countable) {
            mViewHolder.checkView.setCheckedNum(1);
        } else {
            mViewHolder.checkView.setChecked(true);
        }
        mPreviousPos = 0;
        updateSize(selected.get(0));
    }

}