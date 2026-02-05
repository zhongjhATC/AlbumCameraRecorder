package com.zhongjh.multimedia.album.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.listener.OnMoreClickListener
import com.zhongjh.common.utils.ColorFilterUtil.setColorFilterSrcIn
import com.zhongjh.common.utils.DisplayMetricsUtils.dip2px
import com.zhongjh.common.utils.DisplayMetricsUtils.getScreenHeight
import com.zhongjh.common.utils.DoubleUtils.isFastDoubleClick
import com.zhongjh.common.utils.StatusBarUtils.getStatusBarHeight
import com.zhongjh.common.utils.request
import com.zhongjh.multimedia.MainActivity
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.album.entity.Album
import com.zhongjh.multimedia.album.entity.AlbumSpinnerStyle
import com.zhongjh.multimedia.album.ui.manager.BottomToolbarManager
import com.zhongjh.multimedia.album.ui.manager.TvAlbumPermissionManager
import com.zhongjh.multimedia.album.ui.mediaselection.MediaViewUtil
import com.zhongjh.multimedia.album.ui.mediaselection.adapter.AlbumAdapter
import com.zhongjh.multimedia.album.utils.AlbumCompressFileTask
import com.zhongjh.multimedia.album.widget.albumspinner.AlbumSpinner
import com.zhongjh.multimedia.album.widget.albumspinner.OnAlbumItemClickListener
import com.zhongjh.multimedia.databinding.FragmentAlbumZjhBinding
import com.zhongjh.multimedia.model.MainModel
import com.zhongjh.multimedia.model.OriginalManage
import com.zhongjh.multimedia.model.SelectedData.Companion.STATE_SELECTION
import com.zhongjh.multimedia.model.SelectedModel
import com.zhongjh.multimedia.preview.start.PreviewStartManager.startPreviewActivityByAlbum
import com.zhongjh.multimedia.preview.start.PreviewStartManager.startPreviewFragmentByAlbum
import com.zhongjh.multimedia.settings.AlbumSpec
import com.zhongjh.multimedia.settings.GlobalSpec
import com.zhongjh.multimedia.sharedanimation.RecycleItemViewParams.add
import com.zhongjh.multimedia.utils.AttrsUtils
import com.zhongjh.multimedia.utils.LifecycleFlowCollector
import com.zhongjh.multimedia.widget.ConstraintLayoutBehavior
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

/**
 * 相册,该Fragment主要处理 顶部的专辑上拉列表 和 底部的功能选项
 * 相册列表具体功能是在MediaViewUtil实现
 *
 * @author zhongjh
 * @date 2018/8/22
 * @noinspection ALL
 */
class AlbumFragment : Fragment(), AlbumAdapter.CheckStateListener, AlbumAdapter.OnMediaClickListener {
    private val tag: String = this@AlbumFragment.javaClass.simpleName

    private lateinit var mApplicationContext: Context
    private val mAlbumModel: AlbumModel by activityViewModels()
    private val mMainModel: MainModel by activityViewModels()
    private val mSelectedModel: SelectedModel by activityViewModels()

    /**
     * View Binding
     */
    private lateinit var mBinding: FragmentAlbumZjhBinding

    /**
     * 从预览界面回来
     */
    private lateinit var mPreviewActivityResult: ActivityResultLauncher<Intent>

    /**
     * 公共配置
     */
    private val mGlobalSpec = GlobalSpec

    /**
     * 相册配置
     */
    private val mAlbumSpec = AlbumSpec

    /**
     * 声明 TvAlbumPermissionManager 实例
     */
    private lateinit var mTvAlbumPermissionManager: TvAlbumPermissionManager

    private lateinit var mBottomToolbarManager: BottomToolbarManager

    /**
     * 统一管理原图有关功能模块
     */
    private lateinit var mOriginalManage: OriginalManage

    /**
     * 专辑下拉框控件
     */
    private var mAlbumSpinner: AlbumSpinner? = null

    /**
     * 单独处理相册数据源的类
     */
    private var mMediaViewUtil: MediaViewUtil? = null

    /**
     * 是否刷新
     */
    private val mIsRefresh = false

    /**
     * 压缩异步协程
     */
    private var mCompressFileJob: Job? = null

    /**
     * 异步线程的逻辑
     */
    private val mAlbumCompressFileTask by lazy {
        AlbumCompressFileTask(requireActivity(), tag, AlbumFragment::class.java, mGlobalSpec)
    }

    /**
     * 当前点击item的索引
     */
    private var currentPosition: Int = 0

