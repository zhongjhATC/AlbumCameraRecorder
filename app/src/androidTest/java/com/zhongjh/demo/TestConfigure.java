package com.zhongjh.demo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

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
    public static void defaultTest(AlbumUiTest albumUiTest, UiDevice uiDevice) throws InterruptedException {
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

    /**
     * 测试纯图片功能，拍照2个，相册选满照片，测试拍照时，长按看看会不会出现bug
     */
    public static void imageSupport(AlbumUiTest albumUiTest, UiDevice uiDevice) throws InterruptedException {
        // 只支持图片
        onView(withId(R.id.cbAlbumVideo)).perform(scrollTo(), click());
        onView(withId(R.id.cbCameraVideo)).perform(scrollTo(), click());
        // 拍照两个
        albumUiTest.clickGridViewItem(0);
        albumUiTest.clickMainTab(1);
        for (int i = 0; i < 2; i++) {
            albumUiTest.takePhoto();
        }
        albumUiTest.btnConfirm();
        // 试下长按是否出现bug
        albumUiTest.clickGridViewItem(2);
        albumUiTest.clickMainTab(1);
        albumUiTest.recordVideo(5500);
        uiDevice.pressBack();
        // 相册选满照片
        albumUiTest.clickGridViewItem(2);
        albumUiTest.clickMainTab(0);
        albumUiTest.selectAlbumItems(5);
        albumUiTest.albumConfirm();
        waitMainActivityReady(uiDevice);
        // 恢复配置
        onView(withId(R.id.cbAlbumVideo)).perform(scrollTo(), click());
        onView(withId(R.id.cbCameraVideo)).perform(scrollTo(), click());
    }

    private static void waitMainActivityReady(UiDevice uiDevice) {
        uiDevice.wait(Until.hasObject(By.desc("btnSubmitUpload")), 10000);
    }
}
