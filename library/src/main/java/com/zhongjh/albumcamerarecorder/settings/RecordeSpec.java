package com.zhongjh.albumcamerarecorder.settings;

public class RecordeSpec {

    private RecordeSpec() {
    }

    private static final class InstanceHolder {
        private static final RecordeSpec INSTANCE = new RecordeSpec();
    }

    public static RecordeSpec getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static RecordeSpec getCleanInstance() {
        RecordeSpec recordeSpec = getInstance();
        recordeSpec.reset();
        return recordeSpec;
    }

    /**
     * 重置
     */
    private void reset() {
        captureStrategy = null;
    }

    // region 属性
    public CaptureStrategy captureStrategy; // 参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置

    // endregion 属性

}
