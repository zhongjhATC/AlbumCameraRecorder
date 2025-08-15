package com.zhongjh.multimedia.preview

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView.ScaleType
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.zhongjh.common.entity.IncapableCause
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MediaType.Companion.TYPE_PICTURE
import com.zhongjh.common.listener.OnMoreClickListener
import com.zhongjh.common.utils.DisplayMetricsUtils.getScreenHeight
import com.zhongjh.common.utils.DisplayMetricsUtils.getScreenWidth
import com.zhongjh.common.utils.MediaUtils
import com.zhongjh.common.utils.StatusBarUtils.initStatusBar
import com.zhongjh.common.utils.ThreadUtils
import com.zhongjh.common.utils.ThreadUtils.SimpleTask
import com.zhongjh.imageedit.ImageEditActivity
import com.zhongjh.multimedia.BaseFragment
import com.zhongjh.multimedia.MainActivity
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.album.utils.AlbumCompressFileTask
import com.zhongjh.multimedia.album.utils.PhotoMetadataUtils
import com.zhongjh.multimedia.album.widget.CheckRadioView
import com.zhongjh.multimedia.album.widget.CheckView
import com.zhongjh.multimedia.model.MainModel
import com.zhongjh.multimedia.model.OriginalManage
import com.zhongjh.multimedia.model.SelectedData.Companion.STATE_SELECTION
import com.zhongjh.multimedia.model.SelectedModel
import com.zhongjh.multimedia.preview.adapter.PreviewPagerAdapter
import com.zhongjh.multimedia.preview.enum.PreviewType
import com.zhongjh.multimedia.preview.start.PreviewSetting
import com.zhongjh.multimedia.settings.AlbumSpec
import com.zhongjh.multimedia.settings.GlobalSpec
import com.zhongjh.multimedia.sharedanimation.OnSharedAnimationViewListener
import com.zhongjh.multimedia.sharedanimation.RecycleItemViewParams
import com.zhongjh.multimedia.sharedanimation.SharedAnimationView
import com.zhongjh.multimedia.utils.FileMediaUtil
import com.zhongjh.multimedia.utils.MediaStoreUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.ref.WeakReference


/**
 * 目标是可以不止该库自用，也可以别的app别的功能直接使用
 *
 * Activity和Fragment的区别:
 * Activity是一个全新的多媒体数据,而Fragment是一个来源于同一个Activity的多媒体数据
 *
 *
 * 预览窗口的基类支持以下功能：
 * 1. 决定是否共享动画
 * 2. 实例化ViewPager
 * 3. 确定后，返回的数据 - 是否压缩
 * 4. 支持放大缩小
 *
 * @author zhongjh
 * @date 2023/8/31
 */
class PreviewFragment : BaseFragment() {

    companion object {
        const val TAG: String = "PreviewFragment"
    }

    private lateinit var mContext: Context
    private lateinit var mMediaController: MediaController
    private lateinit var mViewHolder: ViewHolder
    private lateinit var mViewPager2: ViewPager2
    private lateinit var mAdapter: PreviewPagerAdapter
    private val mGlobalSpec by lazy {
        GlobalSpec
    }
    private val mAlbumSpec by lazy {
        AlbumSpec
    }

    private val mMainModel by lazy {
        val activity = requireActivity()
        val savedStateViewModelFactory = SavedStateViewModelFactory(activity.application, this)
        return@lazy ViewModelProvider(
            activity, savedStateViewModelFactory
        )[MainModel::class.java]
    }

    private val mSelectedModel by lazy {
        val activity = requireActivity()
        val savedStateViewModelFactory = SavedStateViewModelFactory(activity.application, this)
        return@lazy ViewModelProvider(
            activity, savedStateViewModelFactory
        )[SelectedModel::class.java]
    }

    /**
     * 打开ImageEditActivity的回调
     */
    private lateinit var mImageEditActivityResult: ActivityResultLauncher<Intent>

    /**
     * 统一管理原图有关功能模块
     */
    private val mOriginalManage by lazy {
        OriginalManage(this, mMainModel, mSelectedModel, mAlbumSpec)
    }

