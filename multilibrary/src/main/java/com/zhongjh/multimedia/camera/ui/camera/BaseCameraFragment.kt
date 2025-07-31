package com.zhongjh.multimedia.camera.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityOptionsCompat
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.listener.OnMoreClickListener
import com.zhongjh.common.utils.StatusBarUtils.getStatusBarHeight
import com.zhongjh.common.utils.ThreadUtils
import com.zhongjh.multimedia.BaseFragment
import com.zhongjh.multimedia.MainActivity
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.camera.constants.FlashCacheUtils.getFlashModel
import com.zhongjh.multimedia.camera.constants.FlashCacheUtils.saveFlashModel
import com.zhongjh.multimedia.camera.entity.BitmapData
import com.zhongjh.multimedia.camera.listener.ClickOrLongListener
import com.zhongjh.multimedia.camera.listener.OnCameraManageListener
import com.zhongjh.multimedia.camera.ui.camera.impl.ICameraFragment
import com.zhongjh.multimedia.camera.ui.camera.impl.ICameraView
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraManage
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.camera.ui.camera.state.type.PictureMultiple
import com.zhongjh.multimedia.camera.ui.camera.state.type.PictureSingle
import com.zhongjh.multimedia.camera.ui.camera.state.type.Preview
import com.zhongjh.multimedia.camera.ui.camera.state.type.VideoMultiple
import com.zhongjh.multimedia.camera.ui.camera.state.type.VideoMultipleIn
import com.zhongjh.multimedia.camera.ui.camera.state.type.impl.IState
import com.zhongjh.multimedia.camera.ui.preview.video.PreviewVideoActivity
import com.zhongjh.multimedia.camera.util.LogUtil
import com.zhongjh.multimedia.model.SelectedData
import com.zhongjh.multimedia.settings.CameraSpec
import com.zhongjh.multimedia.settings.GlobalSpec
import com.zhongjh.multimedia.utils.PackageManagerUtils.isSupportCameraLedFlash
import com.zhongjh.multimedia.utils.SelectableUtils.imageMaxCount
import com.zhongjh.multimedia.utils.SelectableUtils.videoMaxCount
import com.zhongjh.multimedia.utils.SelectableUtils.videoValid
import com.zhongjh.multimedia.widget.BaseOperationLayout
import com.zhongjh.multimedia.widget.clickorlongbutton.ClickOrLongButton
import java.io.File
import java.lang.ref.WeakReference
import java.util.Objects

/**
 * 一个父类的拍摄Fragment，用于开放出来给开发自定义，但是同时也需要遵守一些规范
 * 因为该类含有过多方法，所以采用多接口 + Facade 模式
 * Facade模式在presenter包里面体现出来，这个并不是传统意义上的MVP上的P
 * 只是单纯将CameraFragment里面的涉及Picture和Video的两个操作分开出来
 * 这样做的好处是为了减少一个类的代码臃肿、易于扩展维护等等
 *
 *
 * 该类主要根据两个接口实现相关方法
 * [ICameraView]:
 * 主要让开发者提供相关View的实现
 * [ICameraFragment]:
 * 主要实现除了图片、视频的其他相关方法，比如显示LoadingView、闪光灯等操作、底部菜单显示隐藏、图廊预览等等
 *
 * @author zhongjh
 * @date 2022/8/11
 */
