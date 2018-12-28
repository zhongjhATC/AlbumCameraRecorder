package com.zhongjh.cameraviewsoundrecorder.album.ui.preview.selectedpreview;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.zhongjh.cameraviewsoundrecorder.album.entity.Item;
import com.zhongjh.cameraviewsoundrecorder.settings.GlobalSpec;
import com.zhongjh.cameraviewsoundrecorder.album.model.SelectedItemCollection;
import com.zhongjh.cameraviewsoundrecorder.album.ui.preview.BasePreviewActivity;

import java.util.List;

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
        List<Item> selected = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
        mAdapter.addAll(selected);
        mAdapter.notifyDataSetChanged();
        if (mAlbumSpec.countable) {
            mViewHolder.check_view.setCheckedNum(1);
        } else {
            mViewHolder.check_view.setChecked(true);
        }
        mPreviousPos = 0;
        updateSize(selected.get(0));
    }

}