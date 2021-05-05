package gaode.zhongjh.com.common.listener;

/**
 * 视频编辑的回调
 * @author zhongjh
 */
public interface VideoEditListener {

    /**
     * 完成
     */
    void onFinish();

    /**
     * 进度
     * @param progress 进度百分比
     * @param progressTime 进度时间
     */
    void onProgress(int progress, long progressTime);

    /**
     * 取消
     */
    void onCancel();

    /**
     * 异常
     * @param message 信息
     */
    void onError(String message);

}