    /**
     * 完成压缩-复制的异步线程
     */
    private val mCompressFileTask: SimpleTask<ArrayList<LocalMedia>> by lazy {
        object : SimpleTask<ArrayList<LocalMedia>>() {
            private val weakFragment = WeakReference(this@PreviewFragment)

            override fun doInBackground(): ArrayList<LocalMedia> {
                handleEditImages()
                val fragment = weakFragment.get()
                fragment?.let {
                    // 是否只压缩编辑的图片
                    val isOnlyCompressEditPicture = fragment.mPreviewType == PreviewType.GRID || fragment.mPreviewType == PreviewType.THIRD_PARTY
                    return fragment.mAlbumCompressFileTask.compressFileTaskDoInBackground(fragment.mSelectedModel.selectedData.localMedias, isOnlyCompressEditPicture)
                } ?: let {
                    return ArrayList()
                }
            }

            override fun onSuccess(result: ArrayList<LocalMedia>) {
                val fragment = weakFragment.get()
                fragment?.setResultOk(result)
            }

            override fun onFail(t: Throwable) {
                val fragment = weakFragment.get()
                fragment?.let {
                    super.onFail(t)
                    // 结束loading
                    fragment.setControlTouchEnable(true)
                    Toast.makeText(fragment.mContext, t.message, Toast.LENGTH_SHORT).show()
                    t.message?.let {
                        Log.e(TAG, it)
                    }
                }
            }

            override fun onCancel() {
                val fragment = weakFragment.get()
                fragment?.let {
                    super.onCancel()
                    // 结束loading
                    fragment.setControlTouchEnable(true)
                }
            }
        }
    }

    /**
     * 异步线程的逻辑，确定当前选择的文件列表，根据是否压缩配置决定重新返回新的文件列表
     */
    private val mAlbumCompressFileTask by lazy {
        AlbumCompressFileTask(mContext, TAG, PreviewFragment::class.java, mGlobalSpec)
    }

    /**
     * 当前编辑完的图片文件
     */
    private var mEditImagePath: String? = null

    private var screenWidth = 0
    private var screenHeight = 0

    /**
     * 是否从界面恢复回来的
     */
    private var mIsSavedInstanceState = false

    /**
     * 设置是否启动确定功能
     */
    private var mApplyEnable = true

    /**
     * 设置是否启动选择功能
     */
    private var mSelectedEnable = true

    /**
     * 设置是否开启编辑功能
     */
    private var mEditEnable = true

    /**
     * 设置是否开启原图
     */
    private var mOriginalEnable = true

    /**
     * 是否编辑了图片
     */
    private var mIsEdit = false

    /**
     * 设置右上角是否检测类型
     */
    private var mIsSelectedCheck = true

    /**
     * 预览界面类型，代表从什么界面打开
     */
    private var mPreviewType = PreviewType.ALBUM_ACTIVITY

    /**
     * 是否首次共享动画，只有第一次打开的时候才触发共享动画
     */
    private var mFirstSharedAnimation = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context.applicationContext
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // 获取样式
        return initStyle(inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 获取宽高
        screenWidth = getScreenWidth(requireContext())
        screenHeight = getScreenHeight(requireContext())
        // 初始化回调
        initActivityResult()
        // 初始化状态栏
        initStatusBar(requireActivity())
        // 初始化bundle的Value
        initBundleValue(savedInstanceState)
        mMediaController = MediaController(activity)
        mViewHolder = ViewHolder(view)
        mViewHolder.checkView.setCountable(mAlbumSpec.countable)
        // 初始化共享动画view
        initSharedAnimationView()
        initViewPagerData()
        initListener()
        initObserveData()
        updateApplyButton()
    }

