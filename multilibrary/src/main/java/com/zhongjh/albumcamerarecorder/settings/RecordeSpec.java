package com.zhongjh.albumcamerarecorder.settings;

/**
 * @author zhongjh
 */
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
        // 最长录制时间
        duration = 10;
        // 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
        minDuration = 1500;
    }

    /**
     * 最长录制时间
     */
    public int duration = 10;
    /**
     * 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
     */
    public int minDuration = 1500;

}