    /**
     * 预览界面滑动后的索引
     */
    var smoothScrollPosition: Int = 0

    /**
     * 判断scroll是否是用户主动拖拽
     */
    private var isRecyclerViewUserDragging = false

    /**
     * 判断scroll是否处于滑动中
     */
    private var isRecyclerViewScrolling = false

    /**
     * 先执行onAttach生命周期再执行onCreateView
     *
     * @param context 上下文
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mApplicationContext = requireActivity().applicationContext
        if (mAlbumSpec.SelectedData.isNotEmpty()) {
            mSelectedModel.getSelectedData().addAll(mAlbumSpec.SelectedData)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAlbumZjhBinding.inflate(inflater, container, false)
        initConfig()
        initView()
        initActivityResult()
        initListener()
        initMediaViewUtil()
        initObserveData()
        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        updateBottomToolbar()
        // 委托给 TvAlbumPermissionManager 处理编辑权限逻辑（移除原 mAlbumModel.isEditPermission 相关代码）
        mTvAlbumPermissionManager.onResume()
    }

    /**
     * 初始化配置
     */
    private fun initConfig() {
        mOriginalManage = OriginalManage(this, mMainModel, mSelectedModel, mAlbumSpec)
    }

    /**
     * 初始化view
     */
    private fun initView() {
        // 兼容沉倾状态栏
        val statusBarHeight = getStatusBarHeight(requireActivity())
        mBinding.root.setPadding(
            mBinding.root.paddingLeft, statusBarHeight,
            mBinding.root.paddingRight, mBinding.root.paddingBottom
        )
        // 修改颜色
        val navigationIcon = mBinding.toolbar.navigationIcon
        val ta = requireActivity().theme.obtainStyledAttributes(intArrayOf(R.attr.album_element_color))
        val color = ta.getColor(0, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ta.close()
        } else {
            ta.recycle()
        }
        navigationIcon?.let {
            setColorFilterSrcIn(navigationIcon, color)
        }

        // 为 tvAlbumPermission 设置富文本(下划线+点击事件)
        initTvAlbumPermission()
        initBottomToolbar()

        updateBottomToolbar()

        initAlbumSpinner()

        // 获取专辑数据
        mMainModel.loadAlbums()

        // 关闭滑动隐藏布局功能
        if (!mAlbumSpec.slidingHiddenEnable) {
            mBinding.recyclerview.isNestedScrollingEnabled = false
            val params = mBinding.toolbar.layoutParams as AppBarLayout.LayoutParams
            params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
            mBinding.emptyView.setPadding(0, 0, 0, dip2px(50f))
            mBinding.recyclerview.setPadding(0, 0, 0, dip2px(50f))
        }
    }

