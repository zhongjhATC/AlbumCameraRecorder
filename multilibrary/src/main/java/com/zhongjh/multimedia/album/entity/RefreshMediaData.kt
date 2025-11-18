package com.zhongjh.multimedia.album.entity

import androidx.recyclerview.widget.DiffUtil
import com.zhongjh.common.entity.LocalMedia

class RefreshMediaData {

    lateinit var data: List<LocalMedia>
    lateinit var diffResult: DiffUtil.DiffResult
}