package com.zhongjh.albumcamerarecorder.preview.base;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.model.MainModel;
import com.zhongjh.albumcamerarecorder.album.utils.AlbumCompressFileTask;
import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.albumcamerarecorder.album.widget.CheckRadioView;
import com.zhongjh.albumcamerarecorder.album.widget.CheckView;
import com.zhongjh.albumcamerarecorder.preview.adapter.PreviewPagerAdapter;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.common.listener.OnMoreClickListener;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.StatusBarUtils;
import com.zhongjh.common.utils.ThreadUtils;
import com.zhongjh.common.widget.IncapableDialog;

import java.io.File;

/**
 * 预览窗口的基类
 *
 * @author zhongjh
 * @date 2022/8/29
 */
public class BasePreviewFragment extends Fragment implements View.OnClickListener {

    private final String TAG = BasePreviewFragment.this.getClass().getSimpleName();
    public static final String EXTRA_IS_ALLOW_REPEAT = "extra_is_allow_repeat";
    /**
     * 选择的数据源集合和类型
     */
    public static final String EXTRA_DEFAULT_BUNDLE = "extra_default_bundle";
    public static final String EXTRA_RESULT_APPLY = "extra_result_apply";
    public static final String EXTRA_RESULT_IS_EDIT = "extra_result_is_edit";
    /**
     * 设置是否开启原图
     */
    public static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";
    /**
     * 设置是否启动确定功能
     */
    public static final String APPLY_ENABLE = "apply_enable";
    /**
     * 设置是否启动选择功能
     */
    public static final String SELECTED_ENABLE = "selected_enable";
    /**
     * 设置是否开启编辑功能
     */
    public static final String EDIT_ENABLE = "edit_enable";
    /**
     * 设置是否开启压缩
     */
    public static final String COMPRESS_ENABLE = "compress_enable";
    public static final String CHECK_STATE = "checkState";
    public static final String IS_SELECTED_LISTENER = "is_selected_listener";
    public static final String IS_SELECTED_CHECK = "is_selected_check";
    public static final String IS_EXTERNAL_USERS = "is_external_users";

    protected FragmentActivity mActivity;
    protected Context mContext;
    protected ViewHolder mViewHolder;
    protected PreviewPagerAdapter mAdapter;
    protected GlobalSpec mGlobalSpec;
    protected AlbumSpec mAlbumSpec;
    /**
     * 图片存储器
     */
    private MediaStoreCompat mPictureMediaStoreCompat;
    /**
     * 录像文件配置路径
     */
    private MediaStoreCompat mVideoMediaStoreCompat;
    /**
     * 完成压缩-复制的异步线程
     */
    ThreadUtils.SimpleTask<Void> mCompressFileTask;
    /**
     * 完成迁移文件的异步线程
     */
    ThreadUtils.SimpleTask<Void> mMoveFileTask;
    /**
     * 异步线程的逻辑
     */
    private AlbumCompressFileTask mAlbumCompressFileTask;
    /**
     * 打开ImageEditActivity的回调
     */
    ActivityResultLauncher<Intent> mImageEditActivityResult;
    /**
     * 当前编辑完的图片文件
     */
    private File mEditImageFile;
    /**
     * 当前预览的图片的索引,默认第一个
     */
    protected int mPreviousPos = -1;
    /**
     * 是否启动原图
     */
    protected boolean mOriginalEnable;
    /**
     * 设置是否启动确定功能
     */
    protected boolean mApplyEnable = true;
    /**
     * 设置是否启动选择功能
     */
    protected boolean mSelectedEnable = true;
    /**
     * 设置是否开启编辑功能
     */
    protected boolean mEditEnable = true;
    /**
     * 设置是否开启压缩
     */
    protected boolean mCompressEnable = false;
    /**
     * 是否编辑了图片
     */
    private boolean mIsEdit;
    /**
     * 是否触发选择事件，目前除了相册功能没问题之外，别的触发都会闪退，原因是uri不是通过数据库而获得的
     */
    protected boolean mIsSelectedListener = true;
    /**
     * 设置右上角是否检测类型
     */
    protected boolean mIsSelectedCheck = true;
    /**
     * 是否外部直接调用该预览窗口，如果是外部直接调用，那么可以启用回调接口，内部统一使用onActivityResult方式回调
     */
    protected boolean mIsExternalUsers = false;
    protected MainModel mMainModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context.getApplicationContext();
        if (context instanceof Activity) {
            mActivity = (FragmentActivity) context;
        }
        this.mMainModel = new ViewModelProvider(requireActivity())
                .get(MainModel.class);
        // 拦截OnBackPressed
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResultOkByIsCompress(false);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 获取配置
        mGlobalSpec = GlobalSpec.INSTANCE;
        mAlbumSpec = AlbumSpec.INSTANCE;
        // 获取样式
        final ContextThemeWrapper wrapper = new ContextThemeWrapper(requireActivity(),
                mGlobalSpec.getThemeId());
        final LayoutInflater cloneInContext = inflater.cloneInContext(wrapper);
        View view = cloneInContext.inflate(R.layout.activity_media_preview_zjh, container, false);
        onActivityResult();
        StatusBarUtils.initStatusBar(mActivity);
        boolean isAllowRepeat = false;
        if (getArguments() != null) {
            isAllowRepeat = getArguments().getBoolean(EXTRA_IS_ALLOW_REPEAT, false);
            mApplyEnable = getArguments().getBoolean(APPLY_ENABLE, true);
            mSelectedEnable = getArguments().getBoolean(SELECTED_ENABLE, true);
            mIsSelectedListener = getArguments().getBoolean(IS_SELECTED_LISTENER, true);
            mIsSelectedCheck = getArguments().getBoolean(IS_SELECTED_CHECK, true);
            mIsExternalUsers = getArguments().getBoolean(IS_EXTERNAL_USERS, false);
            mCompressEnable = getArguments().getBoolean(COMPRESS_ENABLE, false);
            mEditEnable = getArguments().getBoolean(EDIT_ENABLE, true);
        }

        // 设置图片路径
        if (mGlobalSpec.getPictureStrategy() != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mPictureMediaStoreCompat = new MediaStoreCompat(mContext, mGlobalSpec.getPictureStrategy());
        } else {
            // 否则使用全局的
            if (mGlobalSpec.getSaveStrategy() == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mPictureMediaStoreCompat = new MediaStoreCompat(mContext, mGlobalSpec.getSaveStrategy());
            }
        }
        // 设置视频路径
        if (mGlobalSpec.getVideoStrategy() != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mVideoMediaStoreCompat = new MediaStoreCompat(mContext, mGlobalSpec.getVideoStrategy());
        } else {
            // 否则使用全局的
            if (mGlobalSpec.getSaveStrategy() == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mVideoMediaStoreCompat = new MediaStoreCompat(mContext, mGlobalSpec.getSaveStrategy());
            }
        }
        if (savedInstanceState == null) {
            // 初始化别的界面传递过来的数据
            mOriginalEnable = getArguments().getBoolean(EXTRA_RESULT_ORIGINAL_ENABLE, false);
        } else {
            // 初始化缓存的数据
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE);
        }

        mViewHolder = new ViewHolder(view);

        mAdapter = new PreviewPagerAdapter(mContext, mActivity);
        mViewHolder.pager.setAdapter(mAdapter);
        mViewHolder.checkView.setCountable(mAlbumSpec.getCountable());

        mAlbumCompressFileTask = new AlbumCompressFileTask(mContext, TAG,
                BasePreviewFragment.class, mGlobalSpec, mPictureMediaStoreCompat, mVideoMediaStoreCompat);

        initListener();
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mActivity = null;
    }

    /**
     * 针对回调
     */
    private void onActivityResult() {
        mImageEditActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                mIsEdit = true;
                refreshMultiMediaItem();
            }
        });
    }

    /**
     * 刷新MultiMedia
     */
    private void refreshMultiMediaItem() {
//        // 获取当前查看的multimedia
//        LocalMedia localMedia = mAdapter.getMediaItem(mViewHolder.pager.getCurrentItem());
//        // 获取编辑前的uri
//        Uri oldUri = localMedia.getUri();
//        // 获取编辑后的uri
//        Uri newUri = mPictureMediaStoreCompat.getUri(mEditImageFile.getPath());
//        // 获取编辑前的path
//        String oldPath = null;
//        if (multiMedia.getPath() == null) {
//            File file = UriUtils.uriToFile(mContext, multiMedia.getUri());
//            if (file != null) {
//                oldPath = UriUtils.uriToFile(mContext, multiMedia.getUri()).getAbsolutePath();
//            }
//        } else {
//            oldPath = multiMedia.getPath();
//        }
//        multiMedia.setOldPath(oldPath);
//        // 获取编辑后的path
//        String newPath = mEditImageFile.getPath();
//        // 赋值新旧的path、uri
//        multiMedia.handleEditValue(newPath, newUri, oldPath, oldUri);
//        // 更新当前fragment编辑后的uri和path
//        mAdapter.setMediaItem(mViewHolder.pager.getCurrentItem(), multiMedia);
//        mAdapter.notifyItemChanged(mViewHolder.pager.getCurrentItem());
//
//        // 判断是否跟mSelectedCollection的数据一样，因为通过点击相册预览进来的数据 是共用的，但是如果通过相册某个item点击进来是重新new的数据，如果是重新new的数据要赋值多一个
//        // 如何重现进入这个条件里面：先相册选择第一个，然后点击相册第二个item进入详情，在详情界面滑动到第一个，对第一个进行编辑改动，则会进入这些条件里面
//        for (LocalMedia item : mMainModel.getSelectedData().getLocalMedias()) {
//            if (item.getId() == multiMedia.getId()) {
//                // 如果两个id都一样，那就是同个图片，再判断是否同个对象
//                if (!item.equals(multiMedia)) {
//                    // 如果不是同个对象，那么另外一个对象要赋值
//                    item.handleEditValue(multiMedia.getPath());
//                }
//            }
//        }

    }

    /**
     * 所有事件
     */
    private void initListener() {
        // 编辑
        mViewHolder.tvEdit.setOnClickListener(new OnMoreClickListener() {
            @Override
            public void onListener(@NonNull View v) {
                openImageEditActivity();
            }
        });
        // 返回
        mViewHolder.iBtnBack.setOnClickListener(this);
        // 确认
        mViewHolder.buttonApply.setOnClickListener(new OnMoreClickListener() {
            @Override
            public void onListener(@NonNull View v) {
//                // 确认的一刻赋值
//                ArrayList<LocalMedia> localMediaArrayList = mMainModel.getSelectedData().getLocalMedias();
//                // 设置是否原图状态
//                for (LocalMedia localMedia : localMediaArrayList) {
//                    localMedia.setOriginal(mOriginalEnable);
//                }
//                setResultOkByIsCompress(true);
            }
        });
        // 多图时滑动事件
        mViewHolder.pager.registerOnPageChangeCallback(mOnPageChangeCallback);
        // 右上角选择事件
        mViewHolder.checkView.setOnClickListener(this);
        // 点击原图事件
        mViewHolder.originalLayout.setOnClickListener(v -> {
            int count = countOverMaxSize();
            if (count > 0) {
                IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                        getString(R.string.z_multi_library_error_over_original_count, count, mAlbumSpec.getOriginalMaxSize()));
                incapableDialog.show(getParentFragmentManager(),
                        IncapableDialog.class.getName());
                return;
            }

            mOriginalEnable = !mOriginalEnable;
            mViewHolder.original.setChecked(mOriginalEnable);
            if (!mOriginalEnable) {
                mViewHolder.original.setColor(Color.WHITE);
            }

            if (mAlbumSpec.getOnCheckedListener() != null) {
                mAlbumSpec.getOnCheckedListener().onCheck(mOriginalEnable);
            }
        });
        // 点击Loading停止
        mViewHolder.pbLoading.setOnClickListener(v -> {
            // 中断线程
            if (mCompressFileTask != null) {
                mCompressFileTask.cancel();
            }
            // 恢复界面可用
            setControlTouchEnable(true);
        });

        updateApplyButton();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("checkState", mOriginalEnable);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (mCompressFileTask != null) {
            ThreadUtils.cancel(mCompressFileTask);
        }
        mViewHolder.pager.unregisterOnPageChangeCallback(mOnPageChangeCallback);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ibtnBack) {
            mActivity.onBackPressed();
        } else if (v.getId() == R.id.checkView) {
//            MultiMedia item = mAdapter.getMediaItem(mViewHolder.pager.getCurrentItem());
//            if (mSelectedCollection.isSelected(item)) {
//                mSelectedCollection.remove(item);
//                if (mAlbumSpec.getCountable()) {
//                    mViewHolder.checkView.setCheckedNum(CheckView.UNCHECKED);
//                } else {
//                    mViewHolder.checkView.setChecked(false);
//                }
//            } else {
//                boolean isTrue = true;
//                if (mIsSelectedCheck) {
//                    isTrue = assertAddSelection(item);
//                }
//                if (isTrue) {
//                    mSelectedCollection.add(item);
//                    if (mAlbumSpec.getCountable()) {
//                        mViewHolder.checkView.setCheckedNum(mSelectedCollection.checkedNumOf(item));
//                    } else {
//                        mViewHolder.checkView.setChecked(true);
//                    }
//                }
//            }
//            updateApplyButton();
//
//            if (mAlbumSpec.getOnSelectedListener() != null && mIsSelectedListener) {
//                // 触发选择的接口事件
//                mAlbumSpec.getOnSelectedListener().onSelected(mSelectedCollection.asListOfLocalFile());
//            } else {
//                mSelectedCollection.updatePath();
//            }
        }
    }

    /**
     * 滑动事件
     */
    private final ViewPager2.OnPageChangeCallback mOnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            PreviewPagerAdapter adapter = (PreviewPagerAdapter) mViewHolder.pager.getAdapter();
            if (adapter == null) {
                return;
            }
            if (mPreviousPos != -1 && mPreviousPos != position) {
//                MultiMedia item = adapter.getMediaItem(position);
//                if (mAlbumSpec.getCountable()) {
//                    int checkedNum = mSelectedCollection.checkedNumOf(item);
//                    mViewHolder.checkView.setCheckedNum(checkedNum);
//                    if (checkedNum > 0) {
//                        setCheckViewEnable(true);
//                    } else {
//                        setCheckViewEnable(!mSelectedCollection.maxSelectableReached());
//                    }
//                } else {
//                    boolean checked = mSelectedCollection.isSelected(item);
//                    mViewHolder.checkView.setChecked(checked);
//                    if (checked) {
//                        setCheckViewEnable(true);
//                    } else {
//                        setCheckViewEnable(!mSelectedCollection.maxSelectableReached());
//                    }
//                }
//                updateUi(item);
            }
            mPreviousPos = position;
        }
    };

    /**
     * 更新确定按钮状态
     */
    private void updateApplyButton() {
//        // 获取已选的图片
//        int selectedCount = mSelectedCollection.count();
//        if (selectedCount == 0) {
//            // 禁用
//            mViewHolder.buttonApply.setText(R.string.z_multi_library_button_sure_default);
//            mViewHolder.buttonApply.setEnabled(false);
//        } else if (selectedCount == 1 && mAlbumSpec.singleSelectionModeEnabled()) {
//            // 如果只选择一张或者配置只能选一张，或者不显示数字的时候。启用，不显示数字
//            mViewHolder.buttonApply.setText(R.string.z_multi_library_button_sure_default);
//            mViewHolder.buttonApply.setEnabled(true);
//        } else {
//            // 启用，显示数字
//            mViewHolder.buttonApply.setEnabled(true);
//            mViewHolder.buttonApply.setText(getString(R.string.z_multi_library_button_sure, selectedCount));
//        }
//
//        // 判断是否启动操作
//        if (!mApplyEnable) {
//            mViewHolder.buttonApply.setVisibility(View.GONE);
//        } else {
//            mViewHolder.buttonApply.setVisibility(View.VISIBLE);
//        }
//        setCheckViewEnable(mSelectedEnable);
    }

    /**
     * 更新原图按钮状态
     */
    private void updateOriginalState() {
        // 设置原图按钮根据配置来
        mViewHolder.original.setChecked(mOriginalEnable);
        if (!mOriginalEnable) {
            mViewHolder.original.setColor(Color.WHITE);
        }

        if (countOverMaxSize() > 0) {
            // 如果开启了原图功能
            if (mOriginalEnable) {
                // 弹框提示取消原图
                IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                        getString(R.string.z_multi_library_error_over_original_size, mAlbumSpec.getOriginalMaxSize()));
                incapableDialog.show(getParentFragmentManager(),
                        IncapableDialog.class.getName());
                // 去掉原图按钮的选择状态
                mViewHolder.original.setChecked(false);
                mViewHolder.original.setColor(Color.WHITE);
                mOriginalEnable = false;
            }
        }
    }

    /**
     * 获取当前超过限制原图大小的数量
     *
     * @return 数量
     */
    private int countOverMaxSize() {
        int count = 0;
//        int selectedCount = mMainModel.getSelectedData().count();
//        for (int i = 0; i < selectedCount; i++) {
//            LocalMedia item = mMainModel.getSelectedData().getLocalMedias().get(i);
//            if (item.isImage()) {
//                float size = PhotoMetadataUtils.getSizeInMb(item.getSize());
//                if (size > mAlbumSpec.getOriginalMaxSize()) {
//                    count++;
//                }
//            }
//        }
        return count;
    }

    /**
     * 更新ui
     * 如果当前item是gif就显示多少M的文本
     * 如果当前item是video就显示播放按钮
     *
     * @param item 当前图片
     */
    @SuppressLint("SetTextI18n")
    protected void updateUi(LocalMedia item) {
        if (item.isGif()) {
            mViewHolder.size.setVisibility(View.VISIBLE);
            mViewHolder.size.setText(PhotoMetadataUtils.getSizeInMb(item.getSize()) + "M");
        } else {
            mViewHolder.size.setVisibility(View.GONE);
        }

        // 判断是否开启原图,并且是从相册界面进来才开启原图，同时原图不支持video
        if (mAlbumSpec.getOriginalEnable() && mOriginalEnable && !item.isVideo()) {
            // 显示
            mViewHolder.originalLayout.setVisibility(View.VISIBLE);
            updateOriginalState();
        } else {
            // 隐藏
            mViewHolder.originalLayout.setVisibility(View.GONE);
        }

        if (item.isImage() && mGlobalSpec.getImageEditEnabled() && mEditEnable) {
            mViewHolder.tvEdit.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.tvEdit.setVisibility(View.GONE);
        }
    }

    /**
     * 打开编辑的Activity
     */
    private void openImageEditActivity() {
//        LocalMedia item = mAdapter.getMediaItem(mViewHolder.pager.getCurrentItem());
//        File file;
//        file = mPictureMediaStoreCompat.createFile(0, true, "jpg");
//        mEditImageFile = file;
//        Intent intent = new Intent();
//        intent.setClass(mActivity, ImageEditActivity.class);
//        intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SCREEN_ORIENTATION, mActivity.getRequestedOrientation());
//        intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, item.getUri());
//        intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SAVE_PATH, mEditImageFile.getAbsolutePath());
//        mImageEditActivityResult.launch(intent);
    }

    /**
     * 关闭Activity回调相关数值,如果需要压缩，另外弄一套压缩逻辑
     *
     * @param apply 是否同意
     */
    private void setResultOkByIsCompress(boolean apply) {
        // 判断是否需要压缩
        if (mGlobalSpec.getOnImageCompressionListener() != null) {
            if (apply) {
                compressFile();
            } else {
                // 直接返回
                setResultOk(false);
            }
        } else {
            if (apply) {
                moveStoreCompatFile();
            } else {
                // 直接返回
                setResultOk(false);
            }
        }
    }

    /**
     * 不压缩，直接移动文件
     */
    private void moveStoreCompatFile() {
        // 显示loading动画
        setControlTouchEnable(false);

        // 复制相册的文件
        ThreadUtils.executeByIo(getMoveFileTask());
    }

    /**
     * 线程： 不压缩，直接移动文件
     *
     * @return 线程
     */
    private ThreadUtils.SimpleTask<Void> getMoveFileTask() {
        mMoveFileTask = new ThreadUtils.SimpleTask<Void>() {

            @Override
            public Void doInBackground() {
//                // 不压缩，直接迁移到配置文件
//                for (LocalFile item : mSelectedCollection.asList()) {
//                    if (item.getPath() != null) {
//                        File oldFile = new File(item.getPath());
//                        if (oldFile.exists()) {
//                            if (item.isImage() || item.isVideo()) {
//                                File newFile;
//                                if (item.isImage()) {
//                                    newFile = mPictureMediaStoreCompat.createFile(0, false, mAlbumCompressFileTask.getNameSuffix(item.getPath()));
//                                } else {
//                                    // 如果是视频
//                                    newFile = mVideoMediaStoreCompat.createFile(1, false, mAlbumCompressFileTask.getNameSuffix(item.getPath()));
//                                }
//                                handleEditImages(item, newFile, oldFile, false);
//                            }
//                        }
//                    }
//                }
                return null;
            }

            @Override
            public void onSuccess(Void result) {
                setResultOk(true);
            }

            @Override
            public void onCancel() {
                super.onCancel();
                setResultOk(true);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                setResultOk(true);
            }
        };
        return mMoveFileTask;
    }

    /**
     * 判断是否压缩，如果要压缩先要迁移复制再压缩
     */
    private void compressFile() {
        // 显示loading动画
        setControlTouchEnable(false);

        // 复制相册的文件
        ThreadUtils.executeByIo(getCompressFileTask());
    }

    private ThreadUtils.SimpleTask<Void> getCompressFileTask() {
        mCompressFileTask = new ThreadUtils.SimpleTask<Void>() {

            @Override
            public Void doInBackground() {
//                // 来自相册的，才根据配置处理压缩和迁移
//                if (mCompressEnable) {
//                    // 将 缓存文件 拷贝到 配置目录
//                    for (LocalFile item : mSelectedCollection.asList()) {
//                        Log.d(TAG, "item " + item.getId());
//                        // 判断是否需要压缩
//                        LocalFile isCompressItem = mAlbumCompressFileTask.isCompress(item);
//                        if (isCompressItem != null) {
//                            continue;
//                        }
//                        // 开始压缩逻辑，获取真实路径
//                        String path = mAlbumCompressFileTask.getPath(item);
//                        if (path != null) {
//                            handleCompress(item, path);
//                        }
//                    }
//                }
                return null;
            }

            @Override
            public void onSuccess(Void result) {
                setResultOk(true);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "getCompressFileTask onFail " + t.getMessage());
                setResultOk(true);
            }

            @Override
            public void onCancel() {
                super.onCancel();
                setResultOk(true);
            }
        };
        return mCompressFileTask;
    }

    /**
     * 处理压缩和迁移的核心逻辑
     *
     * @param item LocalFile
     * @param path 当前文件地址
     */
    private void handleCompress(LocalMedia item, String path) {
//        // 只处理图片和视频
//        if (!item.isImage() && !item.isVideo()) {
//            return;
//        }
//        String newFileName = mAlbumCompressFileTask.getNewFileName(item, path);
//        // 取出是否存在着处理后的文件
//        File newFile = mAlbumCompressFileTask.getNewFile(item, path, newFileName);
//        // 存在压缩后的文件并且没有编辑过的 就直接使用
//        if (newFile.exists() && item.getOldPath() == null) {
//            if (item.isImage()) {
//                item.updateFile(mContext, mPictureMediaStoreCompat, item, newFile, true);
//            } else {
//                item.updateFile(mContext, mVideoMediaStoreCompat, item, newFile, true);
//            }
//            Log.d(TAG, "存在直接使用");
//        } else {
//            // 进行压缩
//            if (item.isImage()) {
//                // 处理是否进行压缩图片
//                File compressionFile = mAlbumCompressFileTask.handleImage(path);
//                // 如果是编辑过的就给新的地址
//                if (item.getOldPath() != null) {
//                    newFile = mPictureMediaStoreCompat.createFile(0, false, mAlbumCompressFileTask.getNameSuffix(item.getOldPath()));
//                }
//                handleEditImages(item, newFile, compressionFile, true);
//                Log.d(TAG, "不存在新建文件");
//            } else if (item.isVideo()) {
//                // 压缩视频
//                if (mGlobalSpec.isCompressEnable() && mGlobalSpec.getVideoCompressCoordinator() != null) {
//                    // 如果是编辑过的就给新的地址
//                    if (item.getOldPath() != null) {
//                        newFile = mPictureMediaStoreCompat.createFile(0, false, mAlbumCompressFileTask.getNameSuffix(item.getOldPath()));
//                    }
//                    File finalNewFile = newFile;
//                    mGlobalSpec.getVideoCompressCoordinator().setVideoCompressListener(BasePreviewFragment.class, new VideoEditListener() {
//                        @Override
//                        public void onFinish() {
//                            item.updateFile(mContext, mPictureMediaStoreCompat, item, finalNewFile, true);
//                            // 如果是编辑过的加入相册
//                            if (item.getOldPath() != null) {
//                                Uri uri = MediaStoreUtils.displayToGallery(mContext, finalNewFile, TYPE_VIDEO,
//                                        item.getDuration(), item.getWidth(), item.getHeight(),
//                                        mVideoMediaStoreCompat.getSaveStrategy().getDirectory(), mVideoMediaStoreCompat);
//                                item.setId(MediaStoreUtils.getId(uri));
//                            }
//                            Log.d(TAG, "不存在新建文件");
//                        }
//
//                        @Override
//                        public void onProgress(int progress, long progressTime) {
//                        }
//
//                        @Override
//                        public void onCancel() {
//
//                        }
//
//                        @Override
//                        public void onError(@NotNull String message) {
//                        }
//                    });
//                    if (mGlobalSpec.getVideoCompressCoordinator() != null) {
//                        mGlobalSpec.getVideoCompressCoordinator().compressAsync(BasePreviewFragment.class, path, finalNewFile.getPath());
//                    }
//                }
//            }
//        }
    }

    /**
     * 判断是否编辑过的图片
     * 如果编辑过，则拷贝到配置目录下并加入相册
     *
     * @param item       文件数据
     * @param newFile    新移动的文件
     * @param oldFile    目前的文件
     * @param isCompress 是否压缩
     */
    private void handleEditImages(LocalMedia item, File newFile, File oldFile, Boolean isCompress) {
//        // 迁移到新的文件夹(配置目录)
//        if (item.getOldPath() != null) {
//            // 如果编辑过就直接 移动 文件
//            FileUtil.move(oldFile, newFile);
//        } else {
//            // 如果没有编辑过就拷贝，因为有可能是源文件需要保留
//            FileUtil.copy(oldFile, newFile);
//        }
//        item.updateFile(mContext, mPictureMediaStoreCompat, item, newFile, isCompress);
//        // 如果是编辑过的加入相册
//        if (item.getOldPath() != null) {
//            Uri uri = MediaStoreUtils.displayToGallery(mContext, newFile, TYPE_PICTURE,
//                    item.getDuration(), item.getWidth(), item.getHeight(),
//                    mPictureMediaStoreCompat.getSaveStrategy().getDirectory(), mPictureMediaStoreCompat);
//            item.setId(MediaStoreUtils.getId(uri));
//        }
    }

    /**
     * 设置返回值
     *
     * @param apply 是否同意
     */
    protected synchronized void setResultOk(boolean apply) {
//        Log.d(TAG, "setResultOk");
//        refreshMultiMediaItem(apply);
//        if (mGlobalSpec.getOnResultCallbackListener() == null || !mIsExternalUsers) {
//            // 如果是外部使用并且不同意，则不执行RESULT_OK
//            Intent intent = new Intent();
//            intent.putExtra(EXTRA_RESULT_BUNDLE, mSelectedCollection.getDataWithBundle());
//            intent.putExtra(EXTRA_RESULT_APPLY, apply);
//            intent.putExtra(EXTRA_RESULT_IS_EDIT, mIsEdit);
//            intent.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
//            if (mIsExternalUsers && !apply) {
//                mActivity.setResult(Activity.RESULT_CANCELED, intent);
//            } else {
//                mActivity.setResult(RESULT_OK, intent);
//            }
//        } else {
//            mGlobalSpec.getOnResultCallbackListener().onResultFromPreview(mSelectedCollection.asList(), apply);
//        }
//        mActivity.finish();
    }

    /**
     * 根据确定取消 来 确定是否更新数据源
     *
     * @param apply 是否同意 TODO
     */
    private void refreshMultiMediaItem(boolean apply) {
//        if (mIsEdit) {
//            // 循环当前所有图片进行处理
//            for (MultiMedia multiMedia : mAdapter.getItems()) {
//                if (apply) {
//                    // 获取真实路径
//                    String path = null;
//                    if (multiMedia.getPath() == null) {
//                        File file = UriUtils.uriToFile(mContext, multiMedia.getUri());
//                        if (file != null) {
//                            path = file.getAbsolutePath();
//                        }
//                    } else {
//                        path = multiMedia.getPath();
//                    }
//
//                    // 判断有old才说明编辑过
//                    if (path != null && !TextUtils.isEmpty(multiMedia.getOldPath())) {
//                        File file = new File(path);
//                        multiMedia.setUri(mPictureMediaStoreCompat.getUri(path));
//                        multiMedia.setPath(file.getAbsolutePath());
//                    }
//                } else {
//                    // 更新回旧的数据
//                    if (multiMedia.getOldUri() != null) {
//                        multiMedia.setUri(multiMedia.getOldUri());
//                    }
//                    if (!TextUtils.isEmpty(multiMedia.getOldPath())) {
//                        multiMedia.setPath(multiMedia.getOldPath());
//                    }
//                }
//            }
//        }
    }

    /**
     * 设置是否启用界面触摸，不可禁止中断、退出
     */
    private void setControlTouchEnable(boolean enable) {
        // 如果不可用就显示 加载中 view,否则隐藏
        if (!enable) {
            mViewHolder.pbLoading.setVisibility(View.VISIBLE);
            mViewHolder.buttonApply.setVisibility(View.GONE);
            setCheckViewEnable(false);
            mViewHolder.checkView.setOnClickListener(null);
            mViewHolder.tvEdit.setEnabled(false);
            mViewHolder.originalLayout.setEnabled(false);
        } else {
            mViewHolder.pbLoading.setVisibility(View.GONE);
            mViewHolder.buttonApply.setVisibility(View.VISIBLE);
            setCheckViewEnable(true);
            mViewHolder.checkView.setOnClickListener(this);
            mViewHolder.tvEdit.setEnabled(true);
            mViewHolder.originalLayout.setEnabled(true);
        }
    }

//    /**
//     * 处理窗口
//     *
//     * @param item 当前图片
//     * @return 为true则代表符合规则
//     */
//    private boolean assertAddSelection(MultiMedia item) {
//        IncapableCause cause = mSelectedCollection.isAcceptable(item);
//        IncapableCause.handleCause(mContext, cause);
//        return cause == null;
//    }

    /**
     * 设置checkView是否启动，配置优先
     *
     * @param enable 是否启动
     */
    private void setCheckViewEnable(boolean enable) {
        if (mSelectedEnable) {
            mViewHolder.checkView.setEnabled(enable);
        } else {
            mViewHolder.checkView.setEnabled(false);
        }
    }

    public static class ViewHolder {
        public View rootView;
        public ViewPager2 pager;
        ImageButton iBtnBack;
        TextView tvEdit;
        public CheckRadioView original;
        public LinearLayout originalLayout;
        public TextView size;
        public TextView buttonApply;
        public FrameLayout bottomToolbar;
        public CheckView checkView;
        public ProgressBar pbLoading;

        ViewHolder(View rootView) {
            this.rootView = rootView;
            this.pager = rootView.findViewById(R.id.pager);
            this.iBtnBack = rootView.findViewById(R.id.ibtnBack);
            this.tvEdit = rootView.findViewById(R.id.tvEdit);
            this.original = rootView.findViewById(R.id.original);
            this.originalLayout = rootView.findViewById(R.id.originalLayout);
            this.size = rootView.findViewById(R.id.size);
            this.buttonApply = rootView.findViewById(R.id.buttonApply);
            this.bottomToolbar = rootView.findViewById(R.id.bottomToolbar);
            this.checkView = rootView.findViewById(R.id.checkView);
            this.pbLoading = rootView.findViewById(R.id.pbLoading);
        }

    }

}
