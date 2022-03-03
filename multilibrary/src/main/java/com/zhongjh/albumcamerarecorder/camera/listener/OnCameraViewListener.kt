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
package com.zhongjh.albumcamerarecorder.camera.listener

import com.otaliastudios.cameraview.CameraView

/**
 * CameraView事件
 * @author zhongjh
 */
interface OnCameraViewListener {
    /**
     * 自定义cameraView事件
     * @param cameraView cameraView本身
     */
    fun onInitListener(cameraView: CameraView)
}