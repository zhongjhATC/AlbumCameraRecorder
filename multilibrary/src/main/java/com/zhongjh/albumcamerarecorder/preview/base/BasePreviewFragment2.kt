package com.zhongjh.albumcamerarecorder.preview.base

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.ImageView.ScaleType
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.zhongjh.albumcamerarecorder.MainActivity
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.album.ui.album.SelectedData
import com.zhongjh.albumcamerarecorder.album.utils.AlbumCompressFileTask
import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils
import com.zhongjh.albumcamerarecorder.album.widget.CheckRadioView
import com.zhongjh.albumcamerarecorder.album.widget.CheckView
import com.zhongjh.albumcamerarecorder.preview.adapter.PreviewPagerAdapter
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec
import com.zhongjh.albumcamerarecorder.sharedanimation.OnSharedAnimationViewListener
import com.zhongjh.albumcamerarecorder.sharedanimation.RecycleItemViewParams
import com.zhongjh.albumcamerarecorder.sharedanimation.SharedAnimationView
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.listener.OnMoreClickListener
import com.zhongjh.common.utils.DisplayMetricsUtils.getRealScreenWidth
import com.zhongjh.common.utils.DisplayMetricsUtils.getScreenHeight
import com.zhongjh.common.utils.MediaStoreCompat
import com.zhongjh.common.utils.MediaUtils
import com.zhongjh.common.utils.StatusBarUtils.initStatusBar
import com.zhongjh.common.utils.ThreadUtils
import com.zhongjh.common.utils.ThreadUtils.SimpleTask
import com.zhongjh.common.widget.IncapableDialog
import com.zhongjh.common.widget.IncapableDialog.Companion.newInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 目标是可以不止该库自用，也可以别的app别的功能直接使用
 * 预览窗口的基类支持以下功能：
 * 1. 决定是否共享动画
 * 2. 实例化ViewPager
 * 3. 确定后，返回的数据 - 是否压缩
 * 4. 支持放大缩小
 *
 * @author zhongjh
 * @date 2023/8/31
 */
abstract class BasePreviewFragment2 : Fragment() {

    protected val TAG: String = this@BasePreviewFragment2.javaClass.simpleName

    protected lateinit var mContext: Context
    private lateinit var mViewHolder: ViewHolder
    protected lateinit var mViewPager2: ViewPager2
    private lateinit var mAdapter: PreviewPagerAdapter
    private val mGlobalSpec by lazy {
        GlobalSpec
    }
    private val mAlbumSpec by lazy {
        AlbumSpec
    }

    protected val viewModel by lazy {
        val activity = requireActivity()
        val savedStateViewModelFactory = SavedStateViewModelFactory(activity.application, this)
        return@lazy ViewModelProvider(
            this,
            savedStateViewModelFactory
        )[AndroidViewModel::class.java]
    }

    /**
     * 图片存储器
     */
    private val mPictureMediaStoreCompat by lazy {
        // 设置图片路径
        mGlobalSpec.pictureStrategy?.let {
            // 如果设置了图片的文件夹路径，就使用它的
            MediaStoreCompat(mContext, it)
        } ?: let {
            mGlobalSpec.saveStrategy?.let {
                // 否则使用全局的
                MediaStoreCompat(mContext, it)
            } ?: let {
                // 全局如果都没有，抛错
                throw RuntimeException("Please set the GlobalSpec <saveStrategy> or <pictureStrategy> configuration.")
            }
        }
    }

    /**
     * 录像文件配置路径
     */
    private val mVideoMediaStoreCompat by lazy {
        // 设置视频路径
        mGlobalSpec.videoStrategy?.let {
            // 如果设置了图片的文件夹路径，就使用它的
            MediaStoreCompat(mContext, it)
        } ?: let {
            mGlobalSpec.saveStrategy?.let {
                // 否则使用全局的
                MediaStoreCompat(mContext, it)
            } ?: let {
                // 全局如果都没有，抛错
                throw RuntimeException("Please set the GlobalSpec <saveStrategy> or <videoStrategy> configuration.")
            }
        }
    }

    /**
     * 打开ImageEditActivity的回调
     */
    protected lateinit var mImageEditActivityResult: ActivityResultLauncher<Intent>

