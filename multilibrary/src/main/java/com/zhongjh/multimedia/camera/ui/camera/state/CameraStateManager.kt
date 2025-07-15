package com.zhongjh.multimedia.camera.ui.camera.state

import android.view.View
import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager
import com.zhongjh.multimedia.camera.ui.camera.state.type.PictureMultiple
import com.zhongjh.multimedia.camera.ui.camera.state.type.PictureSingle
import com.zhongjh.multimedia.camera.ui.camera.state.type.Preview
import com.zhongjh.multimedia.camera.ui.camera.state.type.VideoMultiple
import com.zhongjh.multimedia.camera.ui.camera.state.type.VideoMultipleIn
import com.zhongjh.multimedia.camera.ui.camera.state.type.impl.IState
import java.lang.ref.WeakReference

/**
 * CameraLayout涉及到状态改变的事件都在这里
 * 录制视频：
 * 默认状态Preview - 录制中VideoIn - 录制完成VideoComplete - 关闭视频预览回到初始界面Preview
 * 录制多个视频：
 * 默认状态Preview - 录制中VideoMultipleIn - 录制完一小节VideoMultiple - 回退节点至没有视频节点Preview,如果有节点则是VideoMultiple - 即使点击录制完成依然保持VideoMultiple
 *
 * @author zhongjh
 * @date 2021/11/25
 */
open class CameraStateManager(cameraFragment: BaseCameraFragment<out CameraStateManager, out CameraPictureManager, out CameraVideoManager>) : IState {

    // 使用弱引用持有 Fragment
    private val fragmentRef = WeakReference(cameraFragment)

    /**
     * 当前状态
     */
    var state: IState

    /**
     * 预览状态
     */
    val preview: IState = Preview(cameraFragment, this)

    /**
     * 图片完成状态
     */
    val pictureComplete: IState = PictureSingle(cameraFragment, this)

    /**
     * 多个图片状态，至少有一张图片情况
     */
    val pictureMultiple: IState = PictureMultiple(cameraFragment, this)

    /**
     * 多个视频状态，至少有一段视频情况
     */
    val videoMultiple: IState = VideoMultiple(cameraFragment, this)

    /**
     * 正在录制多个视频中的状态
     */
    val videoMultipleIn: IState = VideoMultipleIn(cameraFragment, this)

    init {
        // 设置当前默认状态
        state = preview
    }

    override fun getName(): String {
        return "CameraStateManager"
    }

    override fun onActivityPause() {
        state.onActivityPause()
    }

    override fun onBackPressed(): Boolean? {
        return state.onBackPressed()
    }

    override fun pvLayoutCommit() {
        state.pvLayoutCommit()
    }

    override fun pvLayoutCancel() {
        state.pvLayoutCancel()
    }

    override fun pauseRecord() {
        // 显示右上角菜单
        fragmentRef.get()?.setMenuVisibility(View.VISIBLE)
        state.pauseRecord()
    }

    override fun stopProgress() {
        state.stopProgress()
    }

    override fun onLongClickFinish() {
        state.onLongClickFinish()
    }
}
