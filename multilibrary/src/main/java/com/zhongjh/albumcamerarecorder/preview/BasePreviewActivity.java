package com.zhongjh.albumcamerarecorder.preview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.albumcamerarecorder.album.widget.CheckRadioView;
import com.zhongjh.albumcamerarecorder.album.widget.CheckView;
import com.zhongjh.albumcamerarecorder.album.widget.PreviewViewPager;
import com.zhongjh.albumcamerarecorder.listener.PreviewListener;
import com.zhongjh.albumcamerarecorder.preview.adapter.PreviewPagerAdapter;
import com.zhongjh.albumcamerarecorder.preview.previewitem.PreviewItemFragment;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.utils.BitmapUtils;
import com.zhongjh.imageedit.ImageEditActivity;

import java.io.File;

import com.zhongjh.common.entity.IncapableCause;
import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.enums.MultimediaTypes;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.StatusBarUtils;
import com.zhongjh.common.widget.IncapableDialog;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.TYPE_PICTURE;

/**
 * 预览的基类
 *
 * @author zhongjh
 */
public class BasePreviewActivity extends AppCompatActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, PreviewListener {

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
    public static final String IS_ALBUM_URI = "is_album_uri";

    protected final SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);
    protected GlobalSpec mGlobalSpec;
    protected AlbumSpec mAlbumSpec;

    protected PreviewPagerAdapter mAdapter;

    /**
     * 是否原图
     */
    protected boolean mOriginalEnable;
    /**
     * 是否返回相册的uri，否则是普通文件的uri
     */
    private boolean mIsAlbumUri;
    /**
     * 是否编辑了图片
     */
    private boolean mIsEdit;

    /**
     * 当前预览的图片的索引,默认第一个
     */
    protected int mPreviousPos = -1;

    /**
     * 启用操作，默认true,也不启动右上角的选择框自定义触发事件
     */
    protected boolean mEnableOperation = true;
    /**
     * 是否触发选择事件，目前除了相册功能没问题之外，别的触发都会闪退，原因是uri不是通过数据库而获得的
     */
    protected boolean mIsSelectedListener = true;
    /**
     * 设置右上角是否检测类型
     */
    protected boolean mIsSelectedCheck = true;

    /**
     * 图片存储器
     */
    private MediaStoreCompat mPictureMediaStoreCompat;
    /**
     * 当前编辑完的图片文件
     */
    private File mEditImageFile;

    protected ViewHolder mViewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 获取样式
        setTheme(GlobalSpec.getInstance().themeId);
        super.onCreate(savedInstanceState);
        StatusBarUtils.initStatusBar(BasePreviewActivity.this);
        setContentView(R.layout.activity_media_preview_zjh);

        mGlobalSpec = GlobalSpec.getInstance();
        mAlbumSpec = AlbumSpec.getInstance();
        boolean isAllowRepeat = getIntent().getBooleanExtra(EXTRA_IS_ALLOW_REPEAT, false);
        mEnableOperation = getIntent().getBooleanExtra(ENABLE_OPERATION, true);
        mIsSelectedListener = getIntent().getBooleanExtra(IS_SELECTED_LISTENER, true);
        mIsSelectedCheck = getIntent().getBooleanExtra(IS_SELECTED_CHECK, true);
        mIsAlbumUri = getIntent().getBooleanExtra(IS_ALBUM_URI, false);

        // 设置图片路径
        if (mGlobalSpec.pictureStrategy != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mPictureMediaStoreCompat = new MediaStoreCompat(this,mGlobalSpec.pictureStrategy);
        } else {
            // 否则使用全局的
            if (mGlobalSpec.saveStrategy == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mPictureMediaStoreCompat = new MediaStoreCompat(this,mGlobalSpec.saveStrategy);
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

        mAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, null);
        mViewHolder.pager.setAdapter(mAdapter);
        mViewHolder.checkView.setCountable(mAlbumSpec.countable);

        initListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_IMAGE_EDIT) {
                mIsEdit = true;
                refreshMultiMediaItem();
            }
        }
    }

    /**
     * 刷新MultiMedia TODO
     */
    private void refreshMultiMediaItem() {
        // 未加入相册时候的uri
        Uri editUri = mPictureMediaStoreCompat.getUri(mEditImageFile.getPath());
        // 获取当前查看的multimedia
        MultiMedia item = mAdapter.getMediaItem(mViewHolder.pager.getCurrentItem());
        item.setOldMediaUri(item.getMediaUri());
        item.setOldUri(item.getUri());
        item.setOldPath(item.getPath());
        // 更新当前fragment
        item.setMediaUri(null);
        item.setUri(editUri);
        item.setPath(mEditImageFile.getPath());
        mAdapter.setMediaItem(mViewHolder.pager.getCurrentItem(), item);
        ((PreviewItemFragment) mAdapter.getFragment(mViewHolder.pager.getCurrentItem())).init();
    }

    /**
     * 根据确定取消 来 确定是否更新数据源
     *
     * @param apply 是否同意 TODO
     */
    private void refreshMultiMediaItem(boolean apply) {
        if (mIsEdit)
        // 循环当前所有图片进行处理
        {
            for (MultiMedia multiMedia : mAdapter.getmItems()) {
                if (mIsAlbumUri) {
                    if (apply) {
                        if (multiMedia.getPath() != null) {
                            File file = new File(multiMedia.getPath());
                            // 加入相册库
                            Uri editMediaUri = BitmapUtils.displayToGallery(this, file, TYPE_PICTURE, -1, mPictureMediaStoreCompat.getSaveStrategy().getDirectory(), mPictureMediaStoreCompat);
                            multiMedia.setUri(null);
                            multiMedia.setMediaUri(editMediaUri);
                        }
                    } else {
                        multiMedia.setUri(null);
                        multiMedia.setMediaUri(multiMedia.getOldMediaUri());
                        multiMedia.setPath(multiMedia.getOldPath());
                    }
                } else {
                    if (!apply) {
                        // 更新回旧的数据
                        multiMedia.setUri(multiMedia.getOldUri());
                        multiMedia.setMediaUri(null);
                        multiMedia.setPath(multiMedia.getOldPath());
                    }
                }
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
        mViewHolder.iBtnBack.setOnClickListener(this);
        // 确认
        mViewHolder.buttonApply.setOnClickListener(this);
        // 多图时滑动事件
        mViewHolder.pager.addOnPageChangeListener(this);
        // 右上角选择事件
        mViewHolder.checkView.setOnClickListener(v -> {
            MultiMedia item = mAdapter.getMediaItem(mViewHolder.pager.getCurrentItem());
            if (mSelectedCollection.isSelected(item)) {
                mSelectedCollection.remove(item);
                if (mAlbumSpec.countable) {
                    mViewHolder.checkView.setCheckedNum(CheckView.UNCHECKED);
                } else {
                    mViewHolder.checkView.setChecked(false);
                }
            } else {
                boolean isTrue = true;
                if (mIsSelectedCheck) {
                    isTrue = assertAddSelection(item);
                }
                if (isTrue) {
                    mSelectedCollection.add(item);
                    if (mAlbumSpec.countable) {
                        mViewHolder.checkView.setCheckedNum(mSelectedCollection.checkedNumOf(item));
                    } else {
                        mViewHolder.checkView.setChecked(true);
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
                        getString(R.string.z_multi_library_error_over_original_count, count, mAlbumSpec.originalMaxSize));
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
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
        {
            this.overridePendingTransition(0, R.anim.activity_close);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ibtnBack) {
            onBackPressed();
        } else if (v.getId() == R.id.buttonApply) {
            sendBackResult(true);
            finish();
        } else if (v.getId() == R.id.tvEdit) {
            MultiMedia item = mAdapter.getMediaItem(mViewHolder.pager.getCurrentItem());

            File file;

            file = mPictureMediaStoreCompat.createFile(0, true);
            mEditImageFile = file;

            Intent intent = new Intent();
            intent.setClass(BasePreviewActivity.this, ImageEditActivity.class);
            if (item.getMediaUri() != null) {
                intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, item.getMediaUri());
            } else {
                intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, item.getUri());
            }
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SAVE_PATH, mEditImageFile.getAbsolutePath());
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
                mViewHolder.checkView.setCheckedNum(checkedNum);
                if (checkedNum > 0) {
                    mViewHolder.checkView.setEnabled(true);
                } else {
                    mViewHolder.checkView.setEnabled(!mSelectedCollection.maxSelectableReached());
                }
            } else {
                boolean checked = mSelectedCollection.isSelected(item);
                mViewHolder.checkView.setChecked(checked);
                if (checked) {
                    mViewHolder.checkView.setEnabled(true);
                } else {
                    mViewHolder.checkView.setEnabled(!mSelectedCollection.maxSelectableReached());
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
            mViewHolder.buttonApply.setText(R.string.z_multi_library_button_sure_default);
            mViewHolder.buttonApply.setEnabled(false);
        } else if (selectedCount == 1 && mAlbumSpec.singleSelectionModeEnabled()) {
            // 如果只选择一张或者配置只能选一张，或者不显示数字的时候。启用，不显示数字
            mViewHolder.buttonApply.setText(R.string.z_multi_library_button_sure_default);
            mViewHolder.buttonApply.setEnabled(true);
        } else {
            // 启用，显示数字
            mViewHolder.buttonApply.setEnabled(true);
            mViewHolder.buttonApply.setText(getString(R.string.z_multi_library_button_sure, selectedCount));
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
            mViewHolder.buttonApply.setVisibility(View.GONE);
            mViewHolder.checkView.setVisibility(View.GONE);
        } else {
            mViewHolder.buttonApply.setVisibility(View.VISIBLE);
            mViewHolder.checkView.setVisibility(View.VISIBLE);
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
                        getString(R.string.z_multi_library_error_over_original_size, mAlbumSpec.originalMaxSize));
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
                float size = PhotoMetadataUtils.getSizeInMb(item.getSize());
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
            mViewHolder.size.setText(PhotoMetadataUtils.getSizeInMb(item.getSize()) + "M");
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
        refreshMultiMediaItem(apply);
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

    @Override
    public void test() {

    }

    public static class ViewHolder {
        public Activity activity;
        public PreviewViewPager pager;
        ImageButton iBtnBack;
        TextView tvEdit;
        public CheckRadioView original;
        public LinearLayout originalLayout;
        public TextView size;
        public TextView buttonApply;
        public FrameLayout bottomToolbar;
        public CheckView checkView;

        ViewHolder(Activity activity) {
            this.activity = activity;
            this.pager = activity.findViewById(R.id.pager);
            this.iBtnBack = activity.findViewById(R.id.ibtnBack);
            this.tvEdit = activity.findViewById(R.id.tvEdit);
            this.original = activity.findViewById(R.id.original);
            this.originalLayout = activity.findViewById(R.id.originalLayout);
            this.size = activity.findViewById(R.id.size);
            this.buttonApply = activity.findViewById(R.id.buttonApply);
            this.bottomToolbar = activity.findViewById(R.id.bottomToolbar);
            this.checkView = activity.findViewById(R.id.checkView);
        }

    }
}