    /**
     * 完成压缩-复制的异步线程
     */
    protected val mCompressFileTask: SimpleTask<Boolean> by lazy {
        object : SimpleTask<Boolean>() {
            override fun doInBackground(): Boolean {
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
                return true
            }

            override fun onSuccess(result: Boolean) {
                setResultOk(true)
            }

            override fun onFail(t: Throwable) {
                super.onFail(t)
                Toast.makeText(mContext, t.message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "getCompressFileTask onFail " + t.message)
                setResultOk(true)
            }

            override fun onCancel() {
                super.onCancel()
                setResultOk(true)
            }
        }
    }

    /**
     * 完成迁移文件的异步线程
     */
    protected val mMoveFileTask: SimpleTask<Boolean> by lazy {
        object : SimpleTask<Boolean>() {
            override fun doInBackground(): Boolean {
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
                return true
            }

            override fun onSuccess(result: Boolean) {
                setResultOk(true)
            }

            override fun onCancel() {
                super.onCancel()
                setResultOk(true)
            }

            override fun onFail(t: Throwable) {
                super.onFail(t)
                setResultOk(true)
            }
        }
    }

    /**
     * 异步线程的逻辑，确定当前选择的文件列表，根据是否压缩配置决定重新返回新的文件列表
     */
    protected val mAlbumCompressFileTask by lazy {
        AlbumCompressFileTask(
            mContext,
            TAG,
            BasePreviewFragment::class.java,
            mGlobalSpec,
            mPictureMediaStoreCompat,
            mVideoMediaStoreCompat
        )
    }

    var screenWidth = 0
    var screenHeight = 0

    /**
     * 是否从界面恢复回来的
     */
    protected var mIsSavedInstanceState = false

    /**
     * 是否启动原图
     */
    protected var mOriginalEnable = false

    /**
     * 设置是否启动确定功能
     */
    protected var mApplyEnable = true

    /**
     * 设置是否启动选择功能
     */
    protected var mSelectedEnable = true

    /**
     * 设置是否开启编辑功能
     */
    protected var mEditEnable = true

    /**
     * 设置是否开启压缩
     */
    protected var mCompressEnable = false

    /**
     * 是否编辑了图片
     */
    protected var mIsEdit = false

    /**
     * 是否触发选择事件，目前除了相册功能没问题之外，别的触发都会闪退，原因是uri不是通过数据库而获得的
     */
    protected var mIsSelectedListener = true

    /**
     * 设置右上角是否检测类型
     */
    protected var mIsSelectedCheck = true

    /**
     * 是否外部直接调用该预览窗口，如果是外部直接调用，那么可以启用回调接口，内部统一使用onActivityResult方式回调
     */
    protected var mIsExternalUsers = false

    /**
     * 是否首次共享动画，只有第一次打开的时候才触发共享动画
     */
    private var mFirstSharedAnimation = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context.applicationContext
        // 拦截OnBackPressed
        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    setResultOkByIsCompress(false)
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 获取样式
        return initStyle(inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 获取宽高
        screenWidth = getRealScreenWidth(requireContext())
        screenHeight = getScreenHeight(requireContext())
        // 初始化回调
        initActivityResult()
        // 初始化状态栏
        initStatusBar(requireActivity())
        // 初始化bundle的Value
        initBundleValue(savedInstanceState)
        mViewHolder = ViewHolder(view)
        mViewHolder.checkView.setCountable(mAlbumSpec.countable)
        // 初始化共享动画view
        initSharedAnimationView()
        initViewPagerData()
        initListener()
        updateApplyButton()
    }

    override fun onDestroy() {
//        mAdapter.destroy()
        mViewPager2.unregisterOnPageChangeCallback(mOnPageChangeCallback)
        // 如果依附的Activity是MainActivity,就显示底部控件动画
        if (requireActivity() is MainActivity) {
            (requireActivity() as MainActivity).showHideTableLayoutAnimator(true)
        }
        super.onDestroy()
    }

    /**
     * 获取已选择的数据
     */
    abstract fun getDatas(): ArrayList<LocalMedia>

    /**
     * 获取当前显示的文件索引
     */
    abstract fun getPreviewPosition(): Int

    /**
     * 设置当前显示的文件索引
     */
    abstract fun setPreviewPosition(previewPosition: Int)

    /**
     * 返回当前所选择的数据
     * @return 返回当前所选择的数据
     */
    abstract fun getSelectedData(): SelectedData

