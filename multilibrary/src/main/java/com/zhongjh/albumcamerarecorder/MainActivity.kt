package com.zhongjh.albumcamerarecorder

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.zhongjh.albumcamerarecorder.album.ui.AlbumFragment
import com.zhongjh.albumcamerarecorder.camera.entity.TabEntity
import com.zhongjh.albumcamerarecorder.camera.ui.camera.CameraFragment
import com.zhongjh.albumcamerarecorder.recorder.SoundRecordingFragment
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec.getMimeTypeSet
import com.zhongjh.albumcamerarecorder.utils.AttrsUtils
import com.zhongjh.albumcamerarecorder.utils.HandleBackUtil.handleBackPress
import com.zhongjh.albumcamerarecorder.utils.HandleOnKeyUtil.handleOnKey
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils.albumValid
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils.cameraValid
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils.recorderValid
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils.videoValid
import com.zhongjh.common.enums.MimeType.Companion.ofImage
import com.zhongjh.common.enums.MimeType.Companion.ofVideo
import com.zhongjh.common.utils.AppUtils.getAppName
import com.zhongjh.common.utils.StatusBarUtils.initStatusBar
import kotlin.math.abs


import com.zhongjh.albumcamerarecorder.databinding.ActivityMainZjhBinding


/**
 * 包含三大fragment
 *
 * @author zhongjh
 * @date 2018/8/22
 */
open class MainActivity : AppCompatActivity() {

    private val mActivityMainZjhBinding by lazy {
        ActivityMainZjhBinding.inflate(layoutInflater)
    }

    private var adapterViewPager: MyPagerAdapter? = null

    /**
     * 显示隐藏TabLayout的动画
     */
    var mAnimationTabLayout: ObjectAnimator? = null

    /**
     * 默认索引
     */
    private var mDefaultPosition = 0

    /**
     * 底部控件高度
     */
    private var mTabLayoutHeight = 0f
    var mSpec = GlobalSpec

    /**
     * 是否初始化完毕
     */
    var mIsInit = false

    /**
     * 是否弹出提示多次拒绝权限的dialog
     */
    private var mIsShowDialog = false
    override fun onCreate(savedInstanceState: Bundle?) {
        if (mSpec.needOrientationRestriction()) {
            requestedOrientation = mSpec.orientation
        }
        setTheme(mSpec.themeId)
        initStatusBar(this@MainActivity)
        super.onCreate(savedInstanceState)
        // 确认是否进行了配置
        if (!mSpec.hasInited) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        setContentView(R.layout.activity_main_zjh)
        requestPermissions(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_SAVE_INSTANCE_STATE, true)
    }

