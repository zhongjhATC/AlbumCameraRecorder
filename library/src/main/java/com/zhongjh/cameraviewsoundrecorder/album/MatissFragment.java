package com.zhongjh.cameraviewsoundrecorder.album;

/**
 * Created by zhongjh on 2018/8/23.
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.album.entity.Item;
import com.zhongjh.cameraviewsoundrecorder.album.entity.SelectionSpec;
import com.zhongjh.cameraviewsoundrecorder.album.model.SelectedItemCollection;
import com.zhongjh.cameraviewsoundrecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.cameraviewsoundrecorder.album.widget.AlbumsSpinner;
import com.zhongjh.cameraviewsoundrecorder.album.widget.CheckRadioView;
import com.zhongjh.cameraviewsoundrecorder.widget.IncapableDialog;

/**
 * Created by zhongjh on 2018/8/22.
 */
public class MatissFragment extends Fragment {

    public static final String CHECK_STATE = "checkState";

    protected Activity mActivity;
    private Context mContext;
    private SelectedItemCollection mSelectedCollection = new SelectedItemCollection(getContext());
    private SelectionSpec mSpec;

    private AlbumsSpinner mAlbumsSpinner;
    private AlbumsSpinnerAdapter mAlbumsSpinnerAdapter;   // 左上角的下拉框适配器

    private boolean mOriginalEnable;        // 是否原图

    private ViewHolder mViewHolder;

    public static MatissFragment newInstance(int page, String title) {
        MatissFragment matissFragment = new MatissFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        matissFragment.setArguments(args);
        return matissFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mSpec = SelectionSpec.getInstance();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matiss_zjh, container, false);
        mViewHolder = new ViewHolder(view);
        initView(savedInstanceState);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }


    /**
     * 初始化view
     */
    private void initView(Bundle savedInstanceState) {
//        setSupportActionBar(toolbar);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setDisplayHomeAsUpEnabled(true);

        Drawable navigationIcon = mViewHolder.toolbar.getNavigationIcon();
        TypedArray ta = mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.album_element_color});
        int color = ta.getColor(0, 0);
        ta.recycle();
        if (navigationIcon != null) {
            navigationIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        mSelectedCollection.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE);
        }
        updateBottomToolbar();

        mAlbumsSpinnerAdapter = new AlbumsSpinnerAdapter(mContext, null, false);
        mAlbumsSpinner = new AlbumsSpinner(this);
        mAlbumsSpinner.setOnItemSelectedListener(this);
        mAlbumsSpinner.setSelectedTextView((TextView) findViewById(R.id.selected_album));
        mAlbumsSpinner.setPopupAnchorView(findViewById(R.id.toolbar));
        mAlbumsSpinner.setAdapter(mAlbumsAdapter);
        mAlbumCollection.onCreate(this, this);
        mAlbumCollection.onRestoreInstanceState(savedInstanceState);
        mAlbumCollection.loadAlbums();
    }

    private void initListener() {
//        // 预览
//        mViewHolder.button_preview.setOnClickListener(view -> {
//            Intent intent = new Intent(this, SelectedPreviewActivity.class);
//            intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
//            intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
//            startActivityForResult(intent, REQUEST_CODE_PREVIEW);
//        });
//        mButtonApply.setOnClickListener(this);
    }

    /**
     * 更新底部数据
     */
    private void updateBottomToolbar() {
        int selectedCount = mSelectedCollection.count();

        if (selectedCount == 0) {
            // 如果没有数据，则设置不可点击
            mViewHolder.button_preview.setEnabled(false);
            mViewHolder.button_apply.setEnabled(false);
            mViewHolder.button_apply.setText(getString(R.string.button_sure_default));
        } else if (selectedCount == 1 && mSpec.singleSelectionModeEnabled()) {
            // 不显示选择的数字
            mViewHolder.button_preview.setEnabled(true);
            mViewHolder.button_apply.setText(R.string.button_sure_default);
            mViewHolder.button_apply.setEnabled(true);
        } else {
            // 显示选择的数字
            mViewHolder.button_preview.setEnabled(true);
            mViewHolder.button_apply.setEnabled(true);
            mViewHolder.button_apply.setText(getString(R.string.button_sure, selectedCount));
        }

        // 是否显示原图控件
        if (mSpec.originalable) {
            mViewHolder.originalLayout.setVisibility(View.VISIBLE);
            updateOriginalState();
        } else {
            mViewHolder.originalLayout.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * 更新原图控件状态
     */
    private void updateOriginalState() {
        // 设置选择状态
        mViewHolder.original.setChecked(mOriginalEnable);
        if (countOverMaxSize() > 0) {
            // 是否启用原图
            if (mOriginalEnable) {
                // 弹出窗口提示大于xxmb
                IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                        getString(R.string.error_over_original_size, mSpec.originalMaxSize));
                if (this.getFragmentManager() == null)
                    return;
                incapableDialog.show(this.getFragmentManager(),
                        IncapableDialog.class.getName());

                // 底部的原图钩去掉
                mViewHolder.original.setChecked(false);
                mOriginalEnable = false;
            }
        }
    }

    /**
     * 返回大于限定mb的图片数量
     *
     * @return 数量
     */
    private int countOverMaxSize() {
        int count = 0;
        int selectedCount = mSelectedCollection.count();
        for (int i = 0; i < selectedCount; i++) {
            Item item = mSelectedCollection.asList().get(i);

            if (item.isImage()) {
                float size = PhotoMetadataUtils.getSizeInMB(item.size);

                if (size > mSpec.originalMaxSize) {
                    count++;
                }
            }
        }
        return count;
    }

    public static class ViewHolder {
        public View rootView;
        public TextView selected_album;
        public Toolbar toolbar;
        public TextView button_preview;
        public CheckRadioView original;
        public LinearLayout originalLayout;
        public TextView button_apply;
        public FrameLayout bottom_toolbar;
        public FrameLayout container;
        public TextView empty_view_content;
        public FrameLayout empty_view;
        public RelativeLayout root;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.selected_album = (TextView) rootView.findViewById(R.id.selected_album);
            this.toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            this.button_preview = (TextView) rootView.findViewById(R.id.button_preview);
            this.original = (CheckRadioView) rootView.findViewById(R.id.original);
            this.originalLayout = (LinearLayout) rootView.findViewById(R.id.originalLayout);
            this.button_apply = (TextView) rootView.findViewById(R.id.button_apply);
            this.bottom_toolbar = (FrameLayout) rootView.findViewById(R.id.bottom_toolbar);
            this.container = (FrameLayout) rootView.findViewById(R.id.container);
            this.empty_view_content = (TextView) rootView.findViewById(R.id.empty_view_content);
            this.empty_view = (FrameLayout) rootView.findViewById(R.id.empty_view);
            this.root = (RelativeLayout) rootView.findViewById(R.id.root);
        }

    }
}