abstract class BaseCameraFragment<StateManager : CameraStateManager, PictureManager : CameraPictureManager, VideoManager : CameraVideoManager>
    : BaseFragment(), ICameraView, ICameraFragment {
    /**
     * 使用弱引用持有 Activity
     */
    private var mainActivityRef: WeakReference<MainActivity>? = null

    /**
     * 安全获取 Activity
     */
    val mainActivity: MainActivity?
        get() = mainActivityRef?.get()

    lateinit var myContext: Context
        private set
    /**
     * 在图廊预览界面点击了确定
     */
    private lateinit var albumPreviewActivityResult: ActivityResultLauncher<Intent>

    /**
     * 公共配置
     */
    var globalSpec: GlobalSpec = GlobalSpec
        private set

    /**
     * 拍摄配置
     */
    var cameraSpec: CameraSpec = CameraSpec
        private set
    lateinit var cameraManage: CameraManage
        private set

    /**
     * 闪关灯状态 默认关闭
     */
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF

    /**
     * 请求权限的回调
     */
    private var requestPermissionActivityResult: ActivityResultLauncher<Array<String>>? = null

    /**
     * 声明一个long类型变量：用于存放上一点击“返回键”的时刻
     */
    private var mExitTime: Long = 0

    /**
     * 是否提交,如果不是提交则要删除冗余文件
     */
    private var isCommit = false
    private var isShowContinueTip = false

    /**
     * 设置状态管理,处理不同状态下进行相关逻辑
     * 有以下状态：
     * [Preview]、[PictureSingle]、[PictureMultiple]、[VideoMultiple]、[VideoMultipleIn]
     */
    abstract val cameraStateManager: StateManager

    /**
     * 设置[cameraPictureManager]，专门处理有关图片逻辑
     * 如果没有自定义，则直接返回[cameraPictureManager]
     *
     * @return cameraPictureManager
     */
    abstract val cameraPictureManager: PictureManager

    /**
     * 设置[cameraVideoManager]，专门处理有关视频逻辑
     * 如果没有自定义，则直接返回[cameraVideoManager]
     *
     * @return cameraVideoManager
     */
    abstract val cameraVideoManager: VideoManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActivityResult()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = setContentView(inflater, container)
        view.setOnKeyListener { _: View, keyCode: Int, _: KeyEvent -> keyCode == KeyEvent.KEYCODE_BACK }
        initView(view, savedInstanceState)
        initData()
        setView()
        initListener()
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            this.mainActivityRef = WeakReference(context)
            this.myContext = context.applicationContext
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainActivityRef = null
    }

    override fun onBackPressed(): Boolean {
        val isTrue = cameraStateManager.onBackPressed()
        if (isTrue != null) {
            return isTrue
        } else {
            // 与上次点击返回键时刻作差，第一次不能立即退出
            if ((System.currentTimeMillis() - mExitTime) > MILLISECOND) {
                // 大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(myContext, resources.getString(R.string.z_multi_library_press_confirm_again_to_close), Toast.LENGTH_SHORT).show()
                // 并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis()
                return true
            } else {
                return false
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if ((keyCode and cameraSpec.keyCodeTakePhoto) > 0) {
            cameraPictureManager.takePhoto()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 生命周期onResume
     */
    override fun onResume() {
        super.onResume()
        cameraManage.onResume()
        LogUtil.i("CameraLayout onResume")
        // 清空进度，防止正在进度中突然按home键
        photoVideoLayout.photoVideoLayoutViewHolder.btnClickOrLong.reset()
        // 重置当前按钮的功能
        initPvLayoutButtonFeatures()
    }

    /**
     * 生命周期onPause
     */
    override fun onPause() {
        super.onPause()
        cameraManage.onPause()
        LogUtil.i("CameraLayout onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 清除视图引用
        closeView?.setOnClickListener(null)
        flashView?.setOnClickListener(null)
        switchView?.setOnClickListener(null)
        cameraManage.setOnCameraManageListener(null)
    }

    override fun onDestroy() {
        onDestroy(isCommit)
        photoVideoLayout.onDestroy()
        cameraManage.onDestroy()
        super.onDestroy()
    }

    /**
     * 设置相关view，由子类赋值
     */
    private fun setView() {
        cameraManage.init()

        // 兼容沉倾状态栏
        topView?.let { topView ->
            val statusBarHeight = getStatusBarHeight(requireActivity())
            topView.setPadding(0, statusBarHeight, 0, 0)
            val layoutParams = topView.layoutParams
            layoutParams.height += statusBarHeight
        }

        // 处理图片、视频等需要进度显示
        photoVideoLayout.photoVideoLayoutViewHolder.btnConfirm.setProgressMode(true)

        // 初始化cameraView,判断是否开启录制视频，如果开启就开启录制声音x
        cameraManage.isAudio = videoValid()
        switchView?.setImageResource(cameraSpec.imageSwitch)
        // 设置录制时间
        photoVideoLayout.setDuration(cameraSpec.maxDuration)
    }

    /**
     * 初始化相关数据
     */
    private fun initData() {
        mainActivity?.let { mainActivity ->
            cameraManage = CameraManage(mainActivity, previewView, focusView)
        }
        // 闪光灯修改默认模式
        flashMode = cameraSpec.flashMode
        // 记忆模式
        flashGetCache()

        // 初始化适配器
        cameraPictureManager.initMultiplePhotoAdapter()
    }

    /**
     * 初始化相关事件
     */
    protected open fun initListener() {
        // 关闭事件
        initCameraLayoutCloseListener()
        // 切换闪光灯模式
        initImgFlashListener()
        // 切换摄像头前置/后置
        initImgSwitchListener()
        // 主按钮监听
        initPvLayoutPhotoVideoListener()
        // 左右确认和取消
        initPvLayoutOperateListener()
        // 拍照监听
        initCameraViewListener()
        // 编辑图片事件
        cameraPictureManager.initPhotoEditListener()
    }

    /**
     * 关闭View初始化事件
     */
    private fun initCameraLayoutCloseListener() {
        closeView?.setOnClickListener(object : OnMoreClickListener() {
                /** @noinspection unused
                 */
                override fun onListener(v: View) {
                    mainActivity?.finish()
                }
            })
    }

    /**
     * 切换闪光灯模式
     */
    private fun initImgFlashListener() {
        flashView?.setOnClickListener {
                flashMode++
                if (flashMode > ImageCapture.FLASH_MODE_OFF) {
                    flashMode = ImageCapture.FLASH_MODE_AUTO
                }
                // 重新设置当前闪光灯模式
                setFlashLamp()
            }
    }

    /**
     * 切换摄像头前置/后置
     */
    private fun initImgSwitchListener() {
        switchView?.setOnClickListener { cameraManage.toggleFacing() }
    }

    /**
     * 主按钮监听,拍摄和录制的触发源头事件
     * onClick() 即代表触发拍照
     * onLongClick() 即代表触发录制
     */
    private fun initPvLayoutPhotoVideoListener() {
        photoVideoLayout.setPhotoVideoListener(object : ClickOrLongListener {
            override fun actionDown() {
                Log.d(TAG, "pvLayout actionDown")
                // 母窗体隐藏底部滑动
                mainActivity?.showHideTableLayout(false)
            }

            override fun onClick() {
                Log.d(TAG, "pvLayout onClick")
                this@BaseCameraFragment.cameraPictureManager.takePhoto()
            }

            override fun onLongClick() {
                Log.d(TAG, "pvLayout onLongClick ")
                this@BaseCameraFragment.cameraVideoManager.recordVideo()
                // 设置录制状态
                this@BaseCameraFragment.cameraStateManager.state = this@BaseCameraFragment.cameraStateManager.videoMultipleIn
                // 开始录像
                setMenuVisibility(View.INVISIBLE)
            }

            override fun onLongClickEnd(time: Long) {
                Log.d(TAG, "pvLayout onLongClickEnd ")
                // 录像暂停
                pauseRecord()
            }

            override fun onLongClickFinish() {
                Log.d(TAG, "pvLayout onLongClickFinish ")
                this@BaseCameraFragment.state.onLongClickFinish()
            }

            override fun onLongClickError() {
                Log.d(TAG, "pvLayout onLongClickError ")
            }

            override fun onBanClickTips() {
                // 判断如果是分段录制模式就提示
                photoVideoLayout.setTipAlphaAnimation(resources.getString(R.string.z_multi_library_working_video_click_later))
            }

            override fun onClickStopTips() {
                photoVideoLayout.setTipAlphaAnimation(resources.getString(R.string.z_multi_library_touch_your_suspension))
            }
        })
    }

    /**
     * 左右两个按钮：确认和取消
     */
    private fun initPvLayoutOperateListener() {
        // noinspection unused
        photoVideoLayout.setOperateListener(object : BaseOperationLayout.OperateListener {
            /** @noinspection unused
             */
            override fun beforeConfirm(): Boolean {
                this@BaseCameraFragment.cameraStateManager.pvLayoutCommit()
                return true
            }

            /** @noinspection unused
             */
            override fun cancel() {
                Log.d(TAG, "cancel " + this@BaseCameraFragment.state.toString())
                this@BaseCameraFragment.cameraStateManager.pvLayoutCancel()
            }

            override fun startProgress() {
                Log.d(TAG, "startProgress " + this@BaseCameraFragment.state.toString())
                // 没有所需要请求的权限，就进行后面的逻辑
                this@BaseCameraFragment.cameraStateManager.pvLayoutCommit()
            }

            override fun stopProgress() {
                Log.d(TAG, "stopProgress " + this@BaseCameraFragment.state.toString())
                this@BaseCameraFragment.cameraStateManager.stopProgress()
                // 重置按钮
                photoVideoLayout.resetConfirm()
            }

            override fun doneProgress() {
                Log.d(TAG, "doneProgress " + this@BaseCameraFragment.state.toString())
                photoVideoLayout.resetConfirm()
            }
        })
    }

    /**
     * 拍照、录制监听
     */
    private fun initCameraViewListener() {
        // noinspection unused
        cameraManage.setOnCameraManageListener(object : OnCameraManageListener {
            override fun onRecordStart() {
                this@BaseCameraFragment.cameraVideoManager.onRecordStart()
            }

            override fun onActivityPause() {
                // 重置View
                this@BaseCameraFragment.cameraStateManager.onActivityPause()
            }

            override fun onPictureSuccess(path: String) {
                Log.d(TAG, "onPictureSuccess")
                cameraSpec.onInitCameraManager?.initWatermarkedImage(path)
                // 显示图片
                this@BaseCameraFragment.cameraPictureManager.addCaptureData(path)
                // 恢复点击
                childClickableLayout.setChildClickable(true)
            }

            override fun bindSucceed() {
                // 设置闪光灯模式
                setFlashLamp()
            }

            override fun onRecordSuccess(path: String) {
                Log.d(TAG, "onRecordSuccess")
                // 处理视频文件,最后会解除《禁止点击》
                this@BaseCameraFragment.cameraVideoManager.onRecordSuccess(path)
            }

            override fun onRecordPause(recordedDurationNanos: Long) {
                // 处理暂停,最后会解除《禁止点击》
                this@BaseCameraFragment.cameraVideoManager.onRecordPause(recordedDurationNanos)
            }

            /** @noinspection unused
             */
            override fun onError(errorCode: Int, message: String?, cause: Throwable?) {
                Toast.makeText(this@BaseCameraFragment.myContext, message, Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * 初始化Activity回调
     */
    private fun initActivityResult() {
        // 在图廊预览界面点击了确定
        albumPreviewActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data == null) {
                    return@registerForActivityResult
                }
                // 获取选择的数据
                val selected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableArrayListExtra(SelectedData.STATE_SELECTION, LocalMedia::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableArrayListExtra(SelectedData.STATE_SELECTION)
                }

                selected?.let {
                    // 重新赋值
                    val bitmapDataArrayList = ArrayList<BitmapData>()
                    for (item in selected) {
                        // 如果有编辑图片,则将该图片覆盖最新的拍照图片
                        val path = if (null == item.editorPath) item.absolutePath else item.editorPath
                        path?.let {
                            val uri = Uri.fromFile(File(path)).toString()
                            val bitmapData = BitmapData(item.fileId, uri, path)
                            bitmapDataArrayList.add(bitmapData)
                        }
                    }
                    // 全部刷新
                    cameraPictureManager.refreshMultiPhoto(bitmapDataArrayList)
                }
            }
        }
        // 创建权限申请回调
        requestPermissionActivityResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            if (result[Manifest.permission.WRITE_EXTERNAL_STORAGE] != null
                && result[Manifest.permission.READ_EXTERNAL_STORAGE] != null
            ) {
                if (Objects.requireNonNull(result[Manifest.permission.WRITE_EXTERNAL_STORAGE]) == true && Objects.requireNonNull(result[Manifest.permission.READ_EXTERNAL_STORAGE]) == true) {
                    //权限全部获取到之后的动作
                    cameraStateManager.pvLayoutCommit()
                }
            }
        }
        cameraVideoManager.initActivityResult()
        cameraPictureManager.initActivityResult()
    }

    /**
     * 生命周期onDestroy
     *
     * @param isCommit 是否提交了数据,如果不是提交则要删除冗余文件
     */
    protected fun onDestroy(isCommit: Boolean) {
        try {
            LogUtil.i("CameraLayout destroy")
            cameraPictureManager.onDestroy(isCommit)
            photoVideoLayout.photoVideoLayoutViewHolder.btnConfirm.reset()
            cameraVideoManager.onDestroy()
            cameraManage.onDestroy()
            // 记忆模式
            flashSaveCache()
            cameraSpec.onCaptureListener = null
            cameraSpec.onInitCameraManager = null
        } catch (ignored: NullPointerException) {
        }
    }

    /**
     * 提交图片成功后，返回数据给上一个页面
     *
     * @param newFiles 新的文件
     */
    override fun commitPictureSuccess(newFiles: ArrayList<LocalMedia>) {
        Log.d(TAG, "mMovePictureFileTask onSuccess")
        isCommit = true
        val result = Intent()
        result.putParcelableArrayListExtra(SelectedData.STATE_SELECTION, newFiles)
        mainActivity?.setResult(Activity.RESULT_OK, result)
        mainActivity?.finish()
    }

    /**
     * 提交图片失败后
     *
     * @param throwable 异常
     */
    override fun commitFail(throwable: Throwable) {
        photoVideoLayout.setTipAlphaAnimation(throwable.message)
        setUiEnableTrue()
    }

    override fun cancel() {
        setUiEnableTrue()
    }

    /**
     * 提交视频成功后，返回数据给上一个页面
     *
     * @param intentPreviewVideo 从预览视频界面返回来的数据intent
     */
    override fun commitVideoSuccess(intentPreviewVideo: Intent) {
        val localMedias = ArrayList<LocalMedia?>()
        val localMedia = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intentPreviewVideo.getParcelableExtra(PreviewVideoActivity.LOCAL_FILE, LocalMedia::class.java)
        } else {
            @Suppress("DEPRECATION")
            intentPreviewVideo.getParcelableExtra(PreviewVideoActivity.LOCAL_FILE)
        }
        localMedias.add(localMedia)
        isCommit = true
        // 获取视频路径
        val intent = Intent()
        intent.putParcelableArrayListExtra(SelectedData.STATE_SELECTION, localMedias)
        mainActivity?.setResult(Activity.RESULT_OK, intent)
        mainActivity?.finish()
    }

    /**
     * 打开预览图片
     *
     * @param intent 包含数据源
     */
    fun openAlbumPreviewActivity(intent: Intent) {
        if (globalSpec.cutscenesEnabled) {
            val options = ActivityOptionsCompat.makeCustomAnimation(
                requireContext(),
                R.anim.activity_open_zjh,  // 进入动画
                0  // 退出动画（0 表示无动画）
            )
            albumPreviewActivityResult.launch(intent, options)
        } else {
            albumPreviewActivityResult.launch(intent)
        }
    }

    /**
     * 当多个图片删除到没有图片时候，隐藏相关View
     */
    override fun hideViewByMultipleZero() {
        // 隐藏横版列表
        recyclerViewPhoto?.visibility = View.GONE

        // 隐藏修饰多图控件的View
        multiplePhotoView?.let { multiplePhotoView ->
            for (view in multiplePhotoView) {
                view.visibility = View.GONE
            }
        }

        // 隐藏左右侧按钮
        photoVideoLayout.photoVideoLayoutViewHolder.btnCancel.visibility = View.GONE
        photoVideoLayout.photoVideoLayoutViewHolder.btnConfirm.visibility = View.GONE

        // 如果是单图编辑情况下,隐藏编辑按钮
        photoVideoLayout.photoVideoLayoutViewHolder.rlEdit.visibility = View.GONE

        // 恢复长按事件，即重新启用录制
        photoVideoLayout.photoVideoLayoutViewHolder.btnClickOrLong.visibility = View.VISIBLE
        initPvLayoutButtonFeatures()

        // 设置空闲状态
        cameraStateManager.state = cameraStateManager.preview

        showBottomMenu()
    }

    /**
     * 长按继续录制
     */
    fun setShortTipLongRecording() {
        if (!isShowContinueTip) {
            // 长按继续录制
            photoVideoLayout.setTipAlphaAnimation(resources.getString(R.string.z_long_press_to_continue_recording))
            isShowContinueTip = true
        }
    }

    /**
     * 初始化中心按钮状态
     */
    private fun initPvLayoutButtonFeatures() {
        // 判断点击和长按的权限
        if (cameraSpec.isClickRecord) {
            // 禁用长按功能
            photoVideoLayout.setButtonFeatures(ClickOrLongButton.BUTTON_STATE_CLICK_AND_HOLD)
            photoVideoLayout.setTip(resources.getString(R.string.z_multi_library_light_touch_camera))
        } else {
            if (cameraSpec.onlySupportImages()) {
                // 禁用长按功能
                photoVideoLayout.setButtonFeatures(ClickOrLongButton.BUTTON_STATE_ONLY_CLICK)
                photoVideoLayout.setTip(resources.getString(R.string.z_multi_library_light_touch_take))
            } else if (cameraSpec.onlySupportVideos()) {
                // 禁用点击功能
                photoVideoLayout.setButtonFeatures(ClickOrLongButton.BUTTON_STATE_ONLY_LONG_CLICK)
                photoVideoLayout.setTip(resources.getString(R.string.z_multi_library_long_press_camera))
            } else {
                // 支持所有，不过要判断数量
                if (imageMaxCount == 0) {
                    // 禁用点击功能
                    photoVideoLayout.setButtonFeatures(ClickOrLongButton.BUTTON_STATE_ONLY_LONG_CLICK)
                    photoVideoLayout.setTip(resources.getString(R.string.z_multi_library_long_press_camera))
                } else if (videoMaxCount == 0) {
                    // 禁用长按功能
                    photoVideoLayout.setButtonFeatures(ClickOrLongButton.BUTTON_STATE_ONLY_CLICK)
                    photoVideoLayout.setTip(resources.getString(R.string.z_multi_library_light_touch_take))
                } else {
                    photoVideoLayout.setButtonFeatures(ClickOrLongButton.BUTTON_STATE_BOTH)
                    photoVideoLayout.setTip(resources.getString(R.string.z_multi_library_light_touch_take_long_press_camera))
                }
            }
        }
    }

    override fun showProgress() {
        // 执行等待动画
        photoVideoLayout.photoVideoLayoutViewHolder.btnConfirm.setProgress(1)
    }

    override fun setProgress(progress: Int) {
        photoVideoLayout.photoVideoLayoutViewHolder.btnConfirm.addProgress(progress)
    }

    /**
     * 迁移图片文件，缓存文件迁移到配置目录
     * 在 doInBackground 线程里面也执行了 runOnUiThread 跳转UI的最终事件
     */
    fun movePictureFile() {
        showProgress()
        // 开始迁移文件
        ThreadUtils.executeByIo(cameraPictureManager.newMovePictureFileTask())
    }

    /**
     * 针对单图进行相关UI变化
     *
     * @param bitmapData 显示单图数据源
     * @param file       显示单图的文件
     * @param path       显示单图的path
     */
    override fun showSinglePicture(bitmapData: BitmapData, file: File, path: String) {
        // 拍照  隐藏 闪光灯、右上角的切换摄像头
        setMenuVisibility(View.INVISIBLE)
        // 这样可以重置
        singlePhotoView.isZoomable = true
        singlePhotoView.visibility = View.VISIBLE
        globalSpec.imageEngine.loadUriImage(myContext, singlePhotoView, bitmapData.absolutePath)
        photoVideoLayout.startTipAlphaAnimation()
        photoVideoLayout.startShowLeftRightButtonsAnimator(true)

        // 判断是否要编辑
        if (globalSpec.imageEditEnabled) {
            photoVideoLayout.photoVideoLayoutViewHolder.rlEdit.visibility = View.VISIBLE
            photoVideoLayout.photoVideoLayoutViewHolder.rlEdit.tag = path
        } else {
            photoVideoLayout.photoVideoLayoutViewHolder.rlEdit.visibility = View.INVISIBLE
        }

        // 隐藏拍照按钮
        photoVideoLayout.photoVideoLayoutViewHolder.btnClickOrLong.visibility = View.INVISIBLE

        // 设置当前模式是图片模式
        cameraStateManager.state = cameraStateManager.pictureComplete
    }

    /**
     * 针对多图进行相关UI变化
     */
    override fun showMultiplePicture() {
        // 显示横版列表
        recyclerViewPhoto?.visibility = View.VISIBLE

        // 显示横版列表的线条空间
        multiplePhotoView?.let { multiplePhotoView ->
            for (view in multiplePhotoView) {
                view.visibility = View.VISIBLE
                view.visibility = View.VISIBLE
            }
        }

        photoVideoLayout.startTipAlphaAnimation()
        photoVideoLayout.startOperationBtnAnimatorMulti()

        // 重置按钮，因为每次点击，都会自动关闭
        photoVideoLayout.photoVideoLayoutViewHolder.btnClickOrLong.resetState()
        // 显示右上角
        setMenuVisibility(View.VISIBLE)

        // 禁用长按事件，即禁止录像
        photoVideoLayout.setButtonFeatures(ClickOrLongButton.BUTTON_STATE_ONLY_CLICK)

        // 设置当前模式是图片休闲并存模式
        cameraStateManager.state = cameraStateManager.pictureMultiple
    }

    val state: IState
        /**
         * 获取当前view的状态
         *
         * @return 状态
         */
        get() = cameraStateManager.state

    /**
     * 取消单图后的重置
     */
    fun cancelOnResetBySinglePicture() {
        cameraPictureManager.clearBitmapDataList()

        // 根据不同状态处理相应的事件
        resetStateAll()
    }

    /**
     * 结束所有当前活动，重置状态
     * 一般指完成了当前活动，或者清除所有活动的时候调用
     */
    fun resetStateAll() {
        // 重置右上角菜单
        setMenuVisibility(View.VISIBLE)

        // 恢复底部
        showBottomMenu()

        // 隐藏大图
        singlePhotoView.visibility = View.GONE

        // 隐藏编辑按钮
        photoVideoLayout.photoVideoLayoutViewHolder.rlEdit.visibility = View.GONE

        // 恢复底部按钮
        photoVideoLayout.reset()
        // 恢复底部按钮操作模式
        initPvLayoutButtonFeatures()
    }

    /**
     * 恢复底部菜单,母窗体启动滑动
     */
    override fun showBottomMenu() {
        mainActivity?.showHideTableLayout(true)
    }

    /**
     * 设置界面的功能按钮可以使用
     * 场景：如果压缩或者移动文件时异常，则恢复
     */
    override fun setUiEnableTrue() {
        flashView?.isEnabled = true
        switchView?.isEnabled = true
        photoVideoLayout.setClickOrLongEnable(true)
        // 重置按钮进度
        photoVideoLayout.photoVideoLayoutViewHolder.btnConfirm.reset()
    }

    /**
     * 设置界面的功能按钮禁止使用
     * 场景：确认图片时，压缩中途禁止某些功能使用
     */
    override fun setUiEnableFalse() {
        flashView?.isEnabled = false
        switchView?.isEnabled = false
        photoVideoLayout.setClickOrLongEnable(false)
    }

    /**
     * 设置右上角菜单是否显示
     */
    fun setMenuVisibility(viewVisibility: Int) {
        setSwitchVisibility(viewVisibility)
        flashView?.visibility = viewVisibility
    }

    /**
     * 设置闪光灯是否显示，如果不支持，是一直不会显示
     */
    private fun setSwitchVisibility(viewVisibility: Int) {
        switchView?.let { switchView ->
            if (!isSupportCameraLedFlash(myContext.packageManager)) {
                switchView.visibility = View.GONE
            } else {
                switchView.visibility = viewVisibility
            }
        }
    }

    /**
     * 设置闪关灯
     */
    private fun setFlashLamp() {
        flashView?.let { flashView ->
            when (flashMode) {
                ImageCapture.FLASH_MODE_AUTO -> flashView.setImageResource(cameraSpec.imageFlashAuto)
                ImageCapture.FLASH_MODE_ON -> flashView.setImageResource(cameraSpec.imageFlashOn)
                ImageCapture.FLASH_MODE_OFF -> flashView.setImageResource(cameraSpec.imageFlashOff)
                else -> {}
            }
            cameraManage.setFlashMode(flashMode)
        }
    }

    /**
     * 记忆模式下获取闪光灯缓存的模式
     */
    private fun flashGetCache() {
        // 判断闪光灯是否记忆模式，如果是记忆模式则使用上个闪光灯模式
        context?.let { context ->
            if (cameraSpec.enableFlashMemoryModel) {
                flashMode = getFlashModel(context)
            }
        }
    }

    /**
     * 记忆模式下缓存闪光灯模式
     */
    private fun flashSaveCache() {
        // 判断闪光灯是否记忆模式，如果是记忆模式则存储当前闪光灯模式
        context?.let { context ->
            if (cameraSpec.enableFlashMemoryModel) {
                saveFlashModel(context, flashMode)
            }
        }
    }

    /**
     * 暂停录制
     */
    protected fun pauseRecord() {
        cameraStateManager.pauseRecord()
    }

    companion object {
        private val TAG: String = BaseCameraFragment::class.java.simpleName

        private const val MILLISECOND = 2000
    }
}
