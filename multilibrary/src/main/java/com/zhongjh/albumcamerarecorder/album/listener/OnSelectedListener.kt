/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhongjh.albumcamerarecorder.album.listener

import com.zhongjh.common.entity.LocalFile

/**
 * 相册item事件
 * @author zhihu
 */
interface OnSelectedListener {
    /**
     * 每次选择的事件
     * @param localFiles 所选项目[com.zhongjh.common.entity.LocalFile] 列表.
     */
    fun onSelected(localFiles: List<LocalFile>)
}