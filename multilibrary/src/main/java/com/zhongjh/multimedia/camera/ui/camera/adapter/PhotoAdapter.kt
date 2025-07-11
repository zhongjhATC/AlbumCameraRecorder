package com.zhongjh.multimedia.camera.ui.camera.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.common.listener.OnMoreClickListener
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.camera.entity.BitmapData
import com.zhongjh.multimedia.preview.start.PreviewStartManager.startPreviewActivityByCamera
import com.zhongjh.multimedia.settings.GlobalSpec
import java.lang.ref.WeakReference

/**
 * 横向形式显示多个图片的
 *
 * @author zhongjh
 * @date 2021/10/9
 */
class PhotoAdapter(activity: Activity, private val globalSpec: GlobalSpec, var listData: MutableList<BitmapData>, private var photoAdapterListener: PhotoAdapterListener?) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    private val tag: String = PhotoAdapter::class.java.simpleName
    private val activityRef: WeakReference<Activity> = WeakReference(activity)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(LayoutInflater.from(activityRef.get()).inflate(R.layout.item_image_multilibrary_zjh, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val activity = activityRef.get() ?: return
        val bitmapData = listData[position]
        globalSpec.imageEngine.loadUriImage(activity, holder.imgPhoto, bitmapData.absolutePath)
        // 点击图片
        val currentPosition = holder.absoluteAdapterPosition
        holder.itemView.setOnClickListener(object : OnMoreClickListener() {
            override fun onListener(v: View) {
                onClickListener(currentPosition)
            }
        })
        holder.imgCancel.setOnClickListener(object : OnMoreClickListener() {
            override fun onListener(v: View) {
                removePosition(bitmapData)
            }
        })
    }

    fun dispatchUpdatesTo(listData: MutableList<BitmapData>) {
        // 计算新老数据集差异，将差异更新到Adapter
        val diffResult = DiffUtil.calculateDiff(PhotoCallback(this.listData, listData))
        this.listData = listData
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * 根据索引删除view
     *
     * @param bitmapData 数据
     */
    fun removePosition(bitmapData: BitmapData) {
        val position = listData.indexOf(bitmapData)
        Log.d(tag, "removePosition $position")
        listData.remove(bitmapData)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listData.size)
        photoAdapterListener?.onPhotoAdapterDelete(bitmapData, position)
    }

    override fun getItemCount(): Int {
        Log.d(tag, "getItemCount")
        return listData.size
    }

    /**
     * 释放资源，防止内存泄露
     */
    fun release() {
        photoAdapterListener = null
    }

    /**
     * 点击事件
     *
     * @param position 索引
     */
    private fun onClickListener(position: Int) {
        val activity = activityRef.get() ?: return
        val intent = startPreviewActivityByCamera(activity, listData, position)
        photoAdapterListener?.onPhotoAdapterClick(intent)
    }

    class PhotoViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhoto: ImageView = itemView.findViewById(R.id.imgPhoto)
        val imgCancel: ImageView = itemView.findViewById(R.id.imgCancel)
    }
}