    override fun onBackPressed() {
        if (!handleBackPress(this)) {
            super.onBackPressed()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (handleOnKey(this, keyCode, event)) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun finish() {
        super.finish()
        if (mSpec.cutscenesEnabled) {
            //关闭窗体动画显示
            this.overridePendingTransition(0, R.anim.activity_close_zjh)
        }
    }

    override fun onDestroy() {
        if (mSpec.cameraSetting != null) {
            mSpec.cameraSetting!!.clearCameraFragment()
        }
        if (mAnimationTabLayout != null) {
            mAnimationTabLayout!!.end()
        }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SETTING) {
            // 因为权限一直拒绝后，只能跑到系统设置界面调整，这个是系统设置界面返回后的回调，重新验证权限
            requestPermissions(null)
        }
    }

    @TargetApi(23)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!mIsShowDialog) {
            // 全部拒绝后就提示去到应用设置里面修改配置
            var permissionsLength = 0
            for (i in grantResults.indices) {
                // 只有当用户同时点选了拒绝开启权限和不再提醒后才会true
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    if (grantResults[i] == PermissionChecker.PERMISSION_DENIED) {
                        permissionsLength++
                    }
                }
            }
            // 至少一个不再提醒
            if (permissionsLength > 0) {
                val builder = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
                builder.setPositiveButton(getString(R.string.z_multi_library_setting)) { dialog: DialogInterface?, which: Int ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.fromParts("package", packageName, null)
                    this@MainActivity.startActivityForResult(intent, REQUEST_CODE_SETTING)
                    mIsShowDialog = false
                }
                builder.setNegativeButton(getString(R.string.z_multi_library_cancel)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    finish()
                }

                // 获取app名称
                val appName = getAppName(applicationContext)
                if (TextUtils.isEmpty(appName)) {
                    builder.setMessage(getString(R.string.permission_has_been_set_and_will_no_longer_be_asked))
                } else {
                    val toSettingTipStr =
                        getString(R.string.z_multi_library_in_settings_apply) + appName + getString(
                            R.string.z_multi_library_enable_storage_and_camera_permissions_for_normal_use_of_related_functions
                        )
                    builder.setMessage(toSettingTipStr)
                }
                builder.setTitle(getString(R.string.z_multi_library_hint))
                builder.setOnDismissListener { mIsShowDialog = false }
                val dialog: Dialog = builder.create()
                dialog.setCanceledOnTouchOutside(false)
                dialog.setOnKeyListener { _: DialogInterface?, keyCode: Int, event: KeyEvent ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
                        finish()
                    }
                    false
                }
                dialog.show()
                mIsShowDialog = true
            }
        }
        if (!mIsShowDialog) {
            if (requestCode == GET_PERMISSION_REQUEST) {
                var permissionsLength = 0
                for (grantResult in grantResults) {
                    if (grantResult == PermissionChecker.PERMISSION_DENIED) {
                        // 如果拒绝后
                        permissionsLength++
                    }
                }
                if (permissionsLength > 0) {
                    requestPermissionsDialog()
                } else {
                    requestPermissions(null)
                }
            }
        }
    }

    /**
     * 初始化，在权限全部通过后才进行该初始化
     *
     * @param savedInstanceState 恢复的数值
     */
    private fun init(savedInstanceState: Bundle?) {
        if (!mIsInit) {
            initTabLayoutStyle()
            mActivityMainZjhBinding.tableLayout.setTag(R.id.z_tab_layout_translation_y, 0)
            initListener()
            adapterViewPager = MyPagerAdapter(this, mSpec)
            mActivityMainZjhBinding.viewPager.adapter = adapterViewPager
            mActivityMainZjhBinding.viewPager.offscreenPageLimit = 3
            if (savedInstanceState == null || !savedInstanceState.getBoolean(IS_SAVE_INSTANCE_STATE)) {
                // 根据配置默认选第几个，如果是恢复界面的话，就不赋配置值
                mActivityMainZjhBinding.viewPager.setCurrentItem(mDefaultPosition, false)
            }
            // 判断只有一个的时候
            if (adapterViewPager!!.itemCount <= 1) {
                // 则隐藏底部
                mActivityMainZjhBinding.tableLayout.visibility = View.GONE
            } else {
                mActivityMainZjhBinding.tableLayout.visibility = View.VISIBLE
                // 禁滑viewPager
                mActivityMainZjhBinding.viewPager.isUserInputEnabled = false
            }
            mIsInit = true
        }
    }

    /**
     * 初始化事件
     */
    private fun initListener() {
        // 获取高度，用于tabLayout的一些显示隐藏动画参数
        mActivityMainZjhBinding.tableLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mActivityMainZjhBinding.tableLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                mTabLayoutHeight = mActivityMainZjhBinding.tableLayout.measuredHeight.toFloat()
            }
        })
        mActivityMainZjhBinding.tableLayout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                mActivityMainZjhBinding.viewPager.currentItem = position
            }

            override fun onTabReselect(position: Int) {}
        })
        mActivityMainZjhBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                mActivityMainZjhBinding.tableLayout.currentTab = position
                super.onPageSelected(position)
            }
        })
    }

    /**
     * 设置TabLayout样式
     */
    private fun initTabLayoutStyle() {
        val typedValue = TypedValue()
        this.theme.resolveAttribute(R.attr.main_tabLayout, typedValue, true)
        val tabLayoutBg = AttrsUtils.getTypeValueColor(
            this, typedValue.resourceId, R.attr.tabLayout_bg_zjh
        )
        val tabLayoutUnselectedTextColor = AttrsUtils.getTypeValueColor(
            this, typedValue.resourceId, R.attr.tabLayout_unselected_textColor
        )
        val tabLayoutSelectedTextColor = AttrsUtils.getTypeValueColor(
            this, typedValue.resourceId, R.attr.tabLayout_selected_textColor
        )
        if (tabLayoutBg != 0) {
            mActivityMainZjhBinding.tableLayout.setBackgroundColor(tabLayoutBg)
        }
        if (tabLayoutSelectedTextColor != 0) {
            mActivityMainZjhBinding.tableLayout.textSelectColor = tabLayoutSelectedTextColor
        }
        if (tabLayoutUnselectedTextColor != 0) {
            mActivityMainZjhBinding.tableLayout.textUnselectColor = tabLayoutUnselectedTextColor
        }
    }

    /**
     * 请求权限
     *
     * @param savedInstanceState 恢复的数值
     */
    private fun requestPermissions(savedInstanceState: Bundle?) {
        // 判断权限，权限通过才可以初始化相关
        val needPermissions = needPermissions
        if (needPermissions.size > 0) {
            // 请求权限
            requestPermissions2(needPermissions)
        } else {
            // 没有所需要请求的权限，就进行初始化
            init(savedInstanceState)
        }
    }

    /**
     * 请求权限 - 如果曾经拒绝过，则弹出dialog
     */
    private fun requestPermissionsDialog() {
        // 判断权限，权限通过才可以初始化相关
        val needPermissions = needPermissions
        if (needPermissions.size > 0) {
            // 动态消息
            val message = StringBuilder()
            message.append(getString(R.string.z_multi_library_to_use_this_feature))
            for (item in needPermissions) {
                when (item) {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> message.append(getString(R.string.z_multi_library_file_read_and_write_permission_to_read_and_store_related_files))
                    Manifest.permission.RECORD_AUDIO ->                         // 弹窗提示为什么要请求这个权限
                        message.append(getString(R.string.z_multi_library_record_permission_to_record_sound))
                    Manifest.permission.CAMERA ->                         // 弹窗提示为什么要请求这个权限
                        message.append(getString(R.string.z_multi_library_record_permission_to_shoot))
                    else -> {}
                }
            }
            val builder = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
            // 弹窗提示为什么要请求这个权限
            builder.setTitle(getString(R.string.z_multi_library_hint))
            message.append(getString(R.string.z_multi_library_Otherwise_it_cannot_run_normally_and_will_apply_for_relevant_permissions_from_you))
            builder.setMessage(message.toString())
            builder.setPositiveButton(getString(R.string.z_multi_library_ok)) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                // 请求权限
                requestPermissions2(needPermissions)
            }
            builder.setNegativeButton(getString(R.string.z_multi_library_cancel)) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                finish()
            }
            val dialog: Dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.setOnKeyListener { _: DialogInterface?, keyCode: Int, event: KeyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
                    finish()
                }
                false
            }
            dialog.show()
        } else {
            // 没有所需要请求的权限，就进行初始化
            init(null)
        }
    }

    /**
     * 获取目前需要请求的权限
     */
    private val needPermissions: ArrayList<String>
        get() {
            // 需要请求的权限列表
            val permissions = ArrayList<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 存储功能必须验证,兼容Android SDK 33
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (getMimeTypeSet().containsAll(ofImage())) {
                        // 如果所有功能只支持图片，就只请求图片权限
                        if (ContextCompat.checkSelfPermission(
                                this, Manifest.permission.READ_MEDIA_IMAGES
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    } else if (getMimeTypeSet().containsAll(ofVideo())) {
                        // 如果所有功能只支持视频，就只请求视频权限
                        if (ContextCompat.checkSelfPermission(
                                this, Manifest.permission.READ_MEDIA_VIDEO
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                        }
                    } else {
                        // 如果所有功能都支持视频图片，就请求视频图片权限
                        if (ContextCompat.checkSelfPermission(
                                this, Manifest.permission.READ_MEDIA_IMAGES
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                        if (ContextCompat.checkSelfPermission(
                                this, Manifest.permission.READ_MEDIA_VIDEO
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                        }
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }

                // 判断如果有录音功能则验证录音
                if (recorderValid() || videoValid()) {
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (!permissions.contains(Manifest.permission.RECORD_AUDIO)) {
                            permissions.add(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
                // 判断如果有录制功能则验证录音、录制
                if (cameraValid()) {
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (!permissions.contains(Manifest.permission.CAMERA)) {
                            permissions.add(Manifest.permission.CAMERA)
                        }
                    }
                }
            }
            return permissions
        }

    /**
     * 请求权限
     *
     * @param permissions 权限
     */
    private fun requestPermissions2(permissions: ArrayList<String>) {
        ActivityCompat.requestPermissions(
            this@MainActivity, permissions.toTypedArray(), GET_PERMISSION_REQUEST
        )
    }

    /**
     * 显示或者隐藏底部
     * 显示或者隐藏后，都用tag标记，为了[showHideTableLayoutAnimator]显示时恢复数值
     *
     * @param isShow 是否显示
     */
    fun showHideTableLayout(isShow: Boolean) {
        // 判断只有一个的时候
        if (adapterViewPager!!.itemCount <= 1) {
            // 则隐藏底部
            mActivityMainZjhBinding.tableLayout.visibility = View.GONE
        } else {
            if (isShow) {
                mActivityMainZjhBinding.tableLayout.visibility = View.VISIBLE
            } else {
                mActivityMainZjhBinding.tableLayout.visibility = View.GONE
            }
        }
    }

    /**
     * 根据滑动隐藏显示底部控件
     *
     * @param translationY 顶部控件坐标，底部跟顶部这样可以联动
     */
    fun onDependentViewChanged(translationY: Float) {
        mActivityMainZjhBinding.tableLayout.translationY = abs(translationY)
        mActivityMainZjhBinding.tableLayout.setTag(R.id.z_tab_layout_translation_y, abs(translationY))
        Log.d("MainActivity", abs(translationY).toString() + "")
    }

    /**
     * 动画形式的显示隐藏TableLayout
     *
     * @param isShow 是否显示
     */
    fun showHideTableLayoutAnimator(isShow: Boolean) {
        if (isShow) {
            // 获取动画隐藏之前的坐标，恢复回该坐标
            val translationY = mActivityMainZjhBinding.tableLayout.getTag(R.id.z_tab_layout_translation_y) as Float
            mAnimationTabLayout = ObjectAnimator.ofFloat(mActivityMainZjhBinding.tableLayout, "translationY", translationY)
            Log.d("MainActivity", translationY.toString() + "")
        } else {
            mAnimationTabLayout =
                ObjectAnimator.ofFloat(mActivityMainZjhBinding.tableLayout, "translationY", mTabLayoutHeight)
        }
        mAnimationTabLayout?.let {
            it.interpolator = AnimationUtils.loadInterpolator(
                applicationContext, android.R.interpolator.fast_out_linear_in
            )
            it.duration = 300
            it.start()
        }
    }

    inner class MyPagerAdapter(fa: FragmentActivity?, mSpec: GlobalSpec) : FragmentStateAdapter(
        fa!!
    ) {
        /**
         * 数量
         */
        var numItems = 0

        /**
         * 标题
         */
        var mTitles = ArrayList<String>()

        init {

            // 默认选择谁的类型
            var defaultPositionType = ALBUM
            if (mSpec.defaultPosition == RECORDER) {
                // 默认语音
                defaultPositionType = RECORDER
            } else if (mSpec.defaultPosition == CAMERA) {
                // 默认录制
                defaultPositionType = CAMERA
            }
            val mTabEntities = ArrayList<CustomTabEntity>()

            // 根据相关配置做相应的初始化，相册生效
            if (albumValid()) {
                numItems++
                val title = getString(R.string.z_multi_library_album)
                mTitles.add(title)
                mTabEntities.add(TabEntity(title, R.drawable.ic_flash_on, R.drawable.ic_flash_on))
            }
            // 相机生效
            if (cameraValid()) {
                if (defaultPositionType == CAMERA) {
                    mDefaultPosition = numItems
                }
                numItems++
                val title = getString(R.string.z_multi_library_take_photos)
                mTitles.add(title)
                mTabEntities.add(TabEntity(title, R.drawable.ic_flash_on, R.drawable.ic_flash_on))
            }
            // 录音生效
            if (recorderValid()) {
                if (defaultPositionType == RECORDER) {
                    mDefaultPosition = numItems
                }
                numItems++
                val title = getString(R.string.z_multi_library_sound_recording)
                mTitles.add(title)
                mTabEntities.add(TabEntity(title, R.drawable.ic_flash_on, R.drawable.ic_flash_on))
            }
            mActivityMainZjhBinding.tableLayout.setTabData(mTabEntities)
        }

        override fun createFragment(position: Int): Fragment {
            return if (mTitles[position] == getString(R.string.z_multi_library_album)) {
                if (numItems <= 1) {
                    AlbumFragment.newInstance(0)
                } else AlbumFragment.newInstance(50)
            } else if (mTitles[position] == getString(R.string.z_multi_library_sound_recording)) {
                SoundRecordingFragment.newInstance()
            } else {
                if (mSpec.cameraSetting != null && mSpec.cameraSetting!!.baseCameraFragment != null) {
                    mSpec.cameraSetting!!.baseCameraFragment!!
                } else {
                    CameraFragment.newInstance()
                }
            }
        }

        override fun getItemCount(): Int {
            return numItems
        }
    }

    companion object {
        private const val ALBUM = 0
        private const val CAMERA = 1
        private const val RECORDER = 2

        /**
         * 权限申请自定义码
         */
        private const val GET_PERMISSION_REQUEST = 100

        /**
         * 跳转到设置界面
         */
        private const val REQUEST_CODE_SETTING = 101

        /**
         * 界面屏幕方向切换\切换别的界面时 会触发onSaveInstanceState
         * 会存储该key的值设置为true
         * 然后在恢复界面时根据该值进行相应处理
         */
        private const val IS_SAVE_INSTANCE_STATE = "IS_SAVE_INSTANCE_STATE"
    }
}