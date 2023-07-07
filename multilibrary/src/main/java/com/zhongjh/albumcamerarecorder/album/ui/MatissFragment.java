package com.zhongjh.albumcamerarecorder.album.ui;


import static android.app.Activity.RESULT_OK;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION_LOCAL_FILE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.album.model.AlbumCollection;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.MediaViewUtil;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.adapter.AlbumMediaAdapter;
import com.zhongjh.albumcamerarecorder.album.utils.AlbumCompressFileTask;
import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.albumcamerarecorder.album.widget.CheckRadioView;
import com.zhongjh.albumcamerarecorder.album.widget.albumspinner.AlbumSpinner;
import com.zhongjh.albumcamerarecorder.preview.AlbumPreviewActivity;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.preview.SelectedPreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.widget.ConstraintLayoutBehavior;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.listener.OnMoreClickListener;
import com.zhongjh.common.utils.ColorFilterUtil;
import com.zhongjh.common.utils.DisplayMetricsUtils;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.StatusBarUtils;
import com.zhongjh.common.utils.ThreadUtils;
import com.zhongjh.common.widget.IncapableDialog;

import java.util.ArrayList;

/**
 * 相册,该Fragment主要处理 顶部的专辑上拉列表 和 底部的功能选项
 * 相册列表具体功能是在MediaViewUtil实现
 *
 * @author zhongjh
 * @date 2018/8/22
 */
public class MatissFragment extends Fragment implements AlbumCollection.AlbumCallbacks,
        MediaViewUtil.SelectionProvider,
        AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener {

    private final String TAG = MatissFragment.this.getClass().getSimpleName();

    private static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";
    public static final String ARGUMENTS_MARGIN_BOTTOM = "arguments_margin_bottom";

    private static final String CHECK_STATE = "checkState";

    private Context mContext;
    private MainActivity mActivity;
    /**
     * 从预览界面回来
     */
    private ActivityResultLauncher<Intent> mPreviewActivityResult;
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
    private AlbumSpinner mAlbumSpinner;

    /**
     * 单独处理相册数据源的类
     */
    private MediaViewUtil mMediaViewUtil;

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
    private ThreadUtils.SimpleTask<ArrayList<LocalFile>> mCompressFileTask;
    /**
     * 异步线程的逻辑
     */
    private AlbumCompressFileTask mAlbumCompressFileTask;
    /**
     * 专辑Cursor转换Album实体
     */
    private ThreadUtils.SimpleTask<ArrayList<Album>> mCursorToAlbum;

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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.mActivity = (MainActivity) context;
            this.mContext = context.getApplicationContext();
        }
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
        Log.d(TAG, "onSaveInstanceState onCreateView");
        View view = inflater.inflate(R.layout.fragment_matiss_zjh, container, false);
        this.view = view;
        mViewHolder = new ViewHolder(view);
        initConfig();
        mAlbumCompressFileTask = new AlbumCompressFileTask(mActivity, TAG, MatissFragment.class, mGlobalSpec, mPictureMediaStoreCompat, mVideoMediaStoreCompat);
        initView(savedInstanceState);
        initActivityResult();
        initListener();
        initMediaViewUtil();
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSelectedCollection.onSaveInstanceState(outState);
        mAlbumCollection.onSaveInstanceState(outState);
    }

    /**
     * 初始化配置
     */
    private void initConfig() {
        // 初始化设置
        mAlbumSpec = AlbumSpec.INSTANCE;
        mGlobalSpec = GlobalSpec.INSTANCE;

        // 设置图片路径
        if (mGlobalSpec.getPictureStrategy() != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mPictureMediaStoreCompat = new MediaStoreCompat(mActivity, mGlobalSpec.getPictureStrategy());
        } else {
            // 否则使用全局的
            if (mGlobalSpec.getSaveStrategy() == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mPictureMediaStoreCompat = new MediaStoreCompat(mActivity, mGlobalSpec.getSaveStrategy());
            }
        }

        // 设置视频路径
        if (mGlobalSpec.getVideoStrategy() != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mVideoMediaStoreCompat = new MediaStoreCompat(mActivity, mGlobalSpec.getVideoStrategy());
        } else {
            // 否则使用全局的
            if (mGlobalSpec.getSaveStrategy() == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mVideoMediaStoreCompat = new MediaStoreCompat(mActivity, mGlobalSpec.getSaveStrategy());
            }
        }
    }

    /**
     * 初始化view
     */
    private void initView(Bundle savedInstanceState) {
        // 兼容沉倾状态栏
        int statusBarHeight = StatusBarUtils.getStatusBarHeight(mActivity);
        mViewHolder.root.setPadding(mViewHolder.root.getPaddingLeft(), statusBarHeight,
                mViewHolder.root.getPaddingRight(), mViewHolder.root.getPaddingBottom());
        // 修改颜色
        Drawable navigationIcon = mViewHolder.toolbar.getNavigationIcon();
        TypedArray ta = mActivity.getTheme().obtainStyledAttributes(new int[]{R.attr.album_element_color});
        int color = ta.getColor(0, 0);
        ta.recycle();
        if (navigationIcon != null) {
            ColorFilterUtil.setColorFilterSrcIn(navigationIcon, color);
        }
        Log.d(TAG, "onSaveInstanceState initView");
        mSelectedCollection.onCreate(savedInstanceState, false);
        if (savedInstanceState != null) {
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE);
        }
        updateBottomToolbar();

        mAlbumSpinner = new AlbumSpinner(mActivity);
        mAlbumSpinner.setArrowImageView(mViewHolder.imgArrow);
        mAlbumSpinner.setTitleTextView(mViewHolder.tvAlbumTitle);

        mAlbumCollection.onCreate(getActivity(), this);
        mAlbumCollection.onRestoreInstanceState(savedInstanceState);
        mAlbumCollection.loadAlbums();

        // 关闭滑动隐藏布局功能
        if (!mAlbumSpec.getSlidingHiddenEnable()) {
            mViewHolder.recyclerview.setNestedScrollingEnabled(false);
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mViewHolder.toolbar.getLayoutParams();
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
            mViewHolder.emptyView.setPadding(0, 0, 0, DisplayMetricsUtils.dip2px(50));
            mViewHolder.recyclerview.setPadding(0, 0, 0, DisplayMetricsUtils.dip2px(50));
        }
    }

    /**
     * 初始化事件
     */
    private void initListener() {
        // 关闭事件
        mViewHolder.imgClose.setOnClickListener(v -> mActivity.finish());

        // 下拉框选择的时候
        mAlbumSpinner.setOnAlbumItemClickListener((position, album) -> {
            // 设置缓存值
            mAlbumCollection.setStateCurrentSelection(position);
            onAlbumSelected(album);
            mAlbumSpinner.dismiss();
        });

        // 预览事件
        mViewHolder.buttonPreview.setOnClickListener(new OnMoreClickListener() {
            @Override
            public void onListener(@NonNull View v) {
                Intent intent = new Intent(mActivity, SelectedPreviewActivity.class);
                intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
                intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
                intent.putExtra(BasePreviewActivity.IS_BY_ALBUM, true);
                mPreviewActivityResult.launch(intent);
                if (mGlobalSpec.getCutscenesEnabled()) {
                    mActivity.overridePendingTransition(R.anim.activity_open_zjh, 0);
                }
            }
        });

        // 确认当前选择的图片
        mViewHolder.buttonApply.setOnClickListener(new OnMoreClickListener() {
            @Override
            public void onListener(@NonNull View v) {
                ArrayList<LocalFile> localFiles = mSelectedCollection.asListOfLocalFile();
                // 设置是否原图状态
                for (LocalFile localFile : localFiles) {
                    localFile.setOriginal(mOriginalEnable);
                }
                compressFile(localFiles);
            }
        });

        // 点击原图
        mViewHolder.originalLayout.setOnClickListener(view -> {
            // 如果有大于限制大小的，就提示
            int count = countOverMaxSize();
            if (count > 0) {
                IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                        getString(R.string.z_multi_library_error_over_original_count, count, mAlbumSpec.getOriginalMaxSize()));
                incapableDialog.show(getChildFragmentManager(),
                        IncapableDialog.class.getName());
                return;
            }

            // 设置状态
            mOriginalEnable = !mOriginalEnable;
            mViewHolder.original.setChecked(mOriginalEnable);

            // 设置状态是否原图
            if (mAlbumSpec.getOnCheckedListener() != null) {
                mAlbumSpec.getOnCheckedListener().onCheck(mOriginalEnable);
            }
        });

        // 点击Loading停止
        mViewHolder.pbLoading.setOnClickListener(v -> {
            // 中断线程
            mCompressFileTask.cancel();
            // 恢复界面可用
            setControlTouchEnable(true);
        });

        // 触发滑动事件
        mViewHolder.bottomToolbar.setOnListener(translationY -> mActivity.onDependentViewChanged(translationY));
    }

    /**
     * 初始化MediaViewUtil
     */
    private void initMediaViewUtil() {
        Log.d("onSaveInstanceState", " initMediaViewUtil");
        mMediaViewUtil = new MediaViewUtil(getActivity(), mViewHolder.recyclerview, this, this, this);
    }

    /**
     * 初始化Activity的返回
     */
    private void initActivityResult() {
        mPreviewActivityResult = this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != RESULT_OK) {
                        return;
                    }
                    // 请求的预览界面
                    if (result.getData() != null) {
                        Bundle resultBundle = result.getData().getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
                        // 获取选择的数据
                        ArrayList<MultiMedia> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
                        // 是否启用原图
                        mOriginalEnable = result.getData().getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
                        int collectionType = resultBundle.getInt(SelectedItemCollection.STATE_COLLECTION_TYPE,
                                SelectedItemCollection.COLLECTION_UNDEFINED);
                        // 如果在预览界面点击了确定
                        if (result.getData().getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                            if (selected != null) {
                                ArrayList<LocalFile> localFiles = new ArrayList<>(selected);
                                // 不用处理压缩，压缩处理已经在预览界面处理了
                                setResultOk(localFiles);
                            }
                        } else {
                            // 点击了返回
                            mSelectedCollection.overwrite(selected, collectionType);
                            if (result.getData().getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_IS_EDIT, false)) {
                                mIsRefresh = true;
                                albumsSpinnerNotifyData();
                                // 重新读取数据源
                                mMediaViewUtil.restartLoaderMediaGrid();
                            } else {
                                // 刷新数据源
                                mMediaViewUtil.refreshMediaGrid();
                            }
                            // 刷新底部
                            updateBottomToolbar();
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "MatissFragment onDestroy");
        if (mGlobalSpec.isCompressEnable() && mGlobalSpec.getVideoCompressCoordinator() != null) {
            mGlobalSpec.getVideoCompressCoordinator().onCompressDestroy(MatissFragment.this.getClass());
            mGlobalSpec.setVideoCompressCoordinator(null);
        }
        // 销毁相册model
        mAlbumCollection.onDestroy();
        if (mCompressFileTask != null) {
            ThreadUtils.cancel(mCompressFileTask);
        }
        mMediaViewUtil.onDestroyView();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == android.R.id.home || super.onOptionsItemSelected(item);
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
        if (mAlbumSpec.getOriginalEnable()) {
            mViewHolder.groupOriginal.setVisibility(View.VISIBLE);
            updateOriginalState();
        } else {
            mViewHolder.groupOriginal.setVisibility(View.INVISIBLE);
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
                        getString(R.string.z_multi_library_error_over_original_size, mAlbumSpec.getOriginalMaxSize()));
                incapableDialog.show(this.getChildFragmentManager(),
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

                if (size > mAlbumSpec.getOriginalMaxSize()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public void onAlbumLoadFinished(final Cursor cursor) {
        if (mCursorToAlbum != null) {
            // 取消当前的线程，重新运行
            mCursorToAlbum.cancel();
        }
        mCursorToAlbum = new ThreadUtils.SimpleTask<ArrayList<Album>>() {
            @Override
            public ArrayList<Album> doInBackground() {
                ArrayList<Album> items = new ArrayList<>();
                while (cursor.moveToNext()) {
                    items.add(Album.valueOf(cursor));
                }
                cursor.close();
                return items;
            }

            @Override
            public void onSuccess(ArrayList<Album> result) {
                // 更新专辑列表
                mAlbumSpinner.bindFolder(result);
                // 可能因为别的原因销毁当前界面，回到当前选择的位置
                Album album = result.get(mAlbumCollection.getCurrentSelection());
                ArrayList<Album> albumChecks = new ArrayList<>();
                albumChecks.add(album);
                mAlbumSpinner.updateCheckStatus(albumChecks);
                String displayName = album.getDisplayName(mContext);
                if (mViewHolder.tvAlbumTitle.getVisibility() == View.VISIBLE) {
                    mViewHolder.tvAlbumTitle.setText(displayName);
                } else {
                    mViewHolder.tvAlbumTitle.setAlpha(0.0f);
                    mViewHolder.tvAlbumTitle.setVisibility(View.VISIBLE);
                    mViewHolder.tvAlbumTitle.setText(displayName);
                    mViewHolder.tvAlbumTitle.animate().alpha(1.0f).setDuration(mContext.getResources().getInteger(
                            android.R.integer.config_longAnimTime)).start();
                }
                onAlbumSelected(album);
            }
        };
        ThreadUtils.executeByIo(mCursorToAlbum);
    }

    @Override
    public void onAlbumReset() {
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
            mViewHolder.recyclerview.setVisibility(View.GONE);
            mViewHolder.emptyView.setVisibility(View.VISIBLE);
        } else {
            // 如果有数据，显示相应相关照片
            mViewHolder.recyclerview.setVisibility(View.VISIBLE);
            mViewHolder.emptyView.setVisibility(View.GONE);
            if (!mIsRefresh) {
                if (mMediaViewUtil != null) {
                    mMediaViewUtil.load(album);
                    mViewHolder.tvAlbumTitle.setText(album.getDisplayName(mContext));
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        // notify bottom toolbar that check state changed.
        updateBottomToolbar();
        // 触发选择的接口事件
        if (mAlbumSpec.getOnSelectedListener() != null) {
            mAlbumSpec.getOnSelectedListener().onSelected(mSelectedCollection.asListOfLocalFile());
        } else {
            // 如果没有触发选择，也照样触发该事件赋值path
            mSelectedCollection.updatePath();
        }
    }

    @Override
    public void onMediaClick(Album album, MultiMedia item, int adapterPosition) {
        Intent intent = new Intent(mActivity, AlbumPreviewActivity.class);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, album);
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);
        intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle());
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
        intent.putExtra(BasePreviewActivity.IS_BY_ALBUM, true);
        mPreviewActivityResult.launch(intent);
        if (mGlobalSpec.getCutscenesEnabled()) {
            mActivity.overridePendingTransition(R.anim.activity_open_zjh, 0);
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
            mActivity.showHideTableLayout(false);
        } else {
            // 显示底部
            mViewHolder.bottomToolbar.setVisibility(View.GONE);
            // 隐藏母窗体的table
            mActivity.showHideTableLayout(true);
        }
    }

    /**
     * 压缩文件开始
     *
     * @param localFiles 本地数据包含别的参数
     */
    private void compressFile(ArrayList<LocalFile> localFiles) {
        // 显示loading动画
        setControlTouchEnable(false);

        // 复制相册的文件
        ThreadUtils.executeByIo(getCompressFileTask(localFiles));
    }

    /**
     * 完成压缩-复制的异步线程
     *
     * @param localFiles 需要压缩的数据源
     */
    private ThreadUtils.SimpleTask<ArrayList<LocalFile>> getCompressFileTask(ArrayList<LocalFile> localFiles) {
        mCompressFileTask = new ThreadUtils.SimpleTask<ArrayList<LocalFile>>() {

            @Override
            public ArrayList<LocalFile> doInBackground() {
                return mAlbumCompressFileTask.compressFileTaskDoInBackground(localFiles);
            }

            @Override
            public void onSuccess(ArrayList<LocalFile> result) {
                setResultOk(result);
            }

            @Override
            public void onFail(Throwable t) {
                // 结束loading
                setControlTouchEnable(true);
                Toast.makeText(mActivity.getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                super.onFail(t);
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
        Log.d(TAG, "setResultOk");
        if (mGlobalSpec.getOnResultCallbackListener() == null) {
            // 获取选择的图片的url集合
            Intent result = new Intent();
            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION_LOCAL_FILE, localFiles);
            // 是否启用原图
            result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
            mActivity.setResult(RESULT_OK, result);
        } else {
            mGlobalSpec.getOnResultCallbackListener().onResult(localFiles);
        }
        mActivity.finish();
    }

    /**
     * 设置是否启用界面触摸，不可禁止中断、退出
     */
    private void setControlTouchEnable(boolean enable) {
        mViewHolder.recyclerview.setEnabled(enable);
        // 如果不可用就显示 加载中 view,否则隐藏
        if (!enable) {
            mViewHolder.pbLoading.setVisibility(View.VISIBLE);
            mViewHolder.buttonApply.setVisibility(View.GONE);
            mViewHolder.buttonPreview.setEnabled(false);
        } else {
            mViewHolder.pbLoading.setVisibility(View.GONE);
            mViewHolder.buttonApply.setVisibility(View.VISIBLE);
            mViewHolder.buttonPreview.setEnabled(true);
        }
    }

    public static class ViewHolder {
        public View rootView;
        public View selectedAlbum;
        public TextView tvAlbumTitle;
        public ImageView imgArrow;
        public Toolbar toolbar;
        public TextView buttonPreview;
        public CheckRadioView original;
        public View originalLayout;
        public Group groupOriginal;
        public TextView buttonApply;
        public ConstraintLayoutBehavior bottomToolbar;
        public TextView emptyViewContent;
        public FrameLayout emptyView;
        public CoordinatorLayout root;
        public ImageView imgClose;
        public ProgressBar pbLoading;
        public RecyclerView recyclerview;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.selectedAlbum = rootView.findViewById(R.id.selectedAlbum);
            this.tvAlbumTitle = rootView.findViewById(R.id.tvAlbumTitle);
            this.imgArrow = rootView.findViewById(R.id.imgArrow);
            this.toolbar = rootView.findViewById(R.id.toolbar);
            this.buttonPreview = rootView.findViewById(R.id.buttonPreview);
            this.original = rootView.findViewById(R.id.original);
            this.originalLayout = rootView.findViewById(R.id.originalLayout);
            this.groupOriginal = rootView.findViewById(R.id.groupOriginal);
            this.buttonApply = rootView.findViewById(R.id.buttonApply);
            this.bottomToolbar = rootView.findViewById(R.id.bottomToolbar);
            this.emptyViewContent = rootView.findViewById(R.id.emptyViewContent);
            this.emptyView = rootView.findViewById(R.id.emptyView);
            this.root = rootView.findViewById(R.id.root);
            this.imgClose = rootView.findViewById(R.id.imgClose);
            this.pbLoading = rootView.findViewById(R.id.pbLoading);
            this.recyclerview = rootView.findViewById(R.id.recyclerview);
        }

    }
}
