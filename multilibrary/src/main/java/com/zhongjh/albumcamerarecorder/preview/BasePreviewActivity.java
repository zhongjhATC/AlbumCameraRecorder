package com.zhongjh.albumcamerarecorder.preview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhongjh.albumcamerarecorder.R;

import gaode.zhongjh.com.common.entity.IncapableCause;
import gaode.zhongjh.com.common.entity.MultiMedia;
import gaode.zhongjh.com.common.enums.MultimediaTypes;
import gaode.zhongjh.com.common.utils.FileUtil;
import gaode.zhongjh.com.common.utils.MediaStoreCompat;
import gaode.zhongjh.com.common.widget.IncapableDialog;

import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.albumcamerarecorder.preview.adapter.PreviewPagerAdapter;
import com.zhongjh.albumcamerarecorder.preview.previewitem.PreviewItemFragment;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.album.widget.CheckRadioView;
import com.zhongjh.albumcamerarecorder.album.widget.CheckView;
import com.zhongjh.albumcamerarecorder.album.widget.PreviewViewPager;
import com.zhongjh.albumcamerarecorder.utils.BitmapUtils;

import gaode.zhongjh.com.common.utils.StatusBarUtils;

import com.zhongjh.imageedit.IMGEditActivity;

import java.io.File;
import java.io.IOException;

/**
 * 预览的基类
 */
public class BasePreviewActivity extends AppCompatActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener {

    public static final int REQ_IMAGE_EDIT = 1;
    public static final String EXTRA_IS_ALLOW_REPEAT = "extra_is_allow_repeat";
    public static final String EXTRA_DEFAULT_BUNDLE = "extra_default_bundle";
    public static final String EXTRA_RESULT_BUNDLE = "extra_result_bundle";
    public static final String EXTRA_RESULT_APPLY = "extra_result_apply";
    public static final String EXTRA_RESULT_IS_EDIT = "extra_result_is_edit";
    public static final String EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable";
    public static final String CHECK_STATE = "checkState";
    public static final String ENABLE_OPERATION = "enable_operation";
    public static final String IS_SELECTED_LISTENER = "is_selected_listener";
    public static final String IS_SELECTED_CHECK = "is_selected_check";

