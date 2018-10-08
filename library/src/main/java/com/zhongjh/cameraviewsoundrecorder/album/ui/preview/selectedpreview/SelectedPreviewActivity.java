package com.zhongjh.cameraviewsoundrecorder.album.ui.preview.selectedpreview;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.zhongjh.cameraviewsoundrecorder.album.entity.Item;
import com.zhongjh.cameraviewsoundrecorder.album.entity.SelectionSpec;
import com.zhongjh.cameraviewsoundrecorder.album.model.SelectedItemCollection;
import com.zhongjh.cameraviewsoundrecorder.album.ui.preview.BasePreviewActivity;

import java.util.List;

public class SelectedPreviewActivity extends BasePreviewActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SelectionSpec.getInstance().hasInited) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        Bundle bundle = getIntent().getBundleExtra(EXTRA_DEFAULT_BUNDLE);
        List<Item> selected = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
        mAdapter.addAll(selected);
        mAdapter.notifyDataSetChanged();
        if (mSpec.countable) {
            mCheckView.setCheckedNum(1);
        } else {
            mCheckView.setChecked(true);
        }
        mPreviousPos = 0;
        updateSize(selected.get(0));
    }

}