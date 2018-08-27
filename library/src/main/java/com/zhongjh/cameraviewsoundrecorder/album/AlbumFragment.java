package com.zhongjh.cameraviewsoundrecorder.album;

/**
 * Created by zhongjh on 2018/8/23.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.album.entity.SelectionSpec;

/**
 * Created by zhongjh on 2018/8/22.
 */
public class AlbumFragment extends Fragment {


    protected Activity mActivity;
    private Context mContext;
    private SelectionSpec mSpec;
    private ViewHolder mViewHolder;

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
        mViewHolder = new ViewHolder(view);
        initView();
        return view;
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        this.mActivity = activity;
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }


    /**
     * 初始化view
     */
    private void initView() {
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
//        mSelectedCollection.onCreate(savedInstanceState);

    }

    private void initListener(){
//        // 预览
//        mViewHolder.button_preview.setOnClickListener(view -> {
//            Intent intent = new Intent(this, SelectedPreviewActivity.class);
//            intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
//            intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
//            startActivityForResult(intent, REQUEST_CODE_PREVIEW);
//        });
//        mButtonApply.setOnClickListener(this);
    }


    public static class ViewHolder {
        public View rootView;
        public TextView selected_album;
        public Toolbar toolbar;
        public TextView button_preview;
        public LinearLayout originalLayout;
        public TextView button_apply;
        public FrameLayout bottom_toolbar;
        public FrameLayout container;
        public TextView empty_view_content;
        public FrameLayout empty_view;
        public RelativeLayout root;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.selected_album = rootView.findViewById(R.id.selected_album);
            this.toolbar = rootView.findViewById(R.id.toolbar);
            this.button_preview = rootView.findViewById(R.id.button_preview);
            this.originalLayout = rootView.findViewById(R.id.originalLayout);
            this.button_apply = rootView.findViewById(R.id.button_apply);
            this.bottom_toolbar = rootView.findViewById(R.id.bottom_toolbar);
            this.container = rootView.findViewById(R.id.container);
            this.empty_view_content = rootView.findViewById(R.id.empty_view_content);
            this.empty_view = rootView.findViewById(R.id.empty_view);
            this.root = rootView.findViewById(R.id.root);
        }

    }
}