    /**
     * 初始化样式
     */
    private fun initStyle(inflater: LayoutInflater, container: ViewGroup?): View {
        val wrapper = ContextThemeWrapper(requireActivity(), mGlobalSpec.themeId)
        val cloneInContext = inflater.cloneInContext(wrapper)
        return cloneInContext.inflate(R.layout.fragment_preview_zjh, container, false)
    }

    /**
     * 针对回调
     */
    private fun initActivityResult() {
        mImageEditActivityResult = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                mIsEdit = true
                refreshMultiMediaItem()
            }
        }
    }

    /**
     * 初始化bundle的Value
     */
    private fun initBundleValue(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            // 初始化别的界面传递过来的数据
            arguments?.let {
                mApplyEnable = it.getBoolean(BasePreviewFragment.APPLY_ENABLE, true)
                mSelectedEnable = it.getBoolean(BasePreviewFragment.SELECTED_ENABLE, true)
                mIsSelectedListener = it.getBoolean(BasePreviewFragment.IS_SELECTED_LISTENER, true)
                mIsSelectedCheck = it.getBoolean(BasePreviewFragment.IS_SELECTED_CHECK, true)
                mIsExternalUsers = it.getBoolean(BasePreviewFragment.IS_EXTERNAL_USERS, false)
                mCompressEnable = it.getBoolean(BasePreviewFragment.COMPRESS_ENABLE, false)
                mEditEnable = it.getBoolean(BasePreviewFragment.EDIT_ENABLE, true)
                mOriginalEnable =
                    it.getBoolean(BasePreviewFragment.EXTRA_RESULT_ORIGINAL_ENABLE, false)
            }
        } else {
            mIsSavedInstanceState = true
        }
    }

    /**
     * 初始化MagicalView
     */
    private fun initSharedAnimationView() {
        mViewPager2 = ViewPager2(requireContext())
        mViewHolder.sharedAnimationView.setContentView(mViewPager2)
        if (isSharedAnimation()) {
            val alpha =
                if (mIsSavedInstanceState)
                    1F
                else
                    0F
            mViewHolder.sharedAnimationView.setBackgroundAlpha(alpha)
            mViewHolder.bottomToolbar.alpha = alpha
        } else {
            mViewHolder.sharedAnimationView.setBackgroundAlpha(1.0F)
        }
        mViewHolder.sharedAnimationView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        mViewHolder.sharedAnimationView.setOnSharedAnimationViewListener(object :
            OnSharedAnimationViewListener {
            override fun onBeginBackMinAnim() {
                // 开始 退出共享动画
                this@BasePreviewFragment2.onSharedBeginBackMinAnim()
            }

            override fun onBeginBackMinMagicalFinish(isResetSize: Boolean) {
                this@BasePreviewFragment2.onSharedBeginBackMinFinish(isResetSize)
            }

            override fun onBeginSharedAnimComplete(
                sharedAnimationView: SharedAnimationView,
                showImmediately: Boolean
            ) {
                // 开始共享动画完成后
                this@BasePreviewFragment2.onSharedBeginAnimComplete(
                    sharedAnimationView,
                    showImmediately
                )
            }

            override fun onBackgroundAlpha(alpha: Float) {
                this@BasePreviewFragment2.onBackgroundAlpha(alpha)
            }

            override fun onMagicalViewFinish() {
                this@BasePreviewFragment2.onSharedViewFinish()
            }
        })
    }

    /**
     * 初始化ViewPager2
     */
    private fun initViewPagerData() {
        mAdapter = PreviewPagerAdapter(mContext, requireActivity())
        mAdapter.addAll(getDatas())
        mAdapter.notifyItemRangeChanged(0, getDatas().size)
        mViewPager2.adapter = mAdapter

        // adapter显示view时的触发事件
        mAdapter.setOnFirstAttachedToWindowListener(object :
            PreviewPagerAdapter.OnFirstAttachedToWindowListener {
            override fun onViewFirstAttachedToWindow(holder: PreviewPagerAdapter.PreviewViewHolder) {
                this@BasePreviewFragment2.onViewFirstAttachedToWindow(holder)
            }
        })

        // 多图时滑动事件
        mViewPager2.registerOnPageChangeCallback(mOnPageChangeCallback)
        mViewPager2.setCurrentItem(getPreviewPosition(), false)
    }

    /**
     * 所有事件
     */
    private fun initListener() {
        // 编辑
        mViewHolder.tvEdit.setOnClickListener(object : OnMoreClickListener() {
            override fun onListener(v: View) {
                openImageEditActivity()
            }
        })
        // 返回
        mViewHolder.iBtnBack.setOnClickListener {
            if (isSharedAnimation()) {
                mViewHolder.sharedAnimationView.backToMin()
            } else {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        // 确认
        mViewHolder.buttonApply.setOnClickListener(object : OnMoreClickListener() {
            override fun onListener(v: View) {
                // 确认的一刻赋值
                val localMediaArrayList: java.util.ArrayList<LocalMedia> = getDatas()
                // 设置是否原图状态
                for (localMedia in localMediaArrayList) {
                    localMedia.isOriginal = mOriginalEnable
                }
                setResultOkByIsCompress(true)
            }
        })
        // 右上角选择事件
        mViewHolder.checkView.setOnClickListener {
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
        // 点击原图事件
        mViewHolder.originalLayout.setOnClickListener {
            val count: Int = countOverMaxSize()
            if (count > 0) {
                val incapableDialog = newInstance(
                    "",
                    getString(
                        R.string.z_multi_library_error_over_original_count,
                        count,
                        mAlbumSpec.originalMaxSize
                    )
                )
                incapableDialog.show(
                    parentFragmentManager,
                    IncapableDialog::class.java.name
                )
                return@setOnClickListener
            }
            mOriginalEnable = !mOriginalEnable
            mViewHolder.original.setChecked(mOriginalEnable)
            if (!mOriginalEnable) {
                mViewHolder.original.setColor(Color.WHITE)
            }
            if (mAlbumSpec.onCheckedListener != null) {
                mAlbumSpec.onCheckedListener!!.onCheck(mOriginalEnable)
            }
        }
        // 点击Loading停止
        mViewHolder.pbLoading.setOnClickListener {
            // 中断线程
            mCompressFileTask.cancel()
            // 恢复界面可用
            setControlTouchEnable(true)
        }
    }

    /**
     * 关闭Activity回调相关数值,如果需要压缩，另外弄一套压缩逻辑
     *
     * @param apply 是否同意
     */
    private fun setResultOkByIsCompress(apply: Boolean) {
        // 判断是否需要压缩
        if (mGlobalSpec.imageCompressionInterface != null) {
            if (apply) {
                compressFile()
            } else {
                // 直接返回
                setResultOk(false)
            }
        } else {
            if (apply) {
                moveStoreCompatFile()
            } else {
                // 直接返回
                setResultOk(false)
            }
        }
    }

    /**
     * 刷新MultiMedia
     */
    private fun refreshMultiMediaItem() {
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
     * 是否开启共享动画
     */
    private fun isSharedAnimation(): Boolean {
        return true
    }

    /**
     * 打开编辑的Activity
     */
    private fun openImageEditActivity() {
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
     * 获取当前超过限制原图大小的数量
     * TODO 使用这个方法的逻辑似乎不太对
     *
     * @return 数量
     */
    private fun countOverMaxSize(): Int {
        var count = 0
        val selectedCount: Int = getDatas().count()
        for (i in 0 until selectedCount) {
            val item: LocalMedia = getDatas()[i]
            if (item.isImage()) {
                val size = PhotoMetadataUtils.getSizeInMb(item.size)
                if (size > mAlbumSpec.originalMaxSize) {
                    count++
                }
            }
        }
        return count
    }

    /**
     * 设置返回值
     *
     * @param apply 是否同意
     */
    @Synchronized
    private fun setResultOk(apply: Boolean) {
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
     * 设置是否启用界面触摸，不可禁止中断、退出
     */
    private fun setControlTouchEnable(enable: Boolean) {
        // 如果不可用就显示 加载中 view,否则隐藏
        if (!enable) {
            mViewHolder.pbLoading.visibility = View.VISIBLE
            mViewHolder.buttonApply.visibility = View.GONE
            setCheckViewEnable(false)
            mViewHolder.checkView.setOnClickListener(null)
            mViewHolder.tvEdit.isEnabled = false
            mViewHolder.originalLayout.isEnabled = false
        } else {
            mViewHolder.pbLoading.visibility = View.GONE
            mViewHolder.buttonApply.visibility = View.VISIBLE
            setCheckViewEnable(true)
            mViewHolder.checkView.setOnClickListener {
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
            mViewHolder.tvEdit.isEnabled = true
            mViewHolder.originalLayout.isEnabled = true
        }
    }

    /**
     * 更新确定按钮状态
     */
    private fun updateApplyButton() {
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
     * 判断是否压缩，如果要压缩先要迁移复制再压缩
     */
    private fun compressFile() {
        // 显示loading动画
        setControlTouchEnable(false)

        // 复制相册的文件
        ThreadUtils.executeByIo(mCompressFileTask)
    }

    /**
     * 不压缩，直接移动文件
     */
    private fun moveStoreCompatFile() {
        // 显示loading动画
        setControlTouchEnable(false)

        // 复制相册的文件
        ThreadUtils.executeByIo(mMoveFileTask)
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
    private fun setCheckViewEnable(enable: Boolean) {
        if (mSelectedEnable) {
            mViewHolder.checkView.isEnabled = enable
        } else {
            mViewHolder.checkView.isEnabled = false
        }
    }

    /**
     * 第一次加载Adapter时候触发事件
     *
     * @param holder 预览的view
     */
    open fun onViewFirstAttachedToWindow(holder: PreviewPagerAdapter.PreviewViewHolder) {
        if (mIsSavedInstanceState) {
            return
        }
        if (isSharedAnimation()) {
            setImageViewScaleType(holder, getDatas()[getPreviewPosition()])
        }
    }

    /**
     * 设置图片的scaleType
     * @param holder 预览的view
     * @param media 实体
     */
    open fun setImageViewScaleType(
        holder: PreviewPagerAdapter.PreviewViewHolder,
        media: LocalMedia
    ) {
        if (media.width == 0 && media.height == 0) {
            holder.imageView.scaleType = ScaleType.FIT_CENTER
        } else {
            // 这个才能保持跟RecyclerView的item一样scaleType，不然突兀
            holder.imageView.scaleType = ScaleType.CENTER_CROP
        }
    }

    /**
     * 开启了共享动画
     *
     * @param position 索引
     */
    open fun startSharedAnimation(position: Int) {
        // 先隐藏viewPager,等共享动画结束后，再显示viewPager
        mViewPager2.alpha = 0F
        viewModel.viewModelScope.launch {
            val media = getDatas()[position]
            val mediaRealSize = getMediaRealSizeFromMedia(media)
            val width = mediaRealSize[0]
            val height = mediaRealSize[1]
            val viewParams = RecycleItemViewParams.getItem(getPreviewPosition())

            if (viewParams == null || width == 0 && height == 0) {
                mViewHolder.sharedAnimationView.startNormal(width, height, false)
                mViewHolder.sharedAnimationView.setBackgroundAlpha(1F)
                mViewHolder.bottomToolbar.alpha = 1F
            } else {
                // 将记录好的RecyclerView的位置大小，进行动画扩大到width,height
                mViewHolder.sharedAnimationView.setViewParams(
                    viewParams.left,
                    viewParams.top,
                    viewParams.width,
                    viewParams.height,
                    width,
                    height
                )
                mViewHolder.sharedAnimationView.start(false)
            }
            // 50毫秒时间渐变显示viewPager2,这样可以不会太生硬
            val objectAnimator = ObjectAnimator.ofFloat(mViewPager2, "alpha", 0F, 1F)
            objectAnimator.duration = 50
            objectAnimator.start()
        }
    }

    /**
     * 设置共享参数，主要是为了退出时的共享动画
     */
    open fun setSharedAnimationViewParams(position: Int) {
        viewModel.viewModelScope.launch {
            val media = getDatas()[position]
            val mediaSize = getMediaRealSizeFromMedia(media)
            val width = mediaSize[0]
            val height = mediaSize[1]
            val viewParams = RecycleItemViewParams.getItem(getPreviewPosition())
            if (viewParams == null || width == 0 || height == 0) {
                mViewHolder.sharedAnimationView.setViewParams(0, 0, 0, 0, width, height)
            } else {
                mViewHolder.sharedAnimationView.setViewParams(
                    viewParams.left,
                    viewParams.top,
                    viewParams.width,
                    viewParams.height,
                    width,
                    height
                )
            }
        }
    }

    /**
     * 获取LocalMedia的宽高
     * @param media 文件
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun getMediaRealSizeFromMedia(media: LocalMedia): IntArray {
        var realWidth = media.width
        var realHeight = media.height
        if (MediaUtils.isLongImage(realWidth, realHeight)) {
            return intArrayOf(screenWidth, screenHeight)
        }
        // 如果宽高其中一个<=0  重新获取宽高。如果宽度大于高度也要重新获取，因为有可能是横拍，要根据角度判断重新反转宽高
        if ((realWidth <= 0 || realHeight <= 0) || (realWidth > realHeight)) {
            withContext(Dispatchers.IO) {
                media.absolutePath.let { absolutePath ->
                    MediaUtils.getMediaInfo(requireContext(), media.mimeType, absolutePath).let {
                        if (it.width > 0) {
                            realWidth = it.width
                        }
                        if (it.height > 0) {
                            realHeight = it.height
                        }
                    }
                }
            }
        }
        // 如果是裁剪后的图片，宽高是会改变的 TODO
//        if ((media.isCrop() || media.isEditor()) && media.cropWidth > 0 && media.cropHeight > 0) {
//            realWidth = media.cropWidth
//            realHeight = media.cropHeight
//        }
        return intArrayOf(realWidth, realHeight)
    }

    /**
     * 开始共享动画完成后
     */
    open fun onSharedBeginAnimComplete(
        sharedAnimationView: SharedAnimationView?,
        showImmediately: Boolean
    ) {
        val currentHolder = mAdapter.getCurrentViewHolder(mViewPager2.currentItem) ?: return
        val media = getDatas()[mViewPager2.currentItem]
//        val isResetSize = (media.isCrop() || media.isEditor()) && media.cropWidth > 0 && media.cropHeight > 0
//        val realWidth = if (isResetSize) media.cropWidth else media.width
//        val realHeight = if (isResetSize) media.cropHeight else media.height
        val realWidth = media.width
        val realHeight = media.height
        if (MediaUtils.isLongImage(realWidth, realHeight)) {
            currentHolder.imageView.scaleType = ScaleType.CENTER_CROP
        } else {
            currentHolder.imageView.scaleType = ScaleType.FIT_CENTER
        }
    }

    /**
     * 开始 退出共享动画
     */
    open fun onSharedBeginBackMinAnim() {
        val currentHolder = mAdapter.getCurrentViewHolder(mViewPager2.currentItem) ?: return
        if (currentHolder.imageView.visibility == View.GONE) {
            currentHolder.imageView.visibility = View.VISIBLE
        }
        if (currentHolder.videoPlayButton.visibility == View.VISIBLE) {
            currentHolder.videoPlayButton.visibility = View.GONE
        }
    }

    /**
     * 结束 退出共享动画
     * 设置预览 view 跟相册的 view 一样的高度宽度
     */
    open fun onSharedBeginBackMinFinish(isResetSize: Boolean) {
        val itemViewParams =
            RecycleItemViewParams.getItemViewParams(mViewPager2.currentItem) ?: return
        val currentHolder = mAdapter.getCurrentViewHolder(mViewPager2.currentItem) ?: return
        val layoutParams = currentHolder.imageView.layoutParams
        layoutParams?.width = itemViewParams.width
        layoutParams?.height = itemViewParams.height
        currentHolder.imageView.scaleType = ScaleType.CENTER_CROP
    }

    open fun onBackgroundAlpha(alpha: Float) {
        mViewHolder.sharedAnimationView.setBackgroundAlpha(alpha)
        mViewHolder.bottomToolbar.alpha = alpha
    }

    /**
     * 共享动画结束，退出fragment
     */
    open fun onSharedViewFinish() {
        requireActivity().supportFragmentManager.popBackStack()
    }


    /**
     * 滑动事件
     */
    open fun onViewPageSelected(position: Int) {
        setPreviewPosition(position)
//        setTitleText(position + 1)
        if (mFirstSharedAnimation) {
            startSharedAnimation(position)
            mFirstSharedAnimation = false
        } else {
            setSharedAnimationViewParams(position)
        }

//        if (isLoadMoreThreshold(position)) {
//            loadMediaMore()
//        }
//        if (isPlayPageSelected) {
//            if (config.isAutoPlay) {
//                autoPlayAudioAndVideo()
//            } else {
//                val currentHolder = mAdapter.getCurrentViewHolder(viewPager.currentItem)
//                if (currentHolder is PreviewVideoHolder) {
//                    if (currentHolder.ivPlay.visibility == View.GONE) {
//                        currentHolder.ivPlay.visibility = View.VISIBLE
//                    }
//                }
//            }
//        }
//        isPlayPageSelected = true
    }

    /**
     * 更新ui
     * 如果当前item是gif就显示多少M的文本
     * 如果当前item是video就显示播放按钮
     *
     * @param item 当前图片
     */
    @SuppressLint("SetTextI18n")
    private fun updateUi(item: LocalMedia?) {
        item?.let {
            if (item.isGif()) {
                mViewHolder.tvSize.visibility = View.VISIBLE
                mViewHolder.tvSize.text = PhotoMetadataUtils.getSizeInMb(item.size).toString() + "M"
            } else {
                mViewHolder.tvSize.visibility = View.GONE
            }

            // 判断是否开启原图,并且是从相册界面进来才开启原图，同时原图不支持video
            if (mAlbumSpec.originalEnable && mOriginalEnable && !item.isVideo()) {
                // 显示
                mViewHolder.originalLayout.visibility = View.VISIBLE
                updateOriginalState()
            } else {
                // 隐藏
                mViewHolder.originalLayout.visibility = View.GONE
            }
            if (item.isImage() && mGlobalSpec.imageEditEnabled && mEditEnable) {
                mViewHolder.tvEdit.visibility = View.VISIBLE
            } else {
                mViewHolder.tvEdit.visibility = View.GONE
            }
        }

    }

    /**
     * 更新原图按钮状态
     */
    private fun updateOriginalState() {
        // 设置原图按钮根据配置来
        mViewHolder.original.setChecked(mOriginalEnable)
        if (!mOriginalEnable) {
            mViewHolder.original.setColor(Color.WHITE)
        }
        if (countOverMaxSize() > 0) {
            // 如果开启了原图功能
            if (mOriginalEnable) {
                // 弹框提示取消原图
                val incapableDialog = newInstance(
                    "",
                    getString(
                        R.string.z_multi_library_error_over_original_size,
                        mAlbumSpec.originalMaxSize
                    )
                )
                incapableDialog.show(
                    parentFragmentManager,
                    IncapableDialog::class.java.name
                )
                // 去掉原图按钮的选择状态
                mViewHolder.original.setChecked(false)
                mViewHolder.original.setColor(Color.WHITE)
                mOriginalEnable = false
            }
        }
    }

    /**
     * 滑动事件
     */
    private val mOnPageChangeCallback: OnPageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val adapter = mViewPager2.adapter as PreviewPagerAdapter
            val item = adapter.getLocalMedia(position)
            if (mAlbumSpec.countable) {
                val checkedNum = getSelectedData().checkedNumOf(item)
                mViewHolder.checkView.setCheckedNum(checkedNum)
                if (checkedNum > 0) {
                    setCheckViewEnable(true)
                } else {
                    setCheckViewEnable(!getSelectedData().maxSelectableReached())
                }
            } else {
                val checked = getSelectedData().isSelected(item)
                mViewHolder.checkView.setChecked(checked)
                if (checked) {
                    setCheckViewEnable(true)
                } else {
                    setCheckViewEnable(!getSelectedData().maxSelectableReached())
                }
            }
            updateUi(item)
            onViewPageSelected(position)
        }
    }

    class ViewHolder internal constructor(var rootView: View) {
        var sharedAnimationView: SharedAnimationView =
            rootView.findViewById(R.id.sharedAnimationView)
        var iBtnBack: ImageButton = rootView.findViewById(R.id.ibtnBack)
        var tvEdit: TextView = rootView.findViewById(R.id.tvEdit)
        var original: CheckRadioView = rootView.findViewById(R.id.original)
        var originalLayout: View = rootView.findViewById(R.id.originalLayout)
        var tvSize: TextView = rootView.findViewById(R.id.tvSize)
        var buttonApply: TextView = rootView.findViewById(R.id.buttonApply)
        var bottomToolbar: ConstraintLayout = rootView.findViewById(R.id.bottomToolbar)
        var checkView: CheckView = rootView.findViewById(R.id.checkView)
        var pbLoading: ProgressBar = rootView.findViewById(R.id.pbLoading)
    }

}