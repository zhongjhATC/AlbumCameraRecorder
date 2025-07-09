package com.zhongjh.multimedia.album.entity

import androidx.recyclerview.widget.DiffUtil
import com.zhongjh.common.entity.LocalMedia

class ReloadPageMediaData {

    lateinit var data: MutableList<LocalMedia>
    lateinit var diffResult: DiffUtil.DiffResult
}