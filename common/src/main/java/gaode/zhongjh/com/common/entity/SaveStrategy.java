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
package gaode.zhongjh.com.common.entity;

/**
 * 设置保存目录的实体
 */
public class SaveStrategy {

    // 参数1 true表示拍照存储在共有目录，false表示存储在私有目录；
    public boolean isPublic;
    // 参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
    public String authority;
    // 参数3 子文件夹的具体路径
    public String directory;

    public SaveStrategy(boolean isPublic, String authority, String directory) {
        this.isPublic = isPublic;
        this.authority = authority;
        this.directory = directory;
    }


}
