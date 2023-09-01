package com.zhongjh.albumcamerarecorder.preview.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.album.utils.AlbumCompressFileTask
import com.zhongjh.albumcamerarecorder.album.widget.CheckRadioView
import com.zhongjh.albumcamerarecorder.album.widget.CheckView
import com.zhongjh.albumcamerarecorder.preview.adapter.PreviewPagerAdapter
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec
import com.zhongjh.common.utils.MediaStoreCompat
import com.zhongjh.common.utils.StatusBarUtils.initStatusBar

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
open class BasePreviewFragment2 : Fragment() {

    protected val TAG: String = this@BasePreviewFragment2.javaClass.simpleName

    protected lateinit var mContext: Context
    protected lateinit var mGlobalSpec: GlobalSpec
    protected lateinit var mAlbumSpec: AlbumSpec

    /**
     * 图片存储器
     */
    private lateinit var mPictureMediaStoreCompat: MediaStoreCompat

    /**
     * 录像文件配置路径
     */
    private lateinit var mVideoMediaStoreCompat: MediaStoreCompat

    /**
     * 打开ImageEditActivity的回调
     */
    protected lateinit var mImageEditActivityResult: ActivityResultLauncher<Intent>

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
        // 获取配置
        mGlobalSpec = GlobalSpec
        mAlbumSpec = AlbumSpec
        // 获取样式
        val wrapper: ContextThemeWrapper = ContextThemeWrapper(
            requireActivity(),
            mGlobalSpec.themeId
        )
        val cloneInContext = inflater.cloneInContext(wrapper)
        val view: View =
            cloneInContext.inflate(R.layout.fragment_preview_zjh, container, false)
        onActivityResult()
        initStatusBar(requireActivity())
        var isAllowRepeat = false
        if (savedInstanceState == null) {
            // 初始化别的界面传递过来的数据
            arguments?.let {
                isAllowRepeat = it.getBoolean(BasePreviewFragment.EXTRA_IS_ALLOW_REPEAT, false)
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
            // 初始化缓存的数据
            mOriginalEnable = savedInstanceState.getBoolean(BasePreviewFragment.CHECK_STATE)
        }

        // 设置图片路径
        mGlobalSpec.pictureStrategy?.let {
            // 如果设置了图片的文件夹路径，就使用它的
            mPictureMediaStoreCompat = MediaStoreCompat(mContext, it)
        } ?: let {
            mGlobalSpec.saveStrategy?.let {
                // 否则使用全局的
                mPictureMediaStoreCompat = MediaStoreCompat(mContext, it)
            } ?: let {
                // 全局如果都没有，抛错
                throw RuntimeException("Please set the GlobalSpec <saveStrategy> or <pictureStrategy> configuration.")
            }
        }

        // 设置视频路径
        mGlobalSpec.videoStrategy?.let {
            // 如果设置了图片的文件夹路径，就使用它的
            mVideoMediaStoreCompat = MediaStoreCompat(mContext, it)
        } ?: let {
            mGlobalSpec.saveStrategy?.let {
                // 否则使用全局的
                mVideoMediaStoreCompat = MediaStoreCompat(mContext, it)
            } ?: let {
                // 全局如果都没有，抛错
                throw RuntimeException("Please set the GlobalSpec <saveStrategy> or <videoStrategy> configuration.")
            }
        }

        mViewHolder = ViewHolder(view)
        mAdapter = PreviewPagerAdapter(mContext, mActivity)
        mViewHolder.pager.setAdapter(mAdapter)
        mViewHolder.checkView.setCountable(mAlbumSpec.countable)
        mAlbumCompressFileTask = AlbumCompressFileTask(
            mContext,
            TAG,
            BasePreviewFragment::class.java,
            mGlobalSpec,
            mPictureMediaStoreCompat,
            mVideoMediaStoreCompat
        )
        initListener()
        return view
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
     * 针对回调
     */
    private fun onActivityResult() {
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

    class ViewHolder internal constructor(var rootView: View) {
        var pager: ViewPager2
        var iBtnBack: ImageButton
        var tvEdit: TextView
        var original: CheckRadioView
        var originalLayout: LinearLayout
        var size: TextView
        var buttonApply: TextView
        var bottomToolbar: FrameLayout
        var checkView: CheckView
        var pbLoading: ProgressBar

        init {
            pager = rootView.findViewById(R.id.pager)
            iBtnBack = rootView.findViewById(R.id.ibtnBack)
            tvEdit = rootView.findViewById(R.id.tvEdit)
            original = rootView.findViewById(R.id.original)
            originalLayout = rootView.findViewById(R.id.originalLayout)
            size = rootView.findViewById(R.id.size)
            buttonApply = rootView.findViewById(R.id.buttonApply)
            bottomToolbar = rootView.findViewById(R.id.bottomToolbar)
            checkView = rootView.findViewById(R.id.checkView)
            pbLoading = rootView.findViewById(R.id.pbLoading)
        }
    }

}