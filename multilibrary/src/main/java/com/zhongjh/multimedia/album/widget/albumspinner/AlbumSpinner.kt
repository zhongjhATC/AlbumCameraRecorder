package com.zhongjh.multimedia.album.widget.albumspinner

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.common.utils.AnimUtils
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.album.entity.Album
import com.zhongjh.multimedia.album.entity.AlbumSpinnerStyle

/**
 * 专辑选项控件
 *
 * @author zhongjh
 * @date 2022/9/21
 */
class AlbumSpinner @SuppressLint("InflateParams") constructor(private val context: Context, private val albumSpinnerStyle: AlbumSpinnerStyle) : PopupWindow() {
    private val window: View = LayoutInflater.from(context).inflate(R.layout.view_album_spinner_zjh, null)
    private val mRecyclerView by lazy {
        window.findViewById<RecyclerView>(R.id.folder_list)
    }
    private val adapter by lazy {
        AlbumSpinnerAdapter(albumSpinnerStyle)
    }
    private var isDismiss = false
    private var ivArrowView: ImageView? = null
    private var tvAlbumTitle: TextView? = null
    private val rootViewBg by lazy {
        window.findViewById<View>(R.id.rootViewBg)
    }

    init {
        this.contentView = window
        this.width = RelativeLayout.LayoutParams.MATCH_PARENT
        this.height = RelativeLayout.LayoutParams.WRAP_CONTENT
        this.animationStyle = R.style.AlbumSpinnerThemeStyle
        this.isFocusable = true
        this.isOutsideTouchable = true
        this.update()
        initView()
    }

    fun initView() {
        mRecyclerView.setLayoutManager(LinearLayoutManager(context))
        mRecyclerView.setAdapter(adapter)
        rootViewBg.setOnClickListener { dismiss() }
    }

    fun bindFolder(albums: List<Album>) {
        adapter.bindAlbums(albums)
        val lp = mRecyclerView.layoutParams
        lp.height = if (albums.size > FOLDER_MAX_COUNT) albumSpinnerStyle.maxHeight else ViewGroup.LayoutParams.WRAP_CONTENT
    }

    val isEmpty: Boolean
        get() = adapter.albums.isEmpty()

    fun setArrowImageView(ivArrowView: ImageView) {
        this.ivArrowView = ivArrowView
        this.ivArrowView?.setImageDrawable(albumSpinnerStyle.drawableDown)
        this.ivArrowView?.setOnClickListener { albumSpinnerOnClick() }
    }

    fun setTitleTextView(tvAlbumTitle: TextView) {
        this.tvAlbumTitle = tvAlbumTitle
        this.tvAlbumTitle?.setOnClickListener { albumSpinnerOnClick() }
    }

    override fun showAsDropDown(anchor: View) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            val location = IntArray(2)
            anchor.getLocationInWindow(location)
            showAtLocation(anchor, Gravity.NO_GRAVITY, 0, location[1] + anchor.height)
        } else {
            super.showAsDropDown(anchor)
        }
        isDismiss = false
        ivArrowView?.let { ivArrowView ->
            ivArrowView.setImageDrawable(albumSpinnerStyle.drawableUp)
            AnimUtils.rotateArrow(ivArrowView, true)
        }
        rootViewBg.animate()
            .alpha(1f)
            .setDuration(250)
            .setStartDelay(250).start()
    }

    fun setOnAlbumItemClickListener(listener: OnAlbumItemClickListener?) {
        adapter.setOnAlbumItemClickListener(listener)
    }

    override fun dismiss() {
        if (isDismiss) {
            return
        }
        rootViewBg.animate()
            .alpha(0f)
            .setDuration(50)
            .start()
        ivArrowView?.let { ivArrowView ->
            ivArrowView.setImageDrawable(albumSpinnerStyle.drawableDown)
            AnimUtils.rotateArrow(ivArrowView, false)
        }
        isDismiss = true
        super@AlbumSpinner.dismiss()
        isDismiss = false
    }

    /**
     * 设置选中状态 - 红色圆点
     */
    fun updateFolderCheckStatus(result: List<Album>) {
        val albums = adapter.albums
        val size = albums.size
        val resultSize = result.size
        for (i in 0 until size) {
            val album = albums[i]
            album.checkedCount = 0
            album.name?.let {
                for (j in 0 until resultSize) {
                    val media = result[j]
                    if (album.name == media.name || -1L == album.id) {
                        album.checkedCount = 1
                        break
                    }
                }
            }
        }
        adapter.bindAlbums(albums)
    }

    /**
     * 设置选中状态
     */
    fun updateCheckStatus(selects: List<Album>) {
        val albums = adapter.albums
        for (album in albums) {
            for (select in selects) {
                if (select.name == album.name) {
                    album.isChecked = true
                    break
                }
            }
        }
        adapter.bindAlbums(albums)
    }

    /**
     * 自动绑定相关View显示本身
     */
    private fun albumSpinnerOnClick() {
        if (this.isShowing) {
            this.dismiss()
        } else {
            tvAlbumTitle?.let {
                this.showAsDropDown(it)
            }
        }
    }

    companion object {
        private const val FOLDER_MAX_COUNT = 8
    }
}
