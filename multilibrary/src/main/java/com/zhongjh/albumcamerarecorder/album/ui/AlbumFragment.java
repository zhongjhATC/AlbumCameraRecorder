package com.zhongjh.albumcamerarecorder.album.ui;

import static android.app.Activity.RESULT_OK;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION_LOCAL_MEDIA;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.AppBarLayout;
import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.entity.Album2;
import com.zhongjh.albumcamerarecorder.album.listener.OnLoadPageMediaDataListener;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.MediaViewUtil;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.adapter.AlbumAdapter;
import com.zhongjh.albumcamerarecorder.album.utils.AlbumCompressFileTask;
import com.zhongjh.albumcamerarecorder.album.widget.CheckRadioView;
import com.zhongjh.albumcamerarecorder.album.widget.albumspinner.AlbumSpinner;
import com.zhongjh.albumcamerarecorder.album.widget.recyclerview.RecyclerLoadMoreView;
import com.zhongjh.albumcamerarecorder.model.MainModel;
import com.zhongjh.albumcamerarecorder.model.OriginalManage;
import com.zhongjh.albumcamerarecorder.model.SelectedModel;
import com.zhongjh.albumcamerarecorder.preview.start.PreviewStartManager;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.sharedanimation.RecycleItemViewParams;
import com.zhongjh.albumcamerarecorder.widget.ConstraintLayoutBehavior;
import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.common.listener.OnMoreClickListener;
import com.zhongjh.common.utils.ColorFilterUtil;
import com.zhongjh.common.utils.DisplayMetricsUtils;
import com.zhongjh.common.utils.DoubleUtils;
import com.zhongjh.common.utils.StatusBarUtils;
import com.zhongjh.common.utils.ThreadUtils;

import java.util.ArrayList;

/**
 * 相册,该Fragment主要处理 顶部的专辑上拉列表 和 底部的功能选项
 * 相册列表具体功能是在MediaViewUtil实现
 *
 * @author zhongjh
 * @date 2018/8/22
 */
public class AlbumFragment extends Fragment implements OnLoadPageMediaDataListener,
        AlbumAdapter.CheckStateListener, AlbumAdapter.OnMediaClickListener {

    private final String TAG = AlbumFragment.this.getClass().getSimpleName();

    private static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";
    public static final String ARGUMENTS_MARGIN_BOTTOM = "arguments_margin_bottom";

    private Context mContext;
    private MainModel mMainModel;
    private SelectedModel mSelectedModel;
    /**
     * 从预览界面回来
     */
    private ActivityResultLauncher<Intent> mPreviewActivityResult;
    /**
     * 公共配置
     */
    private final GlobalSpec mGlobalSpec = GlobalSpec.INSTANCE;

    /**
     * 相册配置
     */
    private final AlbumSpec mAlbumSpec = AlbumSpec.INSTANCE;

    /**
     * 统一管理原图有关功能模块
     */
    private OriginalManage mOriginalManage;

    /**
     * 专辑下拉框控件
     */
    private AlbumSpinner mAlbumSpinner;

    /**
     * 单独处理相册数据源的类
     */
    private MediaViewUtil mMediaViewUtil;

    /**
     * 是否刷新
     */
    private boolean mIsRefresh;

    /**
     * 压缩异步线程
     */
    private ThreadUtils.SimpleTask<ArrayList<LocalMedia>> mCompressFileTask;
    /**
     * 异步线程的逻辑
     */
    private AlbumCompressFileTask mAlbumCompressFileTask;

    private ViewHolder mViewHolder;

    /**
     * 当前点击item的索引
     */
    int currentPosition;

    /**
     * @param marginBottom 底部间距
     */
    public static AlbumFragment newInstance(int marginBottom) {
        AlbumFragment albumFragment = new AlbumFragment();
        Bundle args = new Bundle();
        albumFragment.setArguments(args);
        args.putInt(ARGUMENTS_MARGIN_BOTTOM, marginBottom);
        return albumFragment;
    }

    /**
     * 先执行onAttach生命周期再执行onCreateView
     *
     * @param context 上下文
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = requireActivity().getApplicationContext();
        this.mMainModel = new ViewModelProvider(requireActivity())
                .get(MainModel.class);
        this.mSelectedModel = new ViewModelProvider(requireActivity())
                .get(SelectedModel.class);
        if (!mAlbumSpec.getSelectedData().isEmpty()) {
            this.mSelectedModel.getSelectedData().addAll(mAlbumSpec.getSelectedData());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_zjh, container, false);

        mViewHolder = new ViewHolder(view);
        initConfig();
        mAlbumCompressFileTask = new AlbumCompressFileTask(requireActivity(), TAG, AlbumFragment.class, mGlobalSpec);
        initView(savedInstanceState);
        initActivityResult();
        initListener();
        initMediaViewUtil();
        initObserveData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBottomToolbar();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * 初始化配置
     */
    private void initConfig() {
        mOriginalManage = new OriginalManage(this, mMainModel, mSelectedModel, mAlbumSpec);
    }

    /**
     * 初始化view
     */
    private void initView(Bundle savedInstanceState) {
        // 兼容沉倾状态栏
        int statusBarHeight = StatusBarUtils.getStatusBarHeight(requireActivity());
        mViewHolder.root.setPadding(mViewHolder.root.getPaddingLeft(), statusBarHeight,
                mViewHolder.root.getPaddingRight(), mViewHolder.root.getPaddingBottom());
        // 修改颜色
        Drawable navigationIcon = mViewHolder.toolbar.getNavigationIcon();
        TypedArray ta = requireActivity().getTheme().obtainStyledAttributes(new int[]{R.attr.album_element_color});
        int color = ta.getColor(0, 0);
        ta.recycle();
        if (navigationIcon != null) {
            ColorFilterUtil.setColorFilterSrcIn(navigationIcon, color);
        }
        updateBottomToolbar();

        mAlbumSpinner = new AlbumSpinner(getActivity());
        mAlbumSpinner.setArrowImageView(mViewHolder.imgArrow);
        mAlbumSpinner.setTitleTextView(mViewHolder.tvAlbumTitle);

        // 获取专辑数据
        mMainModel.loadAllAlbum();

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
        mViewHolder.imgClose.setOnClickListener(v -> requireActivity().finish());

        // 下拉框选择的时候
        mAlbumSpinner.setOnAlbumItemClickListener((position, album) -> {
            // 设置缓存值
            mMainModel.setCurrentSelection(position);
            onAlbumSelected(album);
            mAlbumSpinner.dismiss();
        });

        // 预览事件
        mViewHolder.buttonPreview.setOnClickListener(new OnMoreClickListener() {
            @Override
            public void onListener(@NonNull View v) {
                PreviewStartManager.startPreviewActivityByAlbum(requireActivity(), mGlobalSpec.getCutscenesEnabled(), mPreviewActivityResult, mSelectedModel.getSelectedData().getLocalMedias());
            }
        });

        // 确认当前选择的图片
        mViewHolder.buttonApply.setOnClickListener(new OnMoreClickListener() {
            @Override
            public void onListener(@NonNull View v) {
                ArrayList<LocalMedia> localMediaArrayList = mSelectedModel.getSelectedData().getLocalMedias();
                // 设置是否原图状态
                for (LocalMedia localMedia : localMediaArrayList) {
                    localMedia.setOriginal(mMainModel.getOriginalEnable());
                }
                compressFile(localMediaArrayList);
            }
        });

        // 点击原图
        mViewHolder.originalLayout.setOnClickListener(view -> mOriginalManage.originalClick());

        // 点击Loading停止
        mViewHolder.pbLoading.setOnClickListener(v -> {
            // 中断线程
            mCompressFileTask.cancel();
            // 恢复界面可用
            setControlTouchEnable(true);
        });

        // 触发滑动事件
        mViewHolder.bottomToolbar.setOnListener(translationY -> ((MainActivity) requireActivity()).onDependentViewChanged(translationY));
    }

    /**
     * 初始化MediaViewUtil
     */
    private void initMediaViewUtil() {
        Log.d("onSaveInstanceState", " initMediaViewUtil");
        mMediaViewUtil = new MediaViewUtil(getActivity(), this, mMainModel, mSelectedModel, mViewHolder.recyclerview, this, this);
    }

    /**
     * 初始化数据的监控
     */
    private void initObserveData() {
        // 专辑加载完毕
        mMainModel.getAlbums().observe(getViewLifecycleOwner(), data -> {
            // 更新专辑列表
            mAlbumSpinner.bindFolder(data);
            // 可能因为别的原因销毁当前界面，回到当前选择的位置
            Album2 album = data.get(mMainModel.getCurrentSelection());
            ArrayList<Album2> albumChecks = new ArrayList<>();
            albumChecks.add(album);
            mAlbumSpinner.updateCheckStatus(albumChecks);
            String displayName = album.getName();
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
        });
        // 选择数据改变
        mSelectedModel.getSelectedDataChange().observe(getViewLifecycleOwner(), data -> mMediaViewUtil.notifyItemByLocalMedia());
        // 原图选项改变
        mMainModel.getOriginalEnableObserve().observe(getViewLifecycleOwner(), value -> mViewHolder.original.setChecked(value));
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
                    if (result.getData() != null) {
                        // 将PreviewActivity传递的数据继续传给上一个Activity
                        requireActivity().setResult(RESULT_OK, result.getData());
                    }
                    requireActivity().finish();
                });
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "AlbumFragment onDestroy");
        if (mGlobalSpec.isCompressEnable() && mGlobalSpec.getVideoCompressCoordinator() != null) {
            mGlobalSpec.getVideoCompressCoordinator().onCompressDestroy(AlbumFragment.this.getClass());
            mGlobalSpec.setVideoCompressCoordinator(null);
        }
        if (mCompressFileTask != null) {
            ThreadUtils.cancel(mCompressFileTask);
        }
        if (mMediaViewUtil != null) {
            mMediaViewUtil.onDestroyView();
        }
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
        int selectedCount = mSelectedModel.getSelectedData().count();

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
        mViewHolder.original.setChecked(mMainModel.getOriginalEnable());
        mOriginalManage.updateOriginalState();
    }

    /**
     * 选择某个专辑的时候
     *
     * @param album 专辑
     */
    private void onAlbumSelected(Album2 album) {
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
                    mViewHolder.tvAlbumTitle.setText(album.getName());
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
            mAlbumSpec.getOnSelectedListener().onSelected(mSelectedModel.getSelectedData().getLocalMedias());
        }
    }

    /**
     * 点击事件
     *
     * @param album           相册集合
     * @param imageView       图片View
     * @param item            选项
     * @param adapterPosition 索引
     */
    @Override
    public void onMediaClick(Album2 album, ImageView imageView, LocalMedia item, int adapterPosition) {
        if (DoubleUtils.isFastDoubleClick()) {
            return;
        }

        RecycleItemViewParams.add(mViewHolder.recyclerview, 0);

        currentPosition = adapterPosition;
        // 设置position
        mMainModel.setPreviewPosition(adapterPosition);

        PreviewStartManager.startPreviewFragmentByAlbum((MainActivity) requireActivity());
    }

    /**
     * 显示本身的底部
     * 隐藏母窗体的table
     * 以后如果有配置，就检查配置是否需要隐藏母窗体
     *
     * @param count 当前选择的数量
     */
    private void showBottomView(int count) {
        // 当前选择图片数量 > 0 并且 处于相册界面
        if (((MainActivity) requireActivity()).getMActivityMainZjhBinding().tableLayout.getCurrentTab() == 0) {
            if (count > 0) {
                // 显示底部
                mViewHolder.bottomToolbar.setVisibility(View.VISIBLE);
                // 隐藏母窗体的table
                ((MainActivity) requireActivity()).showHideTableLayout(false);
            } else {
                // 隐藏底部
                mViewHolder.bottomToolbar.setVisibility(View.GONE);
                // 显示母窗体的table
                ((MainActivity) requireActivity()).showHideTableLayout(true);
            }
        }
    }

    /**
     * 压缩文件开始
     *
     * @param localMediaArrayList 本地数据包含别的参数
     */
    private void compressFile(ArrayList<LocalMedia> localMediaArrayList) {
        // 显示loading动画
        setControlTouchEnable(false);

        // 复制相册的文件
        ThreadUtils.executeByIo(getCompressFileTask(localMediaArrayList));
    }

    /**
     * 完成压缩-复制的异步线程
     *
     * @param localMediaArrayList 需要压缩的数据源
     */
    private ThreadUtils.SimpleTask<ArrayList<LocalMedia>> getCompressFileTask(ArrayList<LocalMedia> localMediaArrayList) {
        mCompressFileTask = new ThreadUtils.SimpleTask<ArrayList<LocalMedia>>() {

            @Override
            public ArrayList<LocalMedia> doInBackground() {
                return mAlbumCompressFileTask.compressFileTaskDoInBackground(localMediaArrayList, false);
            }

            @Override
            public void onSuccess(ArrayList<LocalMedia> result) {
                setResultOk(result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                // 结束loading
                setControlTouchEnable(true);
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, t.getMessage(), t);
            }

            @Override
            public void onCancel() {
                super.onCancel();
                // 结束loading
                setControlTouchEnable(true);
            }
        };
        return mCompressFileTask;
    }

    /**
     * 关闭Activity回调相关数值
     *
     * @param localMediaArrayList 本地数据包含别的参数
     */
    private void setResultOk(ArrayList<LocalMedia> localMediaArrayList) {
        Log.d(TAG, "setResultOk");
        if (mGlobalSpec.getOnResultCallbackListener() == null) {
            // 获取选择的图片的url集合
            Intent result = new Intent();
            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION_LOCAL_MEDIA, localMediaArrayList);
            requireActivity().setResult(RESULT_OK, result);
        } else {
            mGlobalSpec.getOnResultCallbackListener().onResult(localMediaArrayList);
        }
        requireActivity().finish();
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

    @Override
    public void onLoadPageMediaDataComplete(ArrayList<LocalMedia> data, int currentPage, boolean isHasMore) {

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
        public RecyclerLoadMoreView recyclerview;

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
