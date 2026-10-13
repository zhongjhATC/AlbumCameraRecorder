package com.zhongjh.demo;

public class TestConfigure {

    private TestConfigure() {
    }

    /**
     * 默认配置测试
     * 测试录制2个，音频2个，选满照片
     */
    public static void Default(AlbumUiTest albumUiTest) throws InterruptedException {
        // 视频2个
        albumUiTest.clickGridViewItem(0);
        albumUiTest.recordVideoSteps();
        albumUiTest.clickGridViewItem(1);
        albumUiTest.recordVideoSteps();
        // 音频2个
        albumUiTest.clickGridViewItem(2);
        albumUiTest.recordAudioSteps();
        albumUiTest.clickGridViewItem(3);
        albumUiTest.recordAudioSteps();
        // 选满照片
        albumUiTest.clickGridViewItem(0);
        albumUiTest.clickMainTab(1);
        for (int i = 0; i < 18; i++) {
            albumUiTest.takePhoto();
        }
        albumUiTest.btnConfirm();


        // 检测没有+号才是正常的
        albumUiTest.assertGridViewLastMediaNotAdd();
    }
}