    /**
     * 初始化事件
     */
    private fun initListener() {
        // 滑动回调事件主要是处理共享动画的参数设置
        mBinding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_SETTLING ->
                        // 如果不是人为滑动 并且 不是滑动中状态
                        if (!isRecyclerViewUserDragging && !isRecyclerViewScrolling) {
                            // 设置滑动中状态
                            isRecyclerViewScrolling = true
                        }

                    RecyclerView.SCROLL_STATE_DRAGGING ->
                        // 如果是用户主动滑动recyclerview，则不触发位置计算
                        isRecyclerViewUserDragging = true

                    RecyclerView.SCROLL_STATE_IDLE ->
                        // 如果不是人为滑动 并且 是滑动中状态变成停止状态
                        if (!isRecyclerViewUserDragging && isRecyclerViewScrolling) {
                            isRecyclerViewScrolling = false
                            // 将当前列表的组件宽高数据添加到缓存
                            add(mBinding.recyclerview, 0)
                            mMainModel.onScrollToPositionComplete(smoothScrollPosition)
                        }
                }
            }

        })

        // 关闭事件
        mBinding.imgClose.setOnClickListener { requireActivity().finish() }

        // 下拉框选择的时候
        mAlbumSpinner?.setOnAlbumItemClickListener(object : OnAlbumItemClickListener {
            override fun onItemClick(position: Int, album: Album) {
                // 设置缓存值
                mMainModel.currentSelection = position
                onAlbumSelected(album)
                mAlbumSpinner?.dismiss()
            }
        })

        // 预览事件
        mBinding.buttonPreview.setOnClickListener(object : OnMoreClickListener() {
            override fun onListener(v: View) {
                startPreviewActivityByAlbum(requireActivity(), mGlobalSpec.cutscenesEnabled, mPreviewActivityResult, mSelectedModel.getSelectedData().localMedias)
            }
        })

        // 确认当前选择的图片
        mBinding.buttonApply.setOnClickListener(object : OnMoreClickListener() {
            override fun onListener(v: View) {
                val localMediaArrayList = mSelectedModel.getSelectedData().localMedias
                // 设置是否原图状态
                for (localMedia in localMediaArrayList) {
                    localMedia.isOriginal = mMainModel.getOriginalEnable()
                }
                compressFile(localMediaArrayList)
            }
        })

        // 点击原图
        mBinding.originalLayout.setOnClickListener { mOriginalManage.originalClick() }

        // 点击Loading停止
        mBinding.pbLoading.setOnClickListener {
            // 中断线程
            mCompressFileJob?.cancel()
            // 恢复界面可用
            setControlTouchEnable(true)
        }

        // 触发滑动事件
        mBinding.bottomToolbar.onListener = ConstraintLayoutBehavior.Listener { translationY: Float -> (requireActivity() as MainActivity).onDependentViewChanged(translationY) }
    }

    /**
     * 初始化MediaViewUtil
     */
    private fun initMediaViewUtil() {
        Log.d("onSaveInstanceState", " initMediaViewUtil")
        val ta = requireActivity().theme.obtainStyledAttributes(intArrayOf(R.attr.item_placeholder))
        val placeholder = ta.getDrawable(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ta.close()
        } else {
            ta.recycle()
        }
        mMediaViewUtil = MediaViewUtil(this.mApplicationContext, this, mMainModel, mSelectedModel, mBinding.recyclerview, placeholder, this, this)
    }

    /**
     * 初始化数据的监控
     */
    private fun initObserveData() {
        // 专辑加载完毕
        LifecycleFlowCollector.collect(this, mMainModel.albums) { albums ->
            if (albums.isNotEmpty()) {
                // 更新专辑列表
                mAlbumSpinner?.bindFolder(albums)
                // 默认选中第一个专辑
                mMainModel.currentSelection = 0
                onAlbumSelected(albums[0]) // 加载默认专辑数据
                // 更新专辑标题（保留原动画逻辑）
                updateAlbumTitle(albums)
            }
        }
        // 选择数据改变
        mSelectedModel.selectedDataChange.observe(viewLifecycleOwner) { position:Int -> mMediaViewUtil?.notifyItemByLocalMedia(position) }
        // 原图选项改变
        LifecycleFlowCollector.collect(this, mMainModel.originalEnable) { value: Boolean ->
            mBinding.original.setChecked(value)
        }
        // 预览界面的viewPage滑动时触发
        LifecycleFlowCollector.collect(this, mMainModel.onViewPageSelected) { value: Int ->
            smoothScrollPosition = value
            // 滑动到viewPage的一样position
            isRecyclerViewUserDragging = false
            mBinding.recyclerview.smoothScrollToPosition(smoothScrollPosition)
        }
    }

    /**
     * 初始化Activity的返回
     */
    private fun initActivityResult() {
        // 将PreviewActivity传递的数据继续传给上一个Activity
        mPreviewActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }
            result.data?.let {
                requireActivity().setResult(Activity.RESULT_OK, result.data)
            }
            requireActivity().finish()
        }
    }

    /**
     * 初始化下拉框
     */
    private fun initAlbumSpinner() {
        // 获取上下箭头两个图片
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.album_listPopupWindowStyle, typedValue, true)
        val albumSpinnerStyle = AlbumSpinnerStyle()
        albumSpinnerStyle.drawableUp = AttrsUtils.getTypeValueDrawable(mApplicationContext, typedValue.resourceId, R.attr.album_arrow_up_icon, R.drawable.ic_round_keyboard_arrow_up_24)
        albumSpinnerStyle.drawableDown = AttrsUtils.getTypeValueDrawable(mApplicationContext, typedValue.resourceId, R.attr.album_arrow_down_icon, R.drawable.ic_round_keyboard_arrow_down_24)
        albumSpinnerStyle.maxHeight = (getScreenHeight(requireActivity()) * 0.6).toInt()

        val ta = requireActivity().theme.obtainStyledAttributes(intArrayOf(R.attr.album_thumbnail_placeholder))
        ta.getDrawable(0)?.let {
            albumSpinnerStyle.placeholder = it
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ta.close()
        } else {
            ta.recycle()
        }

        mAlbumSpinner = AlbumSpinner(mApplicationContext, albumSpinnerStyle)
        mAlbumSpinner?.setArrowImageView(mBinding.imgArrow)
        mAlbumSpinner?.setTitleTextView(mBinding.tvAlbumTitle)
    }

    /**
     * 为 tvAlbumPermission 设置富文本(下划线+点击事件)
     */
    private fun initTvAlbumPermission() {
        // 初始化 tvAlbumPermission 逻辑（替换原 initTvAlbumPermission() 调用）
        mTvAlbumPermissionManager = TvAlbumPermissionManager(
            context = mApplicationContext,
            fragmentAlbumZjhBinding = mBinding,
            albumModel = mAlbumModel,
            mainModel = mMainModel
        ).apply {
            // 注册权限设置页启动器并传入管理器
            val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                // 设置界面无回调，无需处理
            }
            // 调用管理器初始化方法，传入启动器
            init(launcher)
        }
    }

    private fun initBottomToolbar() {
        mBottomToolbarManager = BottomToolbarManager(
            binding = mBinding,
            albumSpec = mAlbumSpec,
            originalManage = mOriginalManage
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 1. 取消视图相关协程（如 UI 动画、延迟任务）
        lifecycleScope.cancel()

        // 2. 清理视图监听器（避免匿名类持有 Fragment 引用）
        mAlbumSpinner?.setOnAlbumItemClickListener(null)
        mBinding.recyclerview.clearOnScrollListeners()
        mBinding.recyclerview.adapter = null

        // 4. 释放视图工具类（如 MediaViewUtil 包含视图引用）
        mMediaViewUtil?.onDestroyView()
        mMediaViewUtil = null
    }

    override fun onDestroy() {
        Log.d(tag, "AlbumFragment onDestroy")
        // 1. 释放全局静态资源（如 VideoCompressCoordinator）
        mGlobalSpec.videoCompressCoordinator?.let {
            it.onCompressDestroy(this@AlbumFragment.javaClass)
            mGlobalSpec.videoCompressCoordinator = null
        }
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.itemId == android.R.id.home || super.onOptionsItemSelected(item)
    }

    /**
     * 更新专辑标题
     *
     * @param albums 专辑列表
     */
    private fun updateAlbumTitle(albums: List<Album>) {
        // 可能因为别的原因销毁当前界面，回到当前选择的位置
        val album = albums[mMainModel.currentSelection]
        val albumChecks = ArrayList<Album>()
        albumChecks.add(album)
        mAlbumSpinner?.updateCheckStatus(albumChecks)
        val displayName = album.name
        if (mBinding.tvAlbumTitle.visibility == View.VISIBLE) {
            mBinding.tvAlbumTitle.text = displayName
        } else {
            mBinding.tvAlbumTitle.alpha = 0.0f
            mBinding.tvAlbumTitle.visibility = View.VISIBLE
            mBinding.tvAlbumTitle.text = displayName
            mBinding.tvAlbumTitle.animate().alpha(1.0f).setDuration(
                mApplicationContext.resources.getInteger(
                    android.R.integer.config_longAnimTime
                ).toLong()
            ).start()
        }
    }

    /**
     * 更新底部数据
     */
    private fun updateBottomToolbar() {
        val selectedCount = mSelectedModel.getSelectedData().count()
        mBottomToolbarManager.updateSelectedState(selectedCount)
        mBottomToolbarManager.updateOriginalState(mMainModel.getOriginalEnable())
        showBottomView(selectedCount)
    }

    /**
     * 更新原图控件状态
     */
    private fun updateOriginalState() {
        // 设置选择状态
        mBinding.original.setChecked(mMainModel.getOriginalEnable())
        mOriginalManage.updateOriginalState()
    }

    /**
     * 选择某个专辑的时候
     *
     * @param album 专辑
     */
    private fun onAlbumSelected(album: Album) {
        if (album.isAll && album.isEmpty) {
            // 如果是选择全部并且没有数据的话，显示空的view
            mBinding.recyclerview.visibility = View.GONE
            mBinding.emptyView.visibility = View.VISIBLE
        } else {
            // 清空当前相册所有选择的数据
            mSelectedModel.getSelectedData().localMedias.clear()
            // 如果有数据，显示相应相关照片
            mBinding.recyclerview.visibility = View.VISIBLE
            mBinding.emptyView.visibility = View.GONE
            if (!mIsRefresh) {
                mMediaViewUtil?.load(album)
                mBinding.tvAlbumTitle.text = album.name
            }
        }
    }

    override fun onUpdate() {
        // notify bottom toolbar that check state changed.
        updateBottomToolbar()
        // 触发选择的接口事件
        mAlbumSpec.onSelectedListener?.onSelected(mSelectedModel.getSelectedData().localMedias)
    }

    /**
     * 点击事件
     *
     * @param album           相册集合
     * @param imageView       图片View
     * @param item            选项
     * @param adapterPosition 索引
     */
    override fun onMediaClick(album: Album?, imageView: ImageView?, item: LocalMedia?, adapterPosition: Int) {
        if (isFastDoubleClick()) {
            return
        }
        // 将当前列表的组件宽高数据添加到缓存
        add(mBinding.recyclerview, 0)

        currentPosition = adapterPosition
        // 设置position
        mMainModel.previewPosition = adapterPosition

        startPreviewFragmentByAlbum((requireActivity() as MainActivity))
    }

    /**
     * 显示本身的底部
     * 隐藏母窗体的table
     * 以后如果有配置，就检查配置是否需要隐藏母窗体
     *
     * @param count 当前选择的数量
     */
    private fun showBottomView(count: Int) {
        // 当前选择图片数量 > 0 并且 处于相册界面
        if ((requireActivity() as MainActivity).mActivityMainZjhBinding.tableLayout.currentTab == 0) {
            if (count > 0) {
                // 显示底部
                mBinding.bottomToolbar.visibility = View.VISIBLE
                // 隐藏母窗体的table
                (requireActivity() as MainActivity).showHideTableLayout(false)
            } else {
                // 隐藏底部
                mBinding.bottomToolbar.visibility = View.GONE
                // 显示母窗体的table
                (requireActivity() as MainActivity).showHideTableLayout(true)
            }
        }
    }

    /**
     * 压缩文件开始
     *
     * @param localMediaArrayList 本地数据包含别的参数
     */
    private fun compressFile(localMediaArrayList: ArrayList<LocalMedia>) {
        // 显示loading动画
        setControlTouchEnable(false)

        // 复制相册的文件
        compressFileTask(localMediaArrayList)
    }

    /**
     * 完成压缩-复制的异步线程
     *
     * @param localMediaArrayList 需要压缩的数据源
     */
    private fun compressFileTask(localMediaArrayList: ArrayList<LocalMedia>) {
        mCompressFileJob?.cancel()
        // 启动协程并获取Job对象
        mCompressFileJob = lifecycleScope.request {
            mAlbumCompressFileTask.compressFileTaskDoInBackground(localMediaArrayList, false)
        }.onSuccess { data ->
            setResultOk(data)
        }.onFail { error ->
            // 结束loading
            setControlTouchEnable(true)
            Toast.makeText(mApplicationContext, error.message, Toast.LENGTH_SHORT).show()
            Log.e(tag, error.message, error)
        }.onCancel {
            // 结束loading
            setControlTouchEnable(true)
        }.launch()
    }

    /**
     * 关闭Activity回调相关数值
     *
     * @param localMediaArrayList 本地数据包含别的参数
     */
    private fun setResultOk(localMediaArrayList: ArrayList<LocalMedia>) {
        Log.d(tag, "setResultOk")
        // 获取选择的图片的url集合
        val result = Intent()
        result.putParcelableArrayListExtra(STATE_SELECTION, localMediaArrayList)
        requireActivity().setResult(Activity.RESULT_OK, result)
        requireActivity().finish()
    }

    /**
     * 设置是否启用界面触摸，不可禁止中断、退出
     */
    private fun setControlTouchEnable(enable: Boolean) {
        mBinding.recyclerview.isEnabled = enable
        // 如果不可用就显示 加载中 view,否则隐藏
        if (!enable) {
            mBinding.pbLoading.visibility = View.VISIBLE
            mBinding.buttonApply.visibility = View.GONE
            mBinding.buttonPreview.isEnabled = false
        } else {
            mBinding.pbLoading.visibility = View.GONE
            mBinding.buttonApply.visibility = View.VISIBLE
            mBinding.buttonPreview.isEnabled = true
        }
    }

    companion object {
        private const val ARGUMENTS_MARGIN_BOTTOM: String = "arguments_margin_bottom"

        /**
         * @param marginBottom 底部间距
         */
        fun newInstance(marginBottom: Int): AlbumFragment {
            val albumFragment = AlbumFragment()
            val args = Bundle()
            albumFragment.arguments = args
            args.putInt(ARGUMENTS_MARGIN_BOTTOM, marginBottom)
            return albumFragment
        }
    }
}