    protected final SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);
    protected GlobalSpec mGlobalSpec;
    protected AlbumSpec mAlbumSpec;

    protected PreviewPagerAdapter mAdapter;

    protected boolean mOriginalEnable;      // 是否原图
    private boolean mIsEdit; // 是否编辑了图片

    protected int mPreviousPos = -1;    // 当前预览的图片的索引,默认第一个

    protected boolean mEnableOperation = true; // 启用操作，默认true,也不启动右上角的选择框自定义触发事件
    protected boolean mIsSelectedListener = true; // 是否触发选择事件，目前除了相册功能没问题之外，别的触发都会闪退，原因是uri不是通过数据库而获得的
    protected boolean mIsSelectedCheck = true;  // 设置右上角是否检测类型

    private MediaStoreCompat mPictureMediaStoreCompat;  // 图片存储器
    private File mEditImageFile; // 当前编辑完的图片文件

    protected ViewHolder mViewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(GlobalSpec.getInstance().themeId);  // 获取样式
        super.onCreate(savedInstanceState);
        StatusBarUtils.initStatusBar(BasePreviewActivity.this);
        setContentView(R.layout.activity_media_preview_zjh);

        mGlobalSpec = GlobalSpec.getInstance();
        mAlbumSpec = AlbumSpec.getInstance();
        boolean isAllowRepeat = getIntent().getBooleanExtra(EXTRA_IS_ALLOW_REPEAT, false);
        mEnableOperation = getIntent().getBooleanExtra(ENABLE_OPERATION, true);
        mIsSelectedListener = getIntent().getBooleanExtra(IS_SELECTED_LISTENER, true);
        mIsSelectedCheck = getIntent().getBooleanExtra(IS_SELECTED_CHECK, true);

        mPictureMediaStoreCompat = new MediaStoreCompat(this);
        // 设置图片路径
        if (mGlobalSpec.pictureStrategy != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mPictureMediaStoreCompat.setSaveStrategy(mGlobalSpec.pictureStrategy);
        } else {
            // 否则使用全局的
            if (mGlobalSpec.saveStrategy == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mPictureMediaStoreCompat.setSaveStrategy(mGlobalSpec.saveStrategy);
            }
        }

        if (savedInstanceState == null) {
            // 初始化别的界面传递过来的数据
            mSelectedCollection.onCreate(getIntent().getBundleExtra(EXTRA_DEFAULT_BUNDLE), isAllowRepeat);
            mOriginalEnable = getIntent().getBooleanExtra(EXTRA_RESULT_ORIGINAL_ENABLE, false);
        } else {
            // 初始化缓存的数据
            mSelectedCollection.onCreate(savedInstanceState, isAllowRepeat);
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE);
        }

        mViewHolder = new ViewHolder(this);

        mAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), null);
        mViewHolder.pager.setAdapter(mAdapter);
        mViewHolder.check_view.setCountable(mAlbumSpec.countable);

        initListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_IMAGE_EDIT) {
                mIsEdit = true;

                // 更新相册库
                BitmapUtils.displayToGallery(this, mEditImageFile);

                // 更新相册的实体
                Uri mediaUri = FileUtil.getFileUri(getApplicationContext(), MultimediaTypes.PICTURE, mEditImageFile);

                // 更新当前fragment
                MultiMedia item = mAdapter.getMediaItem(mViewHolder.pager.getCurrentItem());
                item.setMediaUri(mediaUri);
                mAdapter.setMediaItem(mViewHolder.pager.getCurrentItem(), item);
                ((PreviewItemFragment) mAdapter.getFragment(mViewHolder.pager.getCurrentItem())).init();
            }
        }
    }

    /**
     * 所有事件
     */
    private void initListener() {
        // 编辑
        mViewHolder.tvEdit.setOnClickListener(this);
        // 返回
        mViewHolder.ibtnBack.setOnClickListener(this);
        // 确认
        mViewHolder.button_apply.setOnClickListener(this);
        // 多图时滑动事件
        mViewHolder.pager.addOnPageChangeListener(this);
        // 右上角选择事件
        mViewHolder.check_view.setOnClickListener(v -> {
            MultiMedia item = mAdapter.getMediaItem(mViewHolder.pager.getCurrentItem());
            if (mSelectedCollection.isSelected(item)) {
                mSelectedCollection.remove(item);
                if (mAlbumSpec.countable) {
                    mViewHolder.check_view.setCheckedNum(CheckView.UNCHECKED);
                } else {
                    mViewHolder.check_view.setChecked(false);
                }
            } else {
                boolean isTrue = true;
                if (mIsSelectedCheck)
                    isTrue = assertAddSelection(item);
                if (isTrue) {
                    mSelectedCollection.add(item);
                    if (mAlbumSpec.countable) {
                        mViewHolder.check_view.setCheckedNum(mSelectedCollection.checkedNumOf(item));
                    } else {
                        mViewHolder.check_view.setChecked(true);
                    }
                }
            }
            updateApplyButton();

            if (mAlbumSpec.onSelectedListener != null && mIsSelectedListener) {
                // 触发选择的接口事件
                mAlbumSpec.onSelectedListener.onSelected(
                        mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString());
            }
        });
        // 点击原图事件
        mViewHolder.originalLayout.setOnClickListener(v -> {
            int count = countOverMaxSize();
            if (count > 0) {
                IncapableDialog incapableDialog = IncapableDialog.newInstance("",
                        getString(R.string.error_over_original_count, count, mAlbumSpec.originalMaxSize));
                incapableDialog.show(getSupportFragmentManager(),
                        IncapableDialog.class.getName());
                return;
            }

            mOriginalEnable = !mOriginalEnable;
            mViewHolder.original.setChecked(mOriginalEnable);
            if (!mOriginalEnable) {
                mViewHolder.original.setColor(Color.WHITE);
            }

            if (mAlbumSpec.onCheckedListener != null) {
                mAlbumSpec.onCheckedListener.onCheck(mOriginalEnable);
            }
        });
        updateApplyButton();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mSelectedCollection.onSaveInstanceState(outState);
        outState.putBoolean("checkState", mOriginalEnable);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        sendBackResult(false);
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        if (mGlobalSpec.isCutscenes)
            //关闭窗体动画显示
            this.overridePendingTransition(0, R.anim.activity_close);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ibtnBack) {
            onBackPressed();
        } else if (v.getId() == R.id.button_apply) {
            sendBackResult(true);
            finish();
        } else if (v.getId() == R.id.tvEdit) {
            MultiMedia item = mAdapter.getMediaItem(mViewHolder.pager.getCurrentItem());

            File file;

            try {
                file = mPictureMediaStoreCompat.createFile(0);
                mEditImageFile = file;
            } catch (IOException e) {
                return;
            }

            Intent intent = new Intent();
            intent.setClass(BasePreviewActivity.this, IMGEditActivity.class);
            if (item.getMediaUri() != null)
                intent.putExtra(IMGEditActivity.EXTRA_IMAGE_URI, item.getMediaUri());
            else
                intent.putExtra(IMGEditActivity.EXTRA_IMAGE_URI, item.getUri());
            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, mEditImageFile.getAbsolutePath());
            startActivityForResult(intent, REQ_IMAGE_EDIT);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * 滑动事件
     *
     * @param position 索引
     */
    @Override
    public void onPageSelected(int position) {
        PreviewPagerAdapter adapter = (PreviewPagerAdapter) mViewHolder.pager.getAdapter();
        if (mPreviousPos != -1 && mPreviousPos != position) {
            ((PreviewItemFragment) adapter.instantiateItem(mViewHolder.pager, mPreviousPos)).resetView();

            MultiMedia item = adapter.getMediaItem(position);
            if (mAlbumSpec.countable) {
                int checkedNum = mSelectedCollection.checkedNumOf(item);
                mViewHolder.check_view.setCheckedNum(checkedNum);
                if (checkedNum > 0) {
                    mViewHolder.check_view.setEnabled(true);
                } else {
                    mViewHolder.check_view.setEnabled(!mSelectedCollection.maxSelectableReached());
                }
            } else {
                boolean checked = mSelectedCollection.isSelected(item);
                mViewHolder.check_view.setChecked(checked);
                if (checked) {
                    mViewHolder.check_view.setEnabled(true);
                } else {
                    mViewHolder.check_view.setEnabled(!mSelectedCollection.maxSelectableReached());
                }
            }
            updateSize(item);
        }
        mPreviousPos = position;
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    /**
     * 更新确定按钮状态
     */
    private void updateApplyButton() {
        // 获取已选的图片
        int selectedCount = mSelectedCollection.count();
        if (selectedCount == 0) {
            // 禁用
            mViewHolder.button_apply.setText(R.string.button_sure_default);
            mViewHolder.button_apply.setEnabled(false);
        } else if (selectedCount == 1 && mAlbumSpec.singleSelectionModeEnabled()) {
            // 如果只选择一张或者配置只能选一张，或者不显示数字的时候。启用，不显示数字
            mViewHolder.button_apply.setText(R.string.button_sure_default);
            mViewHolder.button_apply.setEnabled(true);
        } else {
            // 启用，显示数字
            mViewHolder.button_apply.setEnabled(true);
            mViewHolder.button_apply.setText(getString(R.string.button_sure, selectedCount));
        }

        // 判断是否开启原图
        if (mAlbumSpec.originalable) {
            // 显示
            mViewHolder.originalLayout.setVisibility(View.VISIBLE);
            updateOriginalState();
        } else {
            // 隐藏
            mViewHolder.originalLayout.setVisibility(View.GONE);
        }

        // 判断是否启动操作
        if (!mEnableOperation) {
            mViewHolder.button_apply.setVisibility(View.GONE);
            mViewHolder.check_view.setVisibility(View.GONE);
        } else {
            mViewHolder.button_apply.setVisibility(View.VISIBLE);
            mViewHolder.check_view.setVisibility(View.VISIBLE);
        }
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
                        getString(R.string.error_over_original_size, mAlbumSpec.originalMaxSize));
                incapableDialog.show(getSupportFragmentManager(),
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

    /**
     * 如果当前item是gif就显示多少M的文本
     * 如果当前item是video就显示播放按钮
     *
     * @param item 当前图片
     */
    @SuppressLint("SetTextI18n")
    protected void updateSize(MultiMedia item) {
        if (item.isGif()) {
            mViewHolder.size.setVisibility(View.VISIBLE);
            mViewHolder.size.setText(PhotoMetadataUtils.getSizeInMB(item.size) + "M");
        } else {
            mViewHolder.size.setVisibility(View.GONE);
        }

        if (item.isVideo()) {
            mViewHolder.originalLayout.setVisibility(View.GONE);
        } else if (mAlbumSpec.originalable) {
            mViewHolder.originalLayout.setVisibility(View.VISIBLE);
        }

        if (item.isImage() && mGlobalSpec.isImageEdit) {
            mViewHolder.tvEdit.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.tvEdit.setVisibility(View.GONE);
        }
    }

    /**
     * 设置返回值
     *
     * @param apply 是否同意
     */
    protected void sendBackResult(boolean apply) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_BUNDLE, mSelectedCollection.getDataWithBundle());
        intent.putExtra(EXTRA_RESULT_APPLY, apply);
        intent.putExtra(EXTRA_RESULT_IS_EDIT, mIsEdit);
        intent.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable);
        setResult(Activity.RESULT_OK, intent);
    }

    /**
     * 处理窗口
     *
     * @param item 当前图片
     * @return 为true则代表符合规则
     */
    private boolean assertAddSelection(MultiMedia item) {
        IncapableCause cause = mSelectedCollection.isAcceptable(item);
        IncapableCause.handleCause(this, cause);
        return cause == null;
    }

    public static class ViewHolder {
        public Activity activity;
        public PreviewViewPager pager;
        ImageButton ibtnBack;
        TextView tvEdit;
        public CheckRadioView original;
        public LinearLayout originalLayout;
        public TextView size;
        public TextView button_apply;
        public FrameLayout bottom_toolbar;
        public CheckView check_view;

        ViewHolder(Activity activity) {
            this.activity = activity;
            this.pager = activity.findViewById(R.id.pager);
            this.ibtnBack = activity.findViewById(R.id.ibtnBack);
            this.tvEdit = activity.findViewById(R.id.tvEdit);
            this.original = activity.findViewById(R.id.original);
            this.originalLayout = activity.findViewById(R.id.originalLayout);
            this.size = activity.findViewById(R.id.size);
            this.button_apply = activity.findViewById(R.id.button_apply);
            this.bottom_toolbar = activity.findViewById(R.id.bottom_toolbar);
            this.check_view = activity.findViewById(R.id.check_view);
        }

    }
}
