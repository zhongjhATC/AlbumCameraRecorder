package com.zhongjh.cameraviewsoundrecorder.album;

/**
 * Created by zhongjh on 2018/8/23.
 */

import android.app.Activity;
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

import com.zhongjh.cameraviewsoundrecorder.MainActivity;
import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.album.entity.Album;
import com.zhongjh.cameraviewsoundrecorder.album.entity.Item;
import com.zhongjh.cameraviewsoundrecorder.settings.GlobalSpec;
import com.zhongjh.cameraviewsoundrecorder.album.model.AlbumCollection;
import com.zhongjh.cameraviewsoundrecorder.album.model.SelectedItemCollection;
import com.zhongjh.cameraviewsoundrecorder.album.ui.mediaselection.MediaSelectionFragment;
import com.zhongjh.cameraviewsoundrecorder.album.ui.mediaselection.adapter.AlbumMediaAdapter;
import com.zhongjh.cameraviewsoundrecorder.album.ui.preview.BasePreviewActivity;
import com.zhongjh.cameraviewsoundrecorder.album.ui.preview.selectedpreview.SelectedPreviewActivity;
import com.zhongjh.cameraviewsoundrecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.cameraviewsoundrecorder.album.widget.AlbumsSpinner;
import com.zhongjh.cameraviewsoundrecorder.album.widget.CheckRadioView;
import com.zhongjh.cameraviewsoundrecorder.utils.PathUtils;
import com.zhongjh.cameraviewsoundrecorder.widget.IncapableDialog;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.zhongjh.cameraviewsoundrecorder.utils.Constant.EXTRA_RESULT_SELECTION;
import static com.zhongjh.cameraviewsoundrecorder.utils.Constant.EXTRA_RESULT_SELECTION_PATH;

/**
 * Created by zhongjh on 2018/8/22.
 */
public class MatissFragment extends Fragment implements AlbumCollection.AlbumCallbacks,
        MediaSelectionFragment.SelectionProvider,
        AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener {


    public static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";
    private static final int REQUEST_CODE_PREVIEW = 23;     // 预览
    private static final int REQUEST_CODE_CAPTURE = 24;     // 拍照

    public static final String CHECK_STATE = "checkState";

    protected Activity mActivity;
    private Context mContext;

    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private SelectedItemCollection mSelectedCollection;
    private GlobalSpec mSpec;

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
        mSpec = GlobalSpec.getInstance();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
        mSelectedCollection.onCreate(savedInstanceState);
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
            result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);// 是否启用原图
            mActivity.setResult(RESULT_OK, result);
            mActivity.finish();
        });

        // 点击原图
        mViewHolder.originalLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (getFragmentManager() != null) {
                    // 如果有大于限制大小的，就提示
                    int count = countOverMaxSize();
                    if (count > 0) {
                        IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                                getString(R.string.error_over_original_count, count, mSpec.originalMaxSize));
                        incapableDialog.show(getFragmentManager(),
                                IncapableDialog.class.getName());
                        return;
                    }

                    // 设置状态
                    mOriginalEnable = !mOriginalEnable;
                    mViewHolder.original.setChecked(mOriginalEnable);

                    // 设置状态是否原图
                    if (mSpec.onCheckedListener != null) {
                        mSpec.onCheckedListener.onCheck(mOriginalEnable);
                    }

                }
            }
        });

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
        mSpec.onCheckedListener = null;
        mSpec.onSelectedListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
//            onBackPressed(); // TODO
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            ArrayList<Item> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
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
                    for (Item item : selected) {
                        // 添加uri和path
                        selectedUris.add(item.getContentUri());
                        selectedPaths.add(PathUtils.getPath(getContext(), item.getContentUri()));
                    }
                }
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris);
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths);
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
        } else if (requestCode == REQUEST_CODE_CAPTURE) {
            // 如果是拍照返回
            // Just pass the data back to previous calling Activity.
//            Uri contentUri = mMediaStoreCompat.getCurrentPhotoUri();
//            String path = mMediaStoreCompat.getCurrentPhotoPath();
//            ArrayList<Uri> selected = new ArrayList<>();
//            selected.add(contentUri);
//            ArrayList<String> selectedPath = new ArrayList<>();
//            selectedPath.add(path);
//            Intent result = new Intent();
//            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selected);
//            result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPath);
//            setResult(RESULT_OK, result);
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
//                MatisseActivity.this.revokeUriPermission(contentUri,
//                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            finish();
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

    @Override
    public void onAlbumLoadFinished(final Cursor cursor) {
        // 更新相册列表
        mAlbumsSpinnerAdapter.swapCursor(cursor);
        // 选择默认相册
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                cursor.moveToPosition(mAlbumCollection.getCurrentSelection());
                mAlbumsSpinner.setSelection(getContext(),
                        mAlbumCollection.getCurrentSelection());
                Album album = Album.valueOf(cursor);
                // 作废
//                if (album.isAll() && GlobalSpec.getInstance().capture) {
//                    // 判断如果是 查询全部 并且可以拍照的话，就相片数量+1，放拍照功能
//                    album.addCaptureCount();
//                }
                onAlbumSelected(album);
            }
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
        if (mSpec.onSelectedListener != null) {
            mSpec.onSelectedListener.onSelected(
                    mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString());
        }
    }

    @Override
    public void onMediaClick(Album album, Item item, int adapterPosition) {
//        Intent intent = new Intent(this, AlbumPreviewActivity.class);
//        intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, album);
//        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);
//        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
//        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
//        startActivityForResult(intent, REQUEST_CODE_PREVIEW);
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
