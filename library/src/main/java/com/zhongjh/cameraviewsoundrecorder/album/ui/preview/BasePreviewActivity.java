package com.zhongjh.cameraviewsoundrecorder.album.ui.preview;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.album.entity.IncapableCause;
import com.zhongjh.cameraviewsoundrecorder.album.entity.Item;
import com.zhongjh.cameraviewsoundrecorder.album.entity.SelectionSpec;
import com.zhongjh.cameraviewsoundrecorder.album.model.SelectedItemCollection;
import com.zhongjh.cameraviewsoundrecorder.album.ui.preview.selectedpreview.adapter.PreviewPagerAdapter;
import com.zhongjh.cameraviewsoundrecorder.album.widget.CheckRadioView;
import com.zhongjh.cameraviewsoundrecorder.album.widget.CheckView;
import com.zhongjh.cameraviewsoundrecorder.album.widget.PreviewViewPager;
import com.zhongjh.cameraviewsoundrecorder.utils.VersionUtils;

/**
 * 预览的基类
 */
public class BasePreviewActivity extends AppCompatActivity {

    public static final String EXTRA_DEFAULT_BUNDLE = "extra_default_bundle";
    public static final String EXTRA_RESULT_BUNDLE = "extra_result_bundle";
    public static final String EXTRA_RESULT_APPLY = "extra_result_apply";
    public static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";
    public static final String CHECK_STATE = "checkState";

    protected final SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);
    protected SelectionSpec mSpec;

    protected PreviewPagerAdapter mAdapter;

    protected boolean mOriginalEnable;      // 是否原图

    ViewHolder mViewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(SelectionSpec.getInstance().themeId);  // 获取样式
        super.onCreate(savedInstanceState);
        if (!SelectionSpec.getInstance().hasInited) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        setContentView(R.layout.activity_media_preview_zjh);
        if (VersionUtils.hasKitKat()) {
            // 使用沉倾状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mSpec = SelectionSpec.getInstance();
        if (mSpec.needOrientationRestriction()) {
            // 设置旋转模式
            setRequestedOrientation(mSpec.orientation);
        }

        if (savedInstanceState == null) {
            // 初始化别的界面传递过来的数据
            mSelectedCollection.onCreate(getIntent().getBundleExtra(EXTRA_DEFAULT_BUNDLE));
            mOriginalEnable = getIntent().getBooleanExtra(EXTRA_RESULT_ORIGINAL_ENABLE, false);
        } else {
            // 初始化缓存的数据
            mSelectedCollection.onCreate(savedInstanceState);
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE);
        }

        mViewHolder = new ViewHolder(this);

        mAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), null);
        mViewHolder.pager.setAdapter(mAdapter);
        mViewHolder.check_view.setCountable(mSpec.countable);

    }

    private void initListener(){
        mViewHolder.button_back.setOnClickListener(this);
        mViewHolder.button_apply.setOnClickListener(this);
        mViewHolder.pager.addOnPageChangeListener(this);
        mViewHolder.check_view.setOnClickListener(v -> {
            Item item = mAdapter.getMediaItem(mViewHolder.pager.getCurrentItem());
            if (mSelectedCollection.isSelected(item)) {
                mSelectedCollection.remove(item);
                if (mSpec.countable) {
                    mViewHolder.check_view.setCheckedNum(CheckView.UNCHECKED);
                } else {
                    mViewHolder.check_view.setChecked(false);
                }
            } else {
                if (assertAddSelection(item)) {
                    mSelectedCollection.add(item);
                    if (mSpec.countable) {
                        mViewHolder.check_view.setCheckedNum(mSelectedCollection.checkedNumOf(item));
                    } else {
                        mViewHolder.check_view.setChecked(true);
                    }
                }
            }
            updateApplyButton();

            if (mSpec.onSelectedListener != null) {
                mSpec.onSelectedListener.onSelected(
                        mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString());
            }
        });
    }

    private boolean assertAddSelection(Item item) {
        IncapableCause cause = mSelectedCollection.isAcceptable(item);
        IncapableCause.handleCause(this, cause);
        return cause == null;
    }

    public static class ViewHolder {
        public Activity activity;
        public PreviewViewPager pager;
        public TextView button_back;
        public CheckRadioView original;
        public LinearLayout originalLayout;
        public TextView size;
        public TextView button_apply;
        public FrameLayout bottom_toolbar;
        public CheckView check_view;

        public ViewHolder(Activity activity) {
            this.activity = activity;
            this.pager = activity.findViewById(R.id.pager);
            this.button_back = activity.findViewById(R.id.button_back);
            this.original = activity.findViewById(R.id.original);
            this.originalLayout = activity.findViewById(R.id.originalLayout);
            this.size = activity.findViewById(R.id.size);
            this.button_apply = activity.findViewById(R.id.button_apply);
            this.bottom_toolbar = activity.findViewById(R.id.bottom_toolbar);
            this.check_view = activity.findViewById(R.id.check_view);
        }

    }
}
