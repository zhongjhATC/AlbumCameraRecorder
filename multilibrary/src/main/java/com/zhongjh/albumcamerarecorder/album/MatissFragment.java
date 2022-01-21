package com.zhongjh.albumcamerarecorder.album;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.album.model.AlbumCollection;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.MediaSelectionFragment;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.adapter.AlbumMediaAdapter;
import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.albumcamerarecorder.album.widget.AlbumsSpinner;
import com.zhongjh.albumcamerarecorder.album.widget.CheckRadioView;
import com.zhongjh.albumcamerarecorder.preview.AlbumPreviewActivity;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.preview.SelectedPreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.utils.MediaStoreUtils;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.utils.ColorFilterUtil;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.StatusBarUtils;
import com.zhongjh.common.utils.ThreadUtils;
import com.zhongjh.common.widget.IncapableDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION_LOCAL_FILE;

/**
 * 相册
 *
 * @author zhongjh
 * @date 2018/8/22
 */
public class MatissFragment extends Fragment implements AlbumCollection.AlbumCallbacks,
        MediaSelectionFragment.SelectionProvider,
        AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener {

    private static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";
    public static final String ARGUMENTS_MARGIN_BOTTOM = "arguments_margin_bottom";

    private static final String CHECK_STATE = "checkState";

    private AppCompatActivity mActivity;
    private Context mContext;
    /**
     * 上一个Fragment,因为切换相册后，数据要进行一次销毁才能读取
     */
    MediaSelectionFragment mFragmentLast;

    /**
     * 公共配置
     */
    private GlobalSpec mGlobalSpec;
    /**
     * 图片配置
     */
    private MediaStoreCompat mPictureMediaStoreCompat;
    /**
     * 录像文件配置路径
     */
    private MediaStoreCompat mVideoMediaStoreCompat;

    /**
     * 专辑下拉数据源
     */
    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private SelectedItemCollection mSelectedCollection;
    private AlbumSpec mAlbumSpec;

    /**
     * 专辑下拉框控件
     */
    private AlbumsSpinner mAlbumsSpinner;
    /**
     * 左上角的下拉框适配器
     */
    private AlbumsSpinnerAdapter mAlbumsSpinnerAdapter;

    /**
     * 是否原图
     */
    private boolean mOriginalEnable;
    /**
     * 是否刷新
     */
    private boolean mIsRefresh;

    /**
     * 压缩异步线程
     */
    ThreadUtils.BaseSimpleBaseTask<ArrayList<LocalFile>> mCompressFileTask;

    private ViewHolder mViewHolder;

    /**
     * @param marginBottom 底部间距
     */
    public static MatissFragment newInstance(int marginBottom) {
        MatissFragment matissFragment = new MatissFragment();
        Bundle args = new Bundle();
        matissFragment.setArguments(args);
        args.putInt(ARGUMENTS_MARGIN_BOTTOM, marginBottom);
        return matissFragment;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        this.mActivity = (AppCompatActivity) activity;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
        mSelectedCollection = new SelectedItemCollection(getContext());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matiss_zjh, container, false);
        this.view = view;
        mViewHolder = new ViewHolder(view);
        initConfig();
        initView(savedInstanceState);
        initListener();
        return view;
    }


    /**
     * 初始化配置
     */
    private void initConfig() {
        // 初始化设置
        mAlbumSpec = AlbumSpec.getInstance();
        mGlobalSpec = GlobalSpec.getInstance();
        // 设置图片路径
        if (mGlobalSpec.pictureStrategy != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mPictureMediaStoreCompat = new MediaStoreCompat(getContext(), mGlobalSpec.pictureStrategy);
        } else {
            // 否则使用全局的
            if (mGlobalSpec.saveStrategy == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mPictureMediaStoreCompat = new MediaStoreCompat(getContext(), mGlobalSpec.saveStrategy);
            }
        }
        mVideoMediaStoreCompat = new MediaStoreCompat(getContext(),
                mGlobalSpec.videoStrategy == null ? mGlobalSpec.saveStrategy : mGlobalSpec.videoStrategy);
    }

    /**
     * 初始化view
     */
    private void initView(Bundle savedInstanceState) {
        // 兼容沉倾状态栏
        ViewGroup.LayoutParams layoutParams = mViewHolder.toolbar.getLayoutParams();
        int statusBarHeight = StatusBarUtils.getStatusBarHeight(mContext);
        layoutParams.height = layoutParams.height + statusBarHeight;
        mViewHolder.toolbar.setLayoutParams(layoutParams);
        mViewHolder.toolbar.setPadding(mViewHolder.toolbar.getPaddingLeft(), statusBarHeight,
                mViewHolder.toolbar.getPaddingRight(), mViewHolder.toolbar.getPaddingBottom());
        Drawable navigationIcon = mViewHolder.toolbar.getNavigationIcon();
        TypedArray ta = mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.album_element_color});
        int color = ta.getColor(0, 0);
        ta.recycle();
        if (navigationIcon != null) {
            ColorFilterUtil.setColorFilterSrcIn(navigationIcon, color);
        }
        mSelectedCollection.onCreate(savedInstanceState, false);
        if (savedInstanceState != null) {
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE);
        }
        updateBottomToolbar();

        mAlbumsSpinnerAdapter = new AlbumsSpinnerAdapter(mContext, null, false);
        mAlbumsSpinner = new AlbumsSpinner(mContext);
        mAlbumsSpinner.setSelectedTextView(mViewHolder.selectedAlbum);
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
                onAlbumSelected(album);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 预览事件
        mViewHolder.buttonPreview.setOnClickListener(view -> {
            Intent intent = new Intent(mActivity, SelectedPreviewActivity.class);
            intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
            intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
            startActivityForResult(intent, mGlobalSpec.requestCode);
            if (mGlobalSpec.isCutscenes) {
                mActivity.overridePendingTransition(R.anim.activity_open, 0);
            }
        });

        // 确认当前选择的图片
        mViewHolder.buttonApply.setOnClickListener(view -> {
            ArrayList<LocalFile> localFiles = mSelectedCollection.asListOfLocalFile();
            setResultOkByIsCompress(localFiles);
        });

        // 点击原图
        mViewHolder.originalLayout.setOnClickListener(view -> {
            if (getFragmentManager() != null) {
                // 如果有大于限制大小的，就提示
                int count = countOverMaxSize();
                if (count > 0) {
                    IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                            getString(R.string.z_multi_library_error_over_original_count, count, mAlbumSpec.originalMaxSize));
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
        if (mCompressFileTask != null) {
            ThreadUtils.cancel(mCompressFileTask);
        }
//        mAlbumSpec.onCheckedListener = null;
//        mAlbumSpec.onSelectedListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //            onBackPressed(); // TODO
        return item.getItemId() == android.R.id.home || super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        // 请求的预览界面
        if (requestCode == mGlobalSpec.requestCode) {
            Bundle resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
            // 获取选择的数据
            ArrayList<MultiMedia> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
            // 是否启用原图
            mOriginalEnable = data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
            int collectionType = resultBundle.getInt(SelectedItemCollection.STATE_COLLECTION_TYPE,
                    SelectedItemCollection.COLLECTION_UNDEFINED);
            // 如果在预览界面点击了确定
            if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                if (selected != null) {
                    ArrayList<LocalFile> localFiles = new ArrayList<>(selected);
                    setResultOkByIsCompress(localFiles);
                }
            } else {
                // 点击了返回
                mSelectedCollection.overwrite(selected, collectionType);
                if (getFragmentManager() != null) {
                    Fragment mediaSelectionFragment = getFragmentManager().findFragmentByTag(
                            MediaSelectionFragment.class.getSimpleName());
                    if (mediaSelectionFragment instanceof MediaSelectionFragment) {
                        if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_IS_EDIT, false)) {
                            mIsRefresh = true;
                            albumsSpinnerNotifyData();
                            // 重新读取数据源
                            ((MediaSelectionFragment) mediaSelectionFragment).restartLoaderMediaGrid();
                        } else {
                            // 刷新数据源
                            ((MediaSelectionFragment) mediaSelectionFragment).refreshMediaGrid();
                        }
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
            mViewHolder.buttonPreview.setEnabled(false);
            mViewHolder.buttonApply.setEnabled(false);
            mViewHolder.buttonApply.setText(getString(R.string.z_multi_library_button_sure_default));
        } else if (selectedCount == 1 && mAlbumSpec.singleSelectionModeEnabled()) {
            // 不显示选择的数字
            mViewHolder.buttonPreview.setEnabled(true);
            mViewHolder.buttonApply.setText(R.string.z_multi_library_button_sure_default);
            mViewHolder.buttonApply.setEnabled(true);
        } else {
            // 显示选择的数字
            mViewHolder.buttonPreview.setEnabled(true);
            mViewHolder.buttonApply.setEnabled(true);
            mViewHolder.buttonApply.setText(getString(R.string.z_multi_library_button_sure, selectedCount));
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
                // 弹出窗口提示大于 xx mb
                IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                        getString(R.string.z_multi_library_error_over_original_size, mAlbumSpec.originalMaxSize));
                if (this.getFragmentManager() == null) {
                    return;
                }
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
                float size = PhotoMetadataUtils.getSizeInMb(item.getSize());

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

    public void albumsSpinnerNotifyData() {
        mAlbumCollection.mLoadFinished = false;
        mAlbumCollection.restartLoadAlbums();
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
            mViewHolder.emptyView.setVisibility(View.VISIBLE);
        } else {
            // 如果有数据，则内嵌新的fragment，并且相应相关照片
            mViewHolder.container.setVisibility(View.VISIBLE);
            mViewHolder.emptyView.setVisibility(View.GONE);
            if (!mIsRefresh) {
                assert getArguments() != null;
                if (mFragmentLast != null) {
                    // 在实例化新的之前，先清除旧的数据才可以查询
                    mFragmentLast.onDestroyData();
                }
                mFragmentLast = MediaSelectionFragment.newInstance(album, getArguments().getInt(ARGUMENTS_MARGIN_BOTTOM));
                mActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, mFragmentLast, MediaSelectionFragment.class.getSimpleName())
                        .commitAllowingStateLoss();
            }
        }
    }

    @Override
    public void onUpdate() {
        // notify bottom toolbar that check state changed.
        updateBottomToolbar();
        // 触发选择的接口事件
        if (mAlbumSpec.onSelectedListener != null) {
            mAlbumSpec.onSelectedListener.onSelected(mSelectedCollection.asListOfLocalFile());
        }
    }

    @Override
    public void onMediaClick(Album album, MultiMedia item, int adapterPosition) {
        Intent intent = new Intent(mActivity, AlbumPreviewActivity.class);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, album);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);
        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
        startActivityForResult(intent, mGlobalSpec.requestCode);
        if (mGlobalSpec.isCutscenes) {
            mActivity.overridePendingTransition(R.anim.activity_open, 0);
        }
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
            mViewHolder.bottomToolbar.setVisibility(View.VISIBLE);
            // 隐藏母窗体的table
            ((MainActivity) mActivity).showHideTableLayout(false);


            view.findViewById(R.id.flView).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        } else {
            // 显示底部
            mViewHolder.bottomToolbar.setVisibility(View.GONE);
            // 隐藏母窗体的table
            ((MainActivity) mActivity).showHideTableLayout(true);
        }
    }

    /**
     * 关闭Activity回调相关数值,如果需要压缩，另外弄一套压缩逻辑
     *
     * @param localFiles 本地数据包含别的参数
     */
    private void setResultOkByIsCompress(ArrayList<LocalFile> localFiles) {
        // 判断是否需要压缩
        if (mGlobalSpec.compressionInterface != null) {
            compressFile(localFiles);
        } else {
            setResultOk(localFiles);
        }
    }

    /**
     * 判断是否压缩，如果要压缩先要迁移复制再压缩
     */
    private void compressFile(ArrayList<LocalFile> localFiles) {
        // 显示loading动画

        // 复制相册的文件
        ThreadUtils.executeByIo(getCompressFileTask(localFiles));
    }

    /**
     * 压缩的异步线程
     *
     * @param localFiles 需要压缩的数据源
     */
    private ThreadUtils.BaseSimpleBaseTask<ArrayList<LocalFile>> getCompressFileTask(ArrayList<LocalFile> localFiles) {
        mCompressFileTask = new ThreadUtils.BaseSimpleBaseTask<ArrayList<LocalFile>>() {

            @Override
            public ArrayList<LocalFile> doInBackground() {
                // 将 缓存文件 拷贝到 配置目录
                ArrayList<LocalFile> newLocalFiles = new ArrayList<>();
                for (LocalFile item : localFiles) {
                    if (item.getPath() != null) {
                        File oldFile = new File(item.getPath());
                        // 压缩图片
                        File compressionFile;
                        if (mGlobalSpec.compressionInterface != null) {
                            try {
                                compressionFile = mGlobalSpec.compressionInterface.compressionFile(getContext(), oldFile);
                            } catch (IOException e) {
                                compressionFile = oldFile;
                                e.printStackTrace();
                            }
                        } else {
                            compressionFile = oldFile;
                        }
                        // new localFile
                        LocalFile localFile = new LocalFile();
                        localFile.setPath(compressionFile.getAbsolutePath());
                        localFile.setUri(mPictureMediaStoreCompat.getUri(compressionFile.getAbsolutePath()));
                        int[] imageWidthAndHeight = MediaStoreUtils.getImageWidthAndHeight(compressionFile.getAbsolutePath());
                        localFile.setWidth(imageWidthAndHeight[0]);
                        localFile.setHeight(imageWidthAndHeight[1]);
                        localFile.setSize(compressionFile.length());
                        localFile.setType(item.getType());
                        localFile.setMimeType(item.getMimeType());
                        localFile.setDuration(item.getDuration());
                        newLocalFiles.add(localFile);
                    }
                }
                return newLocalFiles;
            }

            @Override
            public void onSuccess(ArrayList<LocalFile> result) {
                setResultOk(result);
            }

        };
        return mCompressFileTask;
    }

    /**
     * 关闭Activity回调相关数值
     *
     * @param localFiles 本地数据包含别的参数
     */
    private void setResultOk(ArrayList<LocalFile> localFiles) {
        if (mGlobalSpec.onResultCallbackListener == null) {
            // 获取选择的图片的url集合
            Intent result = new Intent();
            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION_LOCAL_FILE, localFiles);
            // 是否启用原图
            result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
            mActivity.setResult(RESULT_OK, result);
            mActivity.finish();
        } else {
            mGlobalSpec.onResultCallbackListener.onResult(localFiles);
            mActivity.finish();
        }
    }

    public static class ViewHolder {
        public View rootView;
        public TextView selectedAlbum;
        public Toolbar toolbar;
        public TextView buttonPreview;
        public CheckRadioView original;
        public LinearLayout originalLayout;
        public TextView buttonApply;
        public FrameLayout bottomToolbar;
        public FrameLayout container;
        public TextView emptyViewContent;
        public FrameLayout emptyView;
        public RelativeLayout root;
        public ImageView imgClose;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.selectedAlbum = rootView.findViewById(R.id.selectedAlbum);
            this.toolbar = rootView.findViewById(R.id.toolbar);
            this.buttonPreview = rootView.findViewById(R.id.buttonPreview);
            this.original = rootView.findViewById(R.id.original);
            this.originalLayout = rootView.findViewById(R.id.originalLayout);
            this.buttonApply = rootView.findViewById(R.id.buttonApply);
            this.bottomToolbar = rootView.findViewById(R.id.bottomToolbar);
            this.container = rootView.findViewById(R.id.container);
            this.emptyViewContent = rootView.findViewById(R.id.emptyViewContent);
            this.emptyView = rootView.findViewById(R.id.emptyView);
            this.root = rootView.findViewById(R.id.root);
            this.imgClose = rootView.findViewById(R.id.imgClose);
        }

    }
}
