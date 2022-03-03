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

package com.zhongjh.albumcamerarecorder.album.listener;

import android.net.Uri;
import androidx.annotation.NonNull;

import com.zhongjh.common.entity.LocalFile;

import java.util.List;

/**
 * 相册item事件
 * @author zhihu
 */
public interface OnSelectedListener {

    /**
     * 每次选择的事件
     * @param localFiles 所选项目{@link com.zhongjh.common.entity.LocalFile} 列表.
     */
    void onSelected(@NonNull List<LocalFile> localFiles);

}
