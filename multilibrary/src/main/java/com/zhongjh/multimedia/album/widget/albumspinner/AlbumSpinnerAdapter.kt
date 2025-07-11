package com.zhongjh.multimedia.album.widget.albumspinner

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.album.entity.Album
import com.zhongjh.multimedia.album.entity.AlbumSpinnerStyle
import com.zhongjh.multimedia.settings.GlobalSpec.imageEngine
import com.zhongjh.multimedia.utils.AttrsUtils

/**
 * 专辑下拉选项框的适配器
 *
 * @author zhongjh
 */
class AlbumSpinnerAdapter(private val albumSpinnerStyle: AlbumSpinnerStyle) : RecyclerView.Adapter<AlbumSpinnerAdapter.ViewHolder>() {
    var albums: List<Album> = ArrayList()
        private set

    fun bindAlbums(albums: List<Album>) {
        // 计算新老数据集差异，将差异更新到Adapter
        val diffResult = DiffUtil.calculateDiff(AlbumCallback(this.albums, albums))
        this.albums = albums
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_album_zjh, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = albums[position]
        val name = album.name
        val imageNum = album.count.toLong()
        val isChecked = album.isChecked
        val checkedNum = album.checkedCount
        holder.tvSign.visibility = if (checkedNum > 0) View.VISIBLE else View.INVISIBLE
        holder.itemView.isSelected = isChecked
        imageEngine.loadThumbnail(
            holder.itemView.context.applicationContext, holder.itemView.context.resources.getDimensionPixelSize(R.dimen.z_media_grid_size),
            albumSpinnerStyle.placeholder, holder.imgFirst, album.firstImagePath ?: ""
        )
        val context = holder.itemView.context
        holder.tvName.text = context.getString(R.string.z_multi_library_album_num, name, imageNum)
        holder.itemView.setOnClickListener {
            onAlbumItemClickListener?.let {
                val size = albums.size
                for (i in 0 until size) {
                    val item = albums[i]
                    item.isChecked = false
                }
                album.isChecked = true
                notifyItemRangeChanged(0, albums.size)
                it.onItemClick(position, album)
            }
        }
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFirst: ImageView = itemView.findViewById(R.id.imgFirst)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvSign: TextView = itemView.findViewById(R.id.tvSign)

        init {
            // 获取下拉框的样式集合
            val typedValue = TypedValue()
            itemView.context.theme.resolveAttribute(R.attr.album_listPopupWindowStyle, typedValue, true)

            // item背景
            val backgroundStyle = AttrsUtils.getTypeValueDrawable(itemView.context, typedValue.resourceId, R.attr.album_backgroundStyle, R.drawable.spinner_item_select_bg_white)
            itemView.background = backgroundStyle

            // 该专辑里面有图片被选择时
            val folderCheckedDotDrawable = AttrsUtils.getTypeValueDrawable(itemView.context, typedValue.resourceId, R.attr.album_checkDotStyle, R.drawable.ic_orange_oval)
            tvSign.background = folderCheckedDotDrawable

            // 专辑字体颜色
            val folderTextColor = AttrsUtils.getTypeValueColor(itemView.context, typedValue.resourceId, R.attr.album_textColor)
            if (folderTextColor != 0) {
                tvName.setTextColor(folderTextColor)
            }

            // 专辑字体大小
            val folderTextSize = AttrsUtils.getTypeValueSizeForInt(itemView.context, typedValue.resourceId, R.attr.album_textSize)
            if (folderTextSize != 0) {
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, folderTextSize.toFloat())
            }
        }
    }

    private var onAlbumItemClickListener: OnAlbumItemClickListener? = null

    fun setOnAlbumItemClickListener(listener: OnAlbumItemClickListener?) {
        this.onAlbumItemClickListener = listener
    }
}
