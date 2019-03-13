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

    }

}
