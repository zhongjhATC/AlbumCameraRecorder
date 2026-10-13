package com.zhongjh.demo;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

public class TestConfigure {

    private TestConfigure() {
    }

    /**
     * 默认配置测试
     * 测试录制1个，音频2个，选满照片
     */
    public static void Default(AlbumUiTest albumUiTest, UiDevice uiDevice) throws InterruptedException {
        // 视频2个
        albumUiTest.clickGridViewItem(0);
        albumUiTest.recordVideoSteps();
        // 音频2个
        albumUiTest.clickGridViewItem(1);
        albumUiTest.recordAudioSteps();
        albumUiTest.clickGridViewItem(2);
        albumUiTest.recordAudioSteps();
        // 相册选满照片
        albumUiTest.clickGridViewItem(3);
        albumUiTest.clickMainTab(0);
        albumUiTest.selectAlbumItems(5);
        albumUiTest.albumConfirm();
        // 检测没有+号才是正常的
        waitMainActivityReady(uiDevice);
        albumUiTest.assertGridViewLastMediaNotAdd();
    }

    private static void waitMainActivityReady(UiDevice uiDevice) {
        uiDevice.wait(Until.hasObject(By.desc("btnSubmitUpload")), 10000);
    }
}
