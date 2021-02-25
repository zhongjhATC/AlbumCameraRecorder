package gaode.zhongjh.com.common.listener;

/**
 * 视频编辑的回调
 */
public interface VideoEditListener {

    void onFinish();

    void onProgress(int progress, long progressTime);

    void onCancel();

    void onError(String message);

}
