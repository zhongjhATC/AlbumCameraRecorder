package com.zhongjh.demo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.zhongjh.demo.AlbumUiTest.isInFragment;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.multimedia.album.ui.mediaselection.adapter.AlbumAdapter;
import com.zhongjh.multimedia.preview.PreviewFragment;

import org.hamcrest.Matcher;

import java.util.List;

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

    /**
     * 相册\录制配置只支持视频，测试点击是否拍照
     */
    public static void videoSupport(AlbumUiTest albumUiTest, UiDevice uiDevice) {
        // 只支持视频
        onView(withId(R.id.cbAlbumImage)).perform(scrollTo(), click());
        onView(withId(R.id.cbCameraImage)).perform(scrollTo(), click());
        // 测试点击是否拍照
        albumUiTest.clickGridViewItem(0);
        albumUiTest.clickMainTab(1);
        albumUiTest.takePhoto();
        uiDevice.pressBack();
        waitMainActivityReady(uiDevice);
        // 恢复配置
        onView(withId(R.id.cbAlbumImage)).perform(scrollTo(), click());
        onView(withId(R.id.cbCameraImage)).perform(scrollTo(), click());
    }

    public static void verifyEditedImageAddedToAlbum(AlbumUiTest albumUiTest, UiDevice uiDevice) {
        verifyEditedImageAddedToAlbum(albumUiTest, uiDevice, true);
        uiDevice.pressBack();
        // 重新点击按钮进入灵活配置版用例
        onView(withId(R.id.btnConfigure)).perform(click());
        // 修改不支持 预览界面的图片编辑后加入相册
        waitMainActivityReady(uiDevice);
        onView(withId(R.id.cbIsAddAlbumByEdit)).perform(scrollTo(), click());
        verifyEditedImageAddedToAlbum(albumUiTest, uiDevice, false);
    }

    /**
     * 验证预览界面的图片编辑后加入相册
     *
     * @param isAddedToAlbum 是否要加入相册
     */
    public static void verifyEditedImageAddedToAlbum(AlbumUiTest albumUiTest, UiDevice uiDevice, boolean isAddedToAlbum) {
        // 测试 预览界面的图片编辑后加入相册
        albumUiTest.clickGridViewItem(0);
        albumUiTest.clickMainTab(0);
        clickAlbumItem(1);
        // 点击编辑
        uiDevice.wait(Until.hasObject(By.res(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceName(com.zhongjh.multimedia.R.id.tvEdit))), 10000);
        onView(withId(com.zhongjh.multimedia.R.id.tvEdit)).perform(click());
        // 点击编辑确认
        uiDevice.wait(Until.hasObject(By.res(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceName(com.zhongjh.imageedit.R.id.iBtnDone))), 10000);
        onView(withId(com.zhongjh.imageedit.R.id.iBtnDone)).perform(click());
        // 点击选择
        Matcher<View> checkView = allOf(
                withId(com.zhongjh.multimedia.R.id.checkView),
                isInFragment(PreviewFragment.class),
                isDisplayed()
        );
        onView(checkView)
                .check(matches(isDisplayed()))
                .perform(click());
        // 点击选择确认
        Matcher<View> buttonApply = allOf(
                withId(com.zhongjh.multimedia.R.id.buttonApply),
                isInFragment(PreviewFragment.class),
                isDisplayed()
        );
        onView(buttonApply)
                .check(matches(isDisplayed()))
                .perform(click());
        waitMainActivityReady(uiDevice);
        albumUiTest.clickGridViewItem(1);
        albumUiTest.clickMainTab(0);
        // 检查最新的是不是刚刚编辑完成的
        verifyAlbumListContainsUri(PreviewFragment.TEST_LAST_CAPTURE_URI, isAddedToAlbum);
        // 清空
        PreviewFragment.TEST_LAST_CAPTURE_URI = null;
        uiDevice.pressBack();
    }

    /**
     * 测试 [编辑图片，拍摄处编辑多个图片,相册处编辑图片] 和 [非编辑] 两个功能
     */
    public static void testEdit(AlbumUiTest albumUiTest, UiDevice uiDevice) {
        // 测试 [编辑图片，拍摄处编辑多个图片,相册处编辑图片]
        testEditImagesFromCaptureAndAlbum(albumUiTest,uiDevice);
        // 去掉编辑选项
        onView(withId(R.id.cbIsAddAlbumByEdit)).perform(scrollTo(), click());
        // 测试 [非编辑]

    }

    /**
     * 验证是否配置首页
     */
    private static void waitMainActivityReady(UiDevice uiDevice) {
        uiDevice.wait(Until.hasObject(By.res(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceName(R.id.btnSubmitUpload))), 10000);
    }

    /**
     * 点击相册的第二个数据
     *
     */
    private static void clickAlbumItem(int position) {
        onView(withId(com.zhongjh.multimedia.R.id.recyclerview))
                .check(matches(isDisplayed()))
                .perform(new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isAssignableFrom(RecyclerView.class);
                    }

                    @Override
                    public String getDescription() {
                        return "点击相册RecyclerView" + position + "项的item";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        RecyclerView recyclerView = (RecyclerView) view;
                        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                        int adapterCount = adapter != null ? adapter.getItemCount() : 0;
                        Log.d("TEST_LOG", "clickAlbumItems adapterCount=" + adapterCount);

                        // i从1开始，跳过拍照入口item
                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                        if (holder != null) {
                            // 媒体item的点击监听挂在MediaGrid内部的缩略图(media_thumbnail)上，
                            // itemView本身没有OnClickListener（只有拍照入口position=0才设置了itemView点击），
                            // 所以必须点缩略图才能触发onMediaClick打开预览
                            View thumbnail = holder.itemView.findViewById(com.zhongjh.multimedia.R.id.media_thumbnail);
                            Log.d("TEST_LOG", "clickAlbumItems position=" + position
                                    + " thumbnail=" + (thumbnail != null));
                            if (thumbnail != null) {
                                thumbnail.performClick();
                            } else {
                                // 兜底：拍照入口item只有itemView有点击
                                holder.itemView.performClick();
                            }
                        } else {
                            Log.d("TEST_LOG", "clickAlbumItems position=" + position + " holder=null，item不在屏幕可视区域");
                        }
                        uiController.loopMainThreadForAtLeast(500);
                    }
                });

    }

    /**
     * 校验相册九宫列表中包含指定Uri
     *
     * @param expectUri 埋点拿到的预期Uri
     */
    private static void verifyAlbumListContainsUri(Uri expectUri, boolean isAddedToAlbum) {
        onView(withId(com.zhongjh.multimedia.R.id.recyclerview))
                .check(matches(isDisplayed()))
                .perform(new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isAssignableFrom(RecyclerView.class);
                    }

                    @Override
                    public String getDescription() {
                        return "读取相册列表Adapter数据，校验是否包含目标Uri";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        RecyclerView recyclerView = (RecyclerView) view;
                        AlbumAdapter adapter = (AlbumAdapter) recyclerView.getAdapter();
                        assertNotNull("adapter为空", adapter);

                        // 拿到Adapter内的媒体列表（你自己的方法 getMediaList()）
                        List<LocalMedia> localMediaList = adapter.getData();
                        boolean found;
                        if (isAddedToAlbum) {
                            found = localMediaList.get(0).getUri().equals(expectUri.toString());
                        } else {
                            if (expectUri == null) {
                                found = true;
                            } else {
                                found = !localMediaList.get(0).getUri().equals(expectUri.toString());
                            }
                        }
                        assertTrue("相册列表未找到Uri：" + localMediaList.get(0).getUri() + " 对比 " + expectUri, found);
                    }
                });
    }

    /**
     * 测试编辑图片，拍摄处编辑多个图片,相册处编辑图片
     */
    private static void testEditImagesFromCaptureAndAlbum(AlbumUiTest albumUiTest, UiDevice uiDevice) {
        // 打开三合一
        albumUiTest.clickGridViewItem(0);
        // 拍摄3个图片
        albumUiTest.clickMainTab(1);
        for (int i = 0; i < 3; i++) {
            albumUiTest.takePhoto();
        }
        // 点击第一个图片
        clickCameraItem(0);
        // 点击编辑图片
        uiDevice.wait(Until.hasObject(By.res(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceName(com.zhongjh.multimedia.R.id.tvEdit))), 10000);
        onView(withId(com.zhongjh.multimedia.R.id.tvEdit)).perform(click());
        // 点击绘制
        Matcher<View> rbDoodle = allOf(withId(com.zhongjh.imageedit.R.id.rb_doodle),isDisplayed());
        onView(rbDoodle).check(matches(isDisplayed())).perform(click());
        // 随便在屏幕中间画
        int screenW = uiDevice.getDisplayWidth();
        int screenH = uiDevice.getDisplayHeight();
        int startX = screenW / 2 - 150;
        int startY = screenH / 2;
        int endX = screenW / 2 + 150;
        int endY = screenH / 2 + 200;
        uiDevice.swipe(startX, startY, endX, endY, 300);
        // 点击编辑确认
        uiDevice.wait(Until.hasObject(By.res(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceName(com.zhongjh.imageedit.R.id.iBtnDone))), 10000);
        onView(withId(com.zhongjh.imageedit.R.id.iBtnDone)).perform(click());
        // 点击确定
        Matcher<View> buttonApply = allOf(
                withId(com.zhongjh.multimedia.R.id.buttonApply),
                isInFragment(PreviewFragment.class),
                isDisplayed()
        );
        onView(buttonApply)
                .check(matches(isDisplayed()))
                .perform(click());
        // 点击第二个图片
        clickCameraItem(1);
        // 点击编辑图片
        uiDevice.wait(Until.hasObject(By.res(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceName(com.zhongjh.multimedia.R.id.tvEdit))), 10000);
        onView(withId(com.zhongjh.multimedia.R.id.tvEdit)).perform(click());
        // 点击绘制
        Matcher<View> rbDoodle2 = allOf(withId(com.zhongjh.imageedit.R.id.rb_doodle),isDisplayed());
        onView(rbDoodle2).check(matches(isDisplayed())).perform(click());
        // 随便在屏幕中间画
        int screenW2 = uiDevice.getDisplayWidth();
        int screenH2 = uiDevice.getDisplayHeight();
        int startX2 = screenW2 / 2 - 150;
        int startY2 = screenH2 / 2;
        int endX2 = screenW2 / 2 + 150;
        int endY2 = screenH2 / 2 + 200;
        uiDevice.swipe(startX2, startY2, endX2, endY2, 300);
        // 点击编辑确认
        uiDevice.wait(Until.hasObject(By.res(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceName(com.zhongjh.imageedit.R.id.iBtnDone))), 10000);
        onView(withId(com.zhongjh.imageedit.R.id.iBtnDone)).perform(click());
        // 往左边滑动跳转到第三个
        onView(withContentDescription("ViewPager2")).perform(ViewActions.swipeLeft());
        // 点击编辑图片
        uiDevice.wait(Until.hasObject(By.res(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceName(com.zhongjh.multimedia.R.id.tvEdit))), 10000);
        onView(withId(com.zhongjh.multimedia.R.id.tvEdit)).perform(click());
        // 点击绘制
        Matcher<View> rbDoodle3 = allOf(withId(com.zhongjh.imageedit.R.id.rb_doodle),isDisplayed());
        onView(rbDoodle3).check(matches(isDisplayed())).perform(click());
        // 随便在屏幕中间画
        int screenW3 = uiDevice.getDisplayWidth();
        int screenH3 = uiDevice.getDisplayHeight();
        int startX3 = screenW3 / 2 - 150;
        int startY3 = screenH3 / 2;
        int endX3 = screenW3 / 2 + 150;
        int endY3 = screenH3 / 2 + 200;
        uiDevice.swipe(startX3, startY3, endX3, endY3, 300);
        // 点击编辑确认
        uiDevice.wait(Until.hasObject(By.res(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceName(com.zhongjh.imageedit.R.id.iBtnDone))), 10000);
        onView(withId(com.zhongjh.imageedit.R.id.iBtnDone)).perform(click());
        // 点击确定
        Matcher<View> buttonApply2 = allOf(
                withId(com.zhongjh.multimedia.R.id.buttonApply),
                isInFragment(PreviewFragment.class),
                isDisplayed()
        );
        onView(buttonApply2)
                .check(matches(isDisplayed()))
                .perform(click());
        // 三合一界面点击确定
        albumUiTest.btnConfirm();
    }

    /**
     * 点击拍摄界面的图片数据
     *
     */
    private static void clickCameraItem(int position) {
        onView(withId(com.zhongjh.multimedia.R.id.rlPhoto))
                .check(matches(isDisplayed()))
                .perform(new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isAssignableFrom(RecyclerView.class);
                    }

                    @Override
                    public String getDescription() {
                        return "点击拍摄界面的RecyclerView" + position + "项的item";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        RecyclerView recyclerView = (RecyclerView) view;
                        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                        int adapterCount = adapter != null ? adapter.getItemCount() : 0;
                        Log.d("TEST_LOG", "clickCameraItem adapterCount=" + adapterCount);

                        // i从1开始，跳过拍照入口item
                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                        if (holder != null) {
                            // 媒体item的点击监听挂在MediaGrid内部的缩略图(media_thumbnail)上，
                            // itemView本身没有OnClickListener（只有拍照入口position=0才设置了itemView点击），
                            // 所以必须点缩略图才能触发onMediaClick打开预览
//                            View thumbnail = holder.itemView.findViewById(com.zhongjh.multimedia.R.id.imgPhoto);
//                            Log.d("TEST_LOG", "clickCameraItem position=" + position
//                                    + " thumbnail=" + (thumbnail != null));
//                            if (thumbnail != null) {
//                                thumbnail.performClick();
//                            } else {
                                // 兜底：拍照入口item只有itemView有点击
                                holder.itemView.performClick();
//                            }
                        } else {
                            Log.d("TEST_LOG", "clickCameraItem position=" + position + " holder=null，item不在屏幕可视区域");
                        }
                        uiController.loopMainThreadForAtLeast(500);
                    }
                });

    }
}