    override fun onBackPressed(): Boolean {
        return if (isSharedAnimation()) {
            mViewHolder.sharedAnimationView.backToMin()
            true
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        mViewPager2.unregisterOnPageChangeCallback(mOnPageChangeCallback)
        mAdapter.onDestroy()
        // 如果依附的Activity是MainActivity,就显示底部控件动画
        if (requireActivity() is MainActivity) {
            (requireActivity() as MainActivity).showHideTableLayoutAnimator(true)
        }
        super.onDestroy()
    }

    /**
     * 初始化样式
     */
    private fun initStyle(inflater: LayoutInflater, container: ViewGroup?): View {
        val wrapper = ContextThemeWrapper(mContext, mGlobalSpec.themeId)
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
            if (result.resultCode == RESULT_OK) {
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
                // 设置是否启动 '确定' 功能
                mApplyEnable = it.getBoolean(PreviewSetting.APPLY_ENABLE, true)
                // 设置是否启动 '选择' 功能
                mSelectedEnable = it.getBoolean(PreviewSetting.SELECTED_ENABLE, true)
                // 验证当前item是否满足可以被选中的条件
                mIsSelectedCheck = it.getBoolean(PreviewSetting.IS_SELECTED_CHECK, true)
                // 预览类型，表达从什么界面打开进来的
                mPreviewType = it.getSerializable(PreviewSetting.PREVIEW_TYPE) as PreviewType
                // 设置是否开启编辑功能
                mEditEnable = it.getBoolean(PreviewSetting.EDIT_ENABLE, true)
                // 设置是否开启原图
                mOriginalEnable = it.getBoolean(PreviewSetting.EXTRA_RESULT_ORIGINAL_ENABLE, true)
                // 数据源
                it.getParcelableArrayList<LocalMedia>(PreviewSetting.PREVIEW_DATA)?.let { selection ->
                    val localMedias = selection as ArrayList<LocalMedia>
                    mMainModel.localMedias.addAll(localMedias)
                    mSelectedModel.selectedData.addAll(localMedias)
                    mMainModel.previewPosition = it.getInt(PreviewSetting.CURRENT_POSITION, 0)
                }
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
            val alpha = if (mIsSavedInstanceState) 1F else 0F
            mViewHolder.sharedAnimationView.setBackgroundAlpha(alpha)
            mViewHolder.bottomToolbar.alpha = alpha
            mViewHolder.constraintLayout.alpha = alpha
        } else {
            mViewHolder.sharedAnimationView.setBackgroundAlpha(1.0F)
        }
        mViewHolder.sharedAnimationView.setBackgroundColor(Color.BLACK)
        mViewHolder.sharedAnimationView.setOnSharedAnimationViewListener(object : OnSharedAnimationViewListener {
            override fun onBeginBackMinAnim() {
                // 开始 退出共享动画
                this@PreviewFragment.onSharedBeginBackMinAnim()
            }

            override fun onBeginBackMinMagicalFinish(isResetSize: Boolean) {
                this@PreviewFragment.onSharedBeginBackMinFinish(isResetSize)
            }

            override fun onBeginSharedAnimComplete(sharedAnimationView: SharedAnimationView, showImmediately: Boolean) {
                // 开始共享动画完成后
                this@PreviewFragment.onSharedBeginAnimComplete(sharedAnimationView, showImmediately)
            }

            override fun onBackgroundAlpha(alpha: Float) {
                this@PreviewFragment.onBackgroundAlpha(alpha)
            }

            override fun onMagicalViewFinish() {
                this@PreviewFragment.onSharedViewFinish()
            }
        })
    }

    /**
     * 初始化ViewPager2
     */
    private fun initViewPagerData() {
        mAdapter = PreviewPagerAdapter(mContext, requireActivity())
        mAdapter.addAll(mMainModel.localMedias)
        mAdapter.notifyItemRangeChanged(0, mMainModel.localMedias.size)
        mViewPager2.adapter = mAdapter

        // adapter显示view时的触发事件
        mAdapter.setOnListener(object : PreviewPagerAdapter.OnListener {
            override fun onViewFirstAttachedToWindow(holder: PreviewPagerAdapter.PreviewViewHolder) {
                this@PreviewFragment.onViewFirstAttachedToWindow(holder)
            }

            override fun onVideoPlay(videoView: VideoView) {
                mMediaController.setAnchorView(videoView)
                mMediaController.setMediaPlayer(videoView)
                videoView.setMediaController(mMediaController)
            }
        })

        // 多图时滑动事件
        mViewPager2.registerOnPageChangeCallback(mOnPageChangeCallback)
        mViewPager2.setCurrentItem(mMainModel.previewPosition, false)
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
                val localMediaArrayList: ArrayList<LocalMedia> = mSelectedModel.selectedData.localMedias
                // 设置是否原图状态
                for (localMedia in localMediaArrayList) {
                    localMedia.isOriginal = mMainModel.getOriginalEnable()
                }
                setResultOkByIsCompress()
            }
        })
        // 右上角选择事件
        mViewHolder.checkView.setOnClickListener {
            val media = mMainModel.localMedias[mViewPager2.currentItem]
            if (mSelectedModel.selectedData.isSelected(media)) {
                mSelectedModel.removeSelectedData(media)
                if (mAlbumSpec.countable) {
                    mViewHolder.checkView.setCheckedNum(CheckView.UNCHECKED)
                } else {
                    mViewHolder.checkView.setChecked(false)
                }
            } else {
                var isTrue = true
                if (mIsSelectedCheck) {
                    isTrue = assertAddSelection(media)
                }
                if (isTrue) {
                    mSelectedModel.addSelectedData(media)
                    if (mAlbumSpec.countable) {
                        mViewHolder.checkView.setCheckedNum(
                            mSelectedModel.selectedData.checkedNumOf(
                                media
                            )
                        )
                    } else {
                        mViewHolder.checkView.setChecked(true)
                    }
                }
            }
            updateApplyButton()
            mAlbumSpec.onSelectedListener?.let {
                if (mPreviewType == PreviewType.ALBUM_ACTIVITY || mPreviewType == PreviewType.ALBUM_FRAGMENT) {
                    // 触发选择的接口事件
                    it.onSelected(mSelectedModel.selectedData.localMedias)
                }
            }
        }
        // 点击原图事件
        mViewHolder.originalLayout.setOnClickListener {
            mOriginalManage.originalClick()
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
     * 初始化数据的监控
     */
    private fun initObserveData() {
        // 原图选项改变
        mMainModel.getOriginalEnableObserve().observe(viewLifecycleOwner) { value: Boolean ->
            mViewHolder.original.setChecked(value)
        }
        // 相册界面移动完成后触发
        mMainModel.onScrollToPositionComplete.observe(viewLifecycleOwner) { value: Int ->
            setSharedAnimationViewParams(value)
        }
    }

    /**
     * 关闭Activity回调相关数值
     */
    private fun setResultOkByIsCompress() {
        if (mPreviewType == PreviewType.CAMERA || mPreviewType == PreviewType.GRID || mPreviewType == PreviewType.THIRD_PARTY) {
            // 直接返回
            setResultOk(mSelectedModel.selectedData.localMedias)
        } else {
            // 其他界面就要先压缩
            compressFile()
        }
    }

    /**
     * 刷新MultiMedia
     */
    private fun refreshMultiMediaItem() {
        // 获取当前查看的multimedia
        val localMedia = mAdapter.getLocalMedia(mViewPager2.currentItem)
        // 更新当前fragment编辑后的uri和path
        localMedia?.let {
            localMedia.editorPath = mEditImagePath
            mAdapter.setLocalMedia(mViewPager2.currentItem, it)
            mAdapter.notifyItemChanged(mViewPager2.currentItem)
        }
    }

    /**
     * 打开编辑的Activity
     */
    private fun openImageEditActivity() {
        val item = mAdapter.getLocalMedia(mMainModel.previewPosition)
        item?.let {
            val file = FileMediaUtil.createCacheFile(mContext, TYPE_PICTURE)
            mEditImagePath = file.absoluteFile.toString()
            val intent = Intent()
            intent.setClass(requireActivity(), ImageEditActivity::class.java)
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SCREEN_ORIENTATION, requireActivity().requestedOrientation)
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, item.uri)
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SAVE_PATH, file.absolutePath)
            mImageEditActivityResult.launch(intent)
        }
    }

    /**
     * 判断所有编辑的图片是否加入相册
     * 如果加入相册后，更新所有数据
     */
    private fun handleEditImages() {
        // 循环所有数据
        for (localMedia in mAdapter.getLocalMedias()) {
            val editFile = localMedia.editorPath?.let { File(it) }
            editFile?.let {
                // 加入相册条件：1.相册界面直接打开的预览 2. 编辑过的
                if ((mPreviewType == PreviewType.ALBUM_ACTIVITY || mPreviewType == PreviewType.ALBUM_FRAGMENT) && mGlobalSpec.isAddAlbumByEdit) {
                    val uri = MediaStoreUtils.displayToGallery(mContext, editFile, TYPE_PICTURE, localMedia.duration, localMedia.width, localMedia.height)
                    uri?.let {
                        localMedia.absolutePath = localMedia.editorPath as String
                        localMedia.uri = uri.toString()
                        // 从Uri中提取ID（最后一段数字）
                        localMedia.fileId = uri.lastPathSegment?.toLongOrNull() ?: -1
                        // 宽高刷新
                        val imageWidthAndHeight: IntArray = MediaUtils.getImageWidthAndHeight(localMedia.absolutePath)
                        localMedia.width = imageWidthAndHeight[0]
                        localMedia.height = imageWidthAndHeight[1]
                        // 大小
                        localMedia.size = editFile.length()
                        // 文件名称
                        localMedia.fileName = editFile.name
                        // 时间
                        localMedia.dateAddedTime = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    /**
     * 设置返回值
     */
    @Synchronized
    private fun setResultOk(localMedias: ArrayList<LocalMedia>) {
        Log.d(TAG, "setResultOk")
        val intent = Intent()
        intent.putExtra(STATE_SELECTION, localMedias)
        requireActivity().setResult(RESULT_OK, intent)
        requireActivity().finish()
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
        // 获取已选的图片
        if (mSelectedModel.selectedData.count() == 0) {
            // 禁用
            mViewHolder.buttonApply.setText(R.string.z_multi_library_button_sure_default)
            mViewHolder.buttonApply.isEnabled = false
        } else if (mSelectedModel.selectedData.count() == 1 && mAlbumSpec.singleSelectionModeEnabled()) {
            // 如果只选择一张或者配置只能选一张，或者不显示数字的时候。启用，不显示数字
            mViewHolder.buttonApply.setText(R.string.z_multi_library_button_sure_default)
            mViewHolder.buttonApply.isEnabled = true
        } else {
            // 启用，显示数字
            mViewHolder.buttonApply.isEnabled = true
            mViewHolder.buttonApply.text = getString(R.string.z_multi_library_button_sure, mSelectedModel.selectedData.count())
        }

        // 判断是否启动操作
        if (!mApplyEnable) {
            mViewHolder.checkView.visibility = View.GONE
            mViewHolder.buttonApply.visibility = View.GONE
        } else {
            mViewHolder.checkView.visibility = View.VISIBLE
            mViewHolder.buttonApply.visibility = View.VISIBLE
        }
        setCheckViewEnable(mSelectedEnable)
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
     * 处理窗口
     *
     * @param item 当前图片
     * @return 为true则代表符合规则
     */
    private fun assertAddSelection(item: LocalMedia): Boolean {
        val cause = mSelectedModel.selectedData.isAcceptable(item)
        IncapableCause.handleCause(mContext, cause)
        return cause == null
    }

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
     * 是否开启共享动画
     */
    private fun isSharedAnimation(): Boolean {
        return mPreviewType == PreviewType.ALBUM_FRAGMENT
    }

    /**
     * 第一次加载Adapter时候触发事件
     *
     * @param holder 预览的view
     */
    private fun onViewFirstAttachedToWindow(holder: PreviewPagerAdapter.PreviewViewHolder) {
        if (mIsSavedInstanceState) {
            return
        }
        if (isSharedAnimation()) {
            setImageViewScaleType(holder, mMainModel.localMedias[mMainModel.previewPosition])
        }
    }

    /**
     * 设置图片的scaleType
     * @param holder 预览的view
     * @param media 实体
     */
    private fun setImageViewScaleType(
        holder: PreviewPagerAdapter.PreviewViewHolder, media: LocalMedia
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
    private fun startSharedAnimation(position: Int) {
        // 先隐藏viewPager,等共享动画结束后，再显示viewPager
        mViewPager2.alpha = 0F
        mMainModel.viewModelScope.launch {
            val media = mMainModel.localMedias[position]
            val mediaRealSize = getMediaRealSizeFromMedia(media)
            val width = mediaRealSize[0]
            val height = mediaRealSize[1]
            val viewParams = RecycleItemViewParams.getItem(mMainModel.previewPosition)

            if (viewParams == null || width == 0 && height == 0) {
                mViewHolder.sharedAnimationView.startNormal(width, height, false)
                mViewHolder.sharedAnimationView.setBackgroundAlpha(1F)
                mViewHolder.bottomToolbar.alpha = 1F
                mViewHolder.constraintLayout.alpha = 1F
            } else {
                // 将记录好的RecyclerView的位置大小，进行动画扩大到width,height
                mViewHolder.sharedAnimationView.setViewParams(
                    viewParams.left, viewParams.top, viewParams.width, viewParams.height, width, height
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
    private fun setSharedAnimationViewParams(position: Int) {
        mMainModel.viewModelScope.launch {
            val media = mMainModel.localMedias[position]
            val mediaSize = getMediaRealSizeFromMedia(media)
            val width = mediaSize[0]
            val height = mediaSize[1]
            val viewParams = RecycleItemViewParams.getItem(mMainModel.previewPosition)
            if (viewParams == null || width == 0 || height == 0) {
                mViewHolder.sharedAnimationView.setViewParams(0, 0, 0, 0, width, height)
            } else {
                mViewHolder.sharedAnimationView.setViewParams(
                    viewParams.left, viewParams.top, viewParams.width, viewParams.height, width, height
                )
            }
        }
    }

    /**
     * 获取LocalMedia的宽高
     * @param media 文件
     */
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
                    MediaUtils.getMediaInfo(requireContext(), media.getMediaType(), absolutePath).let {
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
    private fun onSharedBeginAnimComplete(sharedAnimationView: SharedAnimationView?, showImmediately: Boolean) {
        val currentHolder = mAdapter.getCurrentViewHolder(mViewPager2.currentItem) ?: return
        val media = mMainModel.localMedias[mViewPager2.currentItem]
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
    private fun onSharedBeginBackMinAnim() {
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
    private fun onSharedBeginBackMinFinish(isResetSize: Boolean) {
        val itemViewParams = RecycleItemViewParams.getItemViewParams(mViewPager2.currentItem) ?: return
        val currentHolder = mAdapter.getCurrentViewHolder(mViewPager2.currentItem) ?: return
        val layoutParams = currentHolder.imageView.layoutParams
        layoutParams?.width = itemViewParams.width
        layoutParams?.height = itemViewParams.height
        currentHolder.imageView.scaleType = ScaleType.CENTER_CROP
    }

    private fun onBackgroundAlpha(alpha: Float) {
        mViewHolder.sharedAnimationView.setBackgroundAlpha(alpha)
        mViewHolder.bottomToolbar.alpha = alpha
        mViewHolder.constraintLayout.alpha = alpha
    }

    /**
     * 共享动画结束，退出fragment
     */
    private fun onSharedViewFinish() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    /**
     * 滑动事件
     */
    private fun onViewPageSelected(position: Int) {
        mMainModel.previewPosition = position
        if (isSharedAnimation()) {
            if (mFirstSharedAnimation) {
                startSharedAnimation(position)
                mFirstSharedAnimation = false
            } else {
                // 获取目前可见的view,如果不可见,则将RecyclerView移动到可见
                if (position < RecycleItemViewParams.firstPosition || position > RecycleItemViewParams.lastPosition) {
                    // 移动相册的位置,并且重新赋值RecycleItemViewParams
                    mMainModel.onViewPageSelected(position)
                } else {
                    setSharedAnimationViewParams(position)
                }
            }
        }
    }

    /**
     * 更新ui
     * 如果当前item是gif就显示多少M的文本
     * 如果当前item是video就显示播放按钮
     *
     * @param item 当前图片
     */
    @SuppressLint("SetTextI18n")
    private fun updateUi(item: LocalMedia) {
        if (item.isGif()) {
            mViewHolder.tvSize.visibility = View.VISIBLE
            mViewHolder.tvSize.text = "(${PhotoMetadataUtils.getSizeInMb(item.size)}M)"
        } else {
            mViewHolder.tvSize.visibility = View.GONE
        }

        // 判断是否开启原图,并且是从相册界面进来才开启原图，同时原图不支持video
        if (mAlbumSpec.originalEnable && !item.isVideo() && mOriginalEnable) {
            // 显示
            mViewHolder.groupOriginal.visibility = View.VISIBLE
            updateOriginalState()
        } else {
            // 隐藏
            mViewHolder.groupOriginal.visibility = View.GONE
        }
        if (item.isImage() && mGlobalSpec.imageEditEnabled && mEditEnable) {
            mViewHolder.tvEdit.visibility = View.VISIBLE
        } else {
            mViewHolder.tvEdit.visibility = View.GONE
        }
    }

    /**
     * 更新原图按钮状态
     */
    private fun updateOriginalState() {
        // 设置原图按钮根据配置来
        mViewHolder.original.setChecked(mMainModel.getOriginalEnable())
        mOriginalManage.updateOriginalState()
    }

    /**
     * 滑动事件
     */
    private val mOnPageChangeCallback: OnPageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val adapter = mViewPager2.adapter as PreviewPagerAdapter
            val item = adapter.getLocalMedia(position)
            item?.let {
                if (mAlbumSpec.countable) {
                    val checkedNum = mSelectedModel.selectedData.checkedNumOf(item)
                    mViewHolder.checkView.setCheckedNum(checkedNum)
                    if (checkedNum > 0) {
                        setCheckViewEnable(true)
                    } else {
                        setCheckViewEnable(!mSelectedModel.selectedData.maxSelectableReached())
                    }
                } else {
                    val checked = mSelectedModel.selectedData.isSelected(item)
                    mViewHolder.checkView.setChecked(checked)
                    if (checked) {
                        setCheckViewEnable(true)
                    } else {
                        setCheckViewEnable(!mSelectedModel.selectedData.maxSelectableReached())
                    }
                }
                updateUi(item)
            }
            onViewPageSelected(position)
        }
    }

    class ViewHolder internal constructor(rootView: View) {
        var sharedAnimationView: SharedAnimationView = rootView.findViewById(R.id.sharedAnimationView)
        var iBtnBack: ImageButton = rootView.findViewById(R.id.iBtnBack)
        var tvEdit: TextView = rootView.findViewById(R.id.tvEdit)
        var groupOriginal: Group = rootView.findViewById(R.id.groupOriginal)
        var original: CheckRadioView = rootView.findViewById(R.id.original)
        var tvOriginal: TextView = rootView.findViewById(R.id.tvOriginal)
        var originalLayout: View = rootView.findViewById(R.id.originalLayout)
        var tvSize: TextView = rootView.findViewById(R.id.tvSize)
        var buttonApply: TextView = rootView.findViewById(R.id.buttonApply)
        var bottomToolbar: ConstraintLayout = rootView.findViewById(R.id.bottomToolbar)
        var constraintLayout: ConstraintLayout = rootView.findViewById(R.id.constraintLayout)
        var checkView: CheckView = rootView.findViewById(R.id.checkView)
        var pbLoading: ProgressBar = rootView.findViewById(R.id.pbLoading)
    }

}