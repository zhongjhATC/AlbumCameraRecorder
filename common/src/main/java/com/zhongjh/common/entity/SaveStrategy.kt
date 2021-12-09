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
package com.zhongjh.common.entity

/**
 * 设置保存目录的实体
 * @author zhongjh
 * @param isPublic true表示拍照存储在共有目录，false表示存储在私有目录；
 * @param authority AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
 * @param directory 子文件夹的具体路径
 */
class SaveStrategy(var isPublic: Boolean, var authority: String?, var directory: String) {
    init {
        checkNotNull(authority) { "Authority cannot be null" }
    }
}