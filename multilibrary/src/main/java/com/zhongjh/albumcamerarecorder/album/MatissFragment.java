package com.zhongjh.albumcamerarecorder.album;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;

import gaode.zhongjh.com.common.entity.MultiMedia;
import gaode.zhongjh.com.common.enums.MimeType;
import gaode.zhongjh.com.common.widget.IncapableDialog;

import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.albumcamerarecorder.preview.AlbumPreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.album.model.AlbumCollection;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.MediaSelectionFragment;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.adapter.AlbumMediaAdapter;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.preview.SelectedPreviewActivity;
import com.zhongjh.albumcamerarecorder.album.widget.AlbumsSpinner;
import com.zhongjh.albumcamerarecorder.album.widget.CheckRadioView;
import com.zhongjh.albumcamerarecorder.utils.PathUtils;
import gaode.zhongjh.com.common.enums.MultimediaTypes;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_MULTIMEDIA_TYPES;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_SELECTION;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.EXTRA_RESULT_SELECTION_PATH;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.REQUEST_CODE_PREVIEW;

/**
 * 相册
 * Created by zhongjh on 2018/8/22.
 */
public class MatissFragment extends Fragment implements AlbumCollection.AlbumCallbacks,
        MediaSelectionFragment.SelectionProvider,
        AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener {


    private static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";


    private static final String CHECK_STATE = "checkState";

    private Activity mActivity;
    private Context mContext;

    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private SelectedItemCollection mSelectedCollection;
    private AlbumSpec mAlbumSpec;

    private AlbumsSpinner mAlbumsSpinner;
    private AlbumsSpinnerAdapter mAlbumsSpinnerAdapter;   // 左上角的下拉框适配器

    private boolean mOriginalEnable;        // 是否原图

    private ViewHolder mViewHolder;

    public static MatissFragment newInstance() {
        MatissFragment matissFragment = new MatissFragment();
        Bundle args = new Bundle();
        matissFragment.setArguments(args);
        return matissFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        mSelectedCollection = new SelectedItemCollection(getContext());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mAlbumSpec = AlbumSpec.getInstance();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matiss_zjh, container, false);

        mViewHolder = new ViewHolder(view);
        initView(savedInstanceState);
        initListener();
        return view;
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
        mSelectedCollection.onCreate(savedInstanceState,false);
        if (savedInstanceState != null) {
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE);
        }
        updateBottomToolbar();

        mAlbumsSpinnerAdapter = new AlbumsSpinnerAdapter(mContext, null, false);
        mAlbumsSpinner = new AlbumsSpinner(mContext);
        mAlbumsSpinner.setSelectedTextView(mViewHolder.selected_album);
        mAlbumsSpinner.setPopupAnchorView(mViewHolder.toolbar);
        mAlbumsSpinner.setAdapter(mAlbumsSpinnerAdapter);
        mAlbumCollection.onCreate(this, this);
        mAlbumCollection.onRestoreInstanceState(savedInstanceState);
        mAlbumCollection.loadAlbums();
    }

    private void initListener() {
        // 关闭事件
        mViewHolder.imgClose.setOnClickListener(v -> mActivity.finish());

        // 下拉框选择的时候
        mAlbumsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                // 设置缓存值
                mAlbumCollection.setStateCurrentSelection(position);
                // 移动数据光标到绝对位置
                mAlbumsSpinnerAdapter.getCursor().moveToPosition(position);
                // 获取该位置的专辑
                Album album = Album.valueOf(mAlbumsSpinnerAdapter.getCursor());
//                // 如果有拍照就+1 作废
//                if (album.isAll() && GlobalSpec.getInstance().capture) {
//                    album.addCaptureCount();
//                }
                onAlbumSelected(album);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 预览
        mViewHolder.button_preview.setOnClickListener(view -> {
            Intent intent = new Intent(mActivity, SelectedPreviewActivity.class);
            intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
            intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
            startActivityForResult(intent, REQUEST_CODE_PREVIEW);
        });

        // 确认当前选择的图片
        mViewHolder.button_apply.setOnClickListener(view -> {
            Intent result = new Intent();
            // 获取选择的图片的url集合
            ArrayList<Uri> selectedUris = (ArrayList<Uri>) mSelectedCollection.asListOfUri();
            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris);
            ArrayList<String> selectedPaths = (ArrayList<String>) mSelectedCollection.asListOfString();
            result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths);
            result.putExtra(EXTRA_MULTIMEDIA_TYPES, getMultimediaType(selectedUris));
            result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);// 是否启用原图
            mActivity.setResult(RESULT_OK, result);
            mActivity.finish();
        });

        // 点击原图
        mViewHolder.originalLayout.setOnClickListener(view -> {
            if (getFragmentManager() != null) {
                // 如果有大于限制大小的，就提示
                int count = countOverMaxSize();
                if (count > 0) {
                    IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                            getString(R.string.error_over_original_count, count, mAlbumSpec.originalMaxSize));
                    incapableDialog.show(getFragmentManager(),
                            IncapableDialog.class.getName());
                    return;
                }

                // 设置状态
                mOriginalEnable = !mOriginalEnable;
                mViewHolder.original.setChecked(mOriginalEnable);

                // 设置状态是否原图
                if (mAlbumSpec.onCheckedListener != null) {
                    mAlbumSpec.onCheckedListener.onCheck(mOriginalEnable);
                }

            }
        });

    }

    /**
     * 根据uri列表返回当前全部的类型
     * @param selectedUris uri列表
     * @return 返回当前全部的类型
     */
    private int getMultimediaType(ArrayList<Uri> selectedUris){
        // 循环判断类型
        int isImageSize = 0;// 图片类型的数量
        int isVideoSize = 0;// 视频的数量
        ContentResolver resolver = mContext.getContentResolver();
        for (Uri uri : selectedUris){
            for (MimeType type : MimeType.ofImage()) {
                if (type.checkType(resolver, uri)) {
                    isImageSize++;
                }
            }
            for (MimeType type : MimeType.ofVideo()) {
                if (type.checkType(resolver, uri)) {
                    isVideoSize++;
                }
            }
        }
        // 判断是纯图片还是纯视频
        if (selectedUris.size() == isImageSize){
             return MultimediaTypes.PICTURE;
        }
        if (selectedUris.size() == isVideoSize){
            return MultimediaTypes.VIDEO;
        }
        return MultimediaTypes.BLEND;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSelectedCollection.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 销毁相册model
        mAlbumCollection.onDestroy();
        mAlbumSpec.onCheckedListener = null;
        mAlbumSpec.onSelectedListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //            onBackPressed(); // TODO
        return item.getItemId() == android.R.id.home || super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        // 请求的预览界面
        if (requestCode == REQUEST_CODE_PREVIEW) {
            Bundle resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
            // 获取选择的数据
            ArrayList<MultiMedia> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
            // 是否启用原图
            mOriginalEnable = data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
            int collectionType = resultBundle.getInt(SelectedItemCollection.STATE_COLLECTION_TYPE,
                    SelectedItemCollection.COLLECTION_UNDEFINED);
            // 如果在预览界面点击了确定
            if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                Intent result = new Intent();
                ArrayList<Uri> selectedUris = new ArrayList<>();
                ArrayList<String> selectedPaths = new ArrayList<>();
                if (selected != null) {
                    for (MultiMedia item : selected) {
                        // 添加uri和path
                        selectedUris.add(item.getMediaUri());
                        selectedPaths.add(PathUtils.getPath(getContext(), item.getMediaUri()));
                    }
                }
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris);
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths);
                result.putExtra(EXTRA_MULTIMEDIA_TYPES, getMultimediaType(selectedUris));
                result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable); // 是否启用原图
                mActivity.setResult(RESULT_OK, result);
                mActivity.finish();
            } else {
                // 点击了返回
                mSelectedCollection.overwrite(selected, collectionType);
                if (getFragmentManager() != null) {
                    Fragment mediaSelectionFragment = getFragmentManager().findFragmentByTag(
                            MediaSelectionFragment.class.getSimpleName());
                    if (mediaSelectionFragment instanceof MediaSelectionFragment) {
                        // 刷新数据源
                        ((MediaSelectionFragment) mediaSelectionFragment).refreshMediaGrid();
                    }
                    // 刷新底部
                    updateBottomToolbar();
                }

            }
        }
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
        } else if (selectedCount == 1 && mAlbumSpec.singleSelectionModeEnabled()) {
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
        if (mAlbumSpec.originalable) {
            mViewHolder.originalLayout.setVisibility(View.VISIBLE);
            updateOriginalState();
        } else {
            mViewHolder.originalLayout.setVisibility(View.INVISIBLE);
        }

        showBottomView(selectedCount);
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
                        getString(R.string.error_over_original_size, mAlbumSpec.originalMaxSize));
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
            MultiMedia item = mSelectedCollection.asList().get(i);

            if (item.isImage()) {
                float size = PhotoMetadataUtils.getSizeInMB(item.size);

                if (size > mAlbumSpec.originalMaxSize) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public void onAlbumLoadFinished(final Cursor cursor) {
        // 更新相册列表
        mAlbumsSpinnerAdapter.swapCursor(cursor);
        // 选择默认相册
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            cursor.moveToPosition(mAlbumCollection.getCurrentSelection());
            mAlbumsSpinner.setSelection(getContext(),
                    mAlbumCollection.getCurrentSelection());
            Album album = Album.valueOf(cursor);
            onAlbumSelected(album);
        });
    }

    @Override
    public void onAlbumReset() {
        // 重置相册列表
        mAlbumsSpinnerAdapter.swapCursor(null);
    }

    /**
     * 选择某个专辑的时候
     *
     * @param album 专辑
     */
    private void onAlbumSelected(Album album) {
        if (album.isAll() && album.isEmpty()) {
            // 如果是选择全部并且没有数据的话，显示空的view
            mViewHolder.container.setVisibility(View.GONE);
            mViewHolder.empty_view.setVisibility(View.VISIBLE);
        } else {
            // 如果有数据，则内嵌新的fragment，并且相应相关照片
            mViewHolder.container.setVisibility(View.VISIBLE);
            mViewHolder.empty_view.setVisibility(View.GONE);
            Fragment fragment = MediaSelectionFragment.newInstance(album);
            if (getFragmentManager() != null)
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment, MediaSelectionFragment.class.getSimpleName())
                        .commitAllowingStateLoss();
        }
    }

    @Override
    public void onUpdate() {
        // notify bottom toolbar that check state changed.
        updateBottomToolbar();
        // 触发选择的接口事件
        if (mAlbumSpec.onSelectedListener != null) {
            mAlbumSpec.onSelectedListener.onSelected(
                    mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString());
        }
    }

    @Override
    public void onMediaClick(Album album, MultiMedia item, int adapterPosition) {
        Intent intent = new Intent(mActivity, AlbumPreviewActivity.class);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, album);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);
        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
        startActivityForResult(intent, REQUEST_CODE_PREVIEW);
    }

    @Override
    public SelectedItemCollection provideSelectedItemCollection() {
        return mSelectedCollection;
    }

    /**
     * 显示本身的底部
     * 隐藏母窗体的table
     * 以后如果有配置，就检查配置是否需要隐藏母窗体
     *
     * @param count 当前选择的数量
     */
    private void showBottomView(int count) {
        if (count > 0) {
            // 显示底部
            mViewHolder.bottom_toolbar.setVisibility(View.VISIBLE);
            // 隐藏母窗体的table
            ((MainActivity) mActivity).showHideTableLayout(false);
        } else {
            // 显示底部
            mViewHolder.bottom_toolbar.setVisibility(View.GONE);
            // 隐藏母窗体的table
            ((MainActivity) mActivity).showHideTableLayout(true);
        }
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
        public ImageView imgClose;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.selected_album = rootView.findViewById(R.id.selected_album);
            this.toolbar = rootView.findViewById(R.id.toolbar);
            this.button_preview = rootView.findViewById(R.id.button_preview);
            this.original = rootView.findViewById(R.id.original);
            this.originalLayout = rootView.findViewById(R.id.originalLayout);
            this.button_apply = rootView.findViewById(R.id.button_apply);
            this.bottom_toolbar = rootView.findViewById(R.id.bottom_toolbar);
            this.container = rootView.findViewById(R.id.container);
            this.empty_view_content = rootView.findViewById(R.id.empty_view_content);
            this.empty_view = rootView.findViewById(R.id.empty_view);
            this.root = rootView.findViewById(R.id.root);
            this.imgClose = rootView.findViewById(R.id.imgClose);
        }

    }
}
