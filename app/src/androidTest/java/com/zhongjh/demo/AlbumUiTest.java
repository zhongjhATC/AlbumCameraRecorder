package com.zhongjh.demo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.zhongjh.circularprogressview.CircularProgress;
import com.zhongjh.demo.phone.MainListActivity;
import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.multimedia.recorder.BaseSoundRecordingFragment;
import com.zhongjh.multimedia.widget.clickorlongbutton.ClickOrLongButton;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AlbumUiTest {

    private ActivityScenario<MainListActivity> scenario;
    private UiDevice uiDevice;

    // 每个@Test执行前，启动MainActivity
    @Before
    public void beforeTest() {
        // 获取当前被测App包名
        scenario = ActivityScenario.launch(MainListActivity.class);
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    // 每个@Test执行完毕，关闭Activity
    @After
    public void afterTest() {
        scenario.close();
    }

    // UI测试用例：点击打开相册按钮
    @Test
    public void testClickOpenAlbumButton() throws Exception {
        // 1. 点击按钮，触发app请求权限，弹出系统权限弹窗
        onView(withId(R.id.btnSimple)).perform(click());

        // 九宫界面 - 点击GridView第0项（第一个格子）
        clickGridViewItem(0);

        // 三合一界面 - 通过所有权限
        passAllPermissions();

        // 等待2秒让界面渲染一会
        Thread.sleep(2000);

        // 三合一界面 - 返回
        uiDevice.pressBack();

        // 九宫界面 - 勾选去掉相册功能
        onView(withId(R.id.cbAlbum))
                .check(matches(isChecked()))
                .perform(click());

        // 九宫界面 - 重新进去
        clickGridViewItem(0);

        // 三合一界面(录制) - 拍满照片
        for (int i = 0; i < 5; i++) {
            takePhoto();
        }

        // 三合一界面(录制) - 然后删光照片
        for (int i = 4; i >= 0; i--) {
            deleteGridViewItemByCameraFragment();
        }

        // 三合一界面(录制) - 接着录像
        recordVideo(5500);

        // 三合一界面(录制) - 再录像,满了会进入到录像预览界面
        recordVideo(4000);

        // 录像预览界面 - 然后点击左上角按钮返回
        closeByCameraFragment();

        // 九宫界面 - 重新进去
        clickGridViewItem(0);

        // 三合一界面(录制) - 再重新录像直到自动满
        recordVideo(12000);

        // 录像预览界面 - 点击确定
        onView(withId(R.id.btnConfirm)).perform(click());

        // 九宫界面 - 删除录像
        deleteGridViewItemByMainFragment();

        // 九宫界面 - 重新进去
        clickGridViewItem(0);

        // 三合一界面(录制) - 接着录像到一半
        recordVideo(5500);

        // 三合一界面(录制) - 点击确定
        btnConfirm();

        // 录像预览界面 - 点击确定
        onView(withId(R.id.btnConfirm)).perform(click());

        // 九宫界面 - 删除录像
        deleteGridViewItemByMainFragment();

        // 九宫界面 - 勾选去掉录音功能
        onView(withId(R.id.cbRecorder))
                .check(matches(isChecked()))
                .perform(click());

        // 九宫界面 - 重新进去
        clickGridViewItem(0);

        // 三合一界面(录制) - 再重新录像直到自动满
        recordVideo(12000);

        // 录像预览界面 - 点击确定
        onView(withId(R.id.btnConfirm)).perform(click());

        // 九宫界面 - 删除录像
        deleteGridViewItemByMainFragment();

        // 九宫界面 - 勾选去掉拍摄功能
        onView(withId(R.id.cbCamera))
                .check(matches(isChecked()))
                .perform(click());

        // 九宫界面 - 选择录音功能
        onView(withId(R.id.cbRecorder))
                .check(matches(isNotChecked()))
                .perform(click());

        // 九宫界面 - 重新进去
        clickGridViewItem(0);

        // 三合一界面(录音) - 再重新录音直到自动满
        recordAudio(12000);

        // 三合一界面(录音) - 点击确定回到九宫界面
        btnConfirmByAudio();

        // 九宫界面 - 删除录音
        deleteGridViewItemByMainFragment();

        // 九宫界面 - 重新进去
        clickGridViewItem(0);

        // 三合一界面(录音) - 再重新录音一半
        recordAudio(7000);

        // 三合一界面(录音) - 点击确定回到九宫界面
        btnConfirmByAudio();

        // 等待2秒让界面渲染一会
        Thread.sleep(2000);
    }

    /**
     * 点击gridView
     *
     * @param position 索引
     */
    private void clickGridViewItem(int position) {
        onView(withId(R.id.gridView))
                .check(matches(isDisplayed()))
                .perform(new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isAssignableFrom(FrameLayout.class);
                    }

                    @Override
                    public String getDescription() {
                        return "获取自定义GridView内部RecyclerView并点击第0个item";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        // view就是com.zhongjh.gridview.widget.GridView实例
                        com.zhongjh.gridview.widget.GridView gridView = (com.zhongjh.gridview.widget.GridView) view;
                        RecyclerView recyclerView = gridView.getRecyclerView();
                        // 点击第0项，修改数字切换不同item
                        actionOnItemAtPosition(position, click()).perform(uiController, recyclerView);
                    }
                });
    }

    /**
     * 点击录制界面-gridView的删除事件
     */
    private void deleteGridViewItemByCameraFragment() {
        onView(withId(R.id.rlPhoto))
                .check(matches(isDisplayed()))
                .perform(new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isAssignableFrom(RecyclerView.class);
                    }

                    @Override
                    public String getDescription() {
                        return "点击录制界面-gridView的删除事件";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        RecyclerView recyclerView = (RecyclerView) view;

                        int position = 0;
                        // 滚动到目标position，确保item被渲染
                        recyclerView.scrollToPosition(position);
                        uiController.loopMainThreadForAtLeast(500);

                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                        if (holder != null) {
                            // 这里换成你item里面按钮的id
                            View btnDelete = holder.itemView.findViewById(com.zhongjh.multimedia.R.id.imgCancel);
                            btnDelete.performClick();
                        }
                    }
                });
    }

    /**
     * 点击主界面-gridView的删除事件
     */
    private void deleteGridViewItemByMainFragment() {
        onView(withId(R.id.gridView))
                .check(matches(isDisplayed()))
                .perform(new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isAssignableFrom(FrameLayout.class);
                    }

                    @Override
                    public String getDescription() {
                        return "获取自定义GridView内部RecyclerView并点击-gridView的删除事件";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        // view就是com.zhongjh.gridview.widget.GridView实例
                        com.zhongjh.gridview.widget.GridView gridView = (com.zhongjh.gridview.widget.GridView) view;
                        RecyclerView recyclerView = gridView.getRecyclerView();

                        int position = 0;
                        // 滚动到目标position，确保item被渲染
                        recyclerView.scrollToPosition(position);
                        uiController.loopMainThreadForAtLeast(500);

                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                        if (holder != null) {
                            // 这里换成你item里面按钮的id
                            View btnDelete = holder.itemView.findViewById(com.zhongjh.gridview.R.id.imgClose);
                            btnDelete.performClick();
                        }
                    }
                });
    }

    /**
     * 通过所有权限
     */
    private void passAllPermissions() {
        // 等待 3秒，通过摄像头权限，匹配包含 "允许"
        UiObject2 permissionBtn = uiDevice.wait(
                Until.findObject(By.textContains("允许")),
                3000
        );
        if (permissionBtn != null) {
            permissionBtn.click();
        }
        // 等待 3秒，通过麦克风权限，匹配包含 "允许"
        UiObject2 permissionBtn2 = uiDevice.wait(
                Until.findObject(By.textContains("允许")),
                3000
        );
        if (permissionBtn2 != null) {
            permissionBtn2.click();
        }

        // 等待 3秒，通过相册权限，匹配包含 "允许"
        UiObject2 permissionBtn3 = uiDevice.wait(
                Until.findObject(By.textContains("允许")),
                3000
        );
        if (permissionBtn3 != null) {
            permissionBtn3.click();
        }
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        Matcher<View> pvLayoutMatcher = allOf(
                withId(R.id.pvLayout),
                isInFragment(BaseCameraFragment.class),
                isDisplayed()
        );

        onView(allOf(
                isAssignableFrom(ClickOrLongButton.class),
                hasAncestor(pvLayoutMatcher),
                isDisplayed()
        )).perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 录像
     */
    private void recordVideo(long holdMs) throws InterruptedException {
        Matcher<View> pvLayoutMatcher = allOf(
                withId(R.id.pvLayout),
                isInFragment(BaseCameraFragment.class),
                isDisplayed()
        );
        Matcher<View> btnMatcher = allOf(
                isAssignableFrom(ClickOrLongButton.class),
                hasAncestor(pvLayoutMatcher),
                isDisplayed()
        );

        // 2. 获取按钮屏幕中心点
        float[] centerPos = getViewScreenCenter(btnMatcher);
        float x = centerPos[0];
        float y = centerPos[1];

        // 3. 执行系统长按
        GestureInstrumentUtil.longPress(x, y, holdMs);
    }

    /**
     * 录像 - 确定
     */
    private void btnConfirm() {
        Matcher<View> pvLayoutMatcher = allOf(
                withId(R.id.pvLayout),
                isInFragment(BaseCameraFragment.class),
                isDisplayed()
        );

        onView(allOf(
                withId(R.id.btnConfirm),
                isAssignableFrom(CircularProgress.class),
                hasAncestor(pvLayoutMatcher),
                isDisplayed()
        )).perform(click());
    }

    /**
     * 录音
     */
    private void recordAudio(long holdMs) throws InterruptedException {
        Matcher<View> pvLayoutMatcher = allOf(
                withId(R.id.pvLayout),
                isInFragment(BaseSoundRecordingFragment.class),
                isDisplayed()
        );
        Matcher<View> btnMatcher = allOf(
                isAssignableFrom(ClickOrLongButton.class),
                hasAncestor(pvLayoutMatcher),
                isDisplayed()
        );

        // 2. 获取按钮屏幕中心点
        float[] centerPos = getViewScreenCenter(btnMatcher);
        float x = centerPos[0];
        float y = centerPos[1];

        // 3. 执行系统长按
        GestureInstrumentUtil.longPress(x, y, holdMs);
    }

    /**
     * 录音 - 确定
     */
    private void btnConfirmByAudio() {
        Log.d("TEST_LOG", "===== 进入 btnConfirmByAudio =====");
        Matcher<View> pvLayoutMatcher = allOf(
                withId(R.id.pvLayout),
                isInFragment(BaseSoundRecordingFragment.class),
                isDisplayed()
        );
        Matcher<View> confirmMatcher = allOf(
                withId(R.id.btnConfirm),
                isAssignableFrom(CircularProgress.class),
                hasAncestor(pvLayoutMatcher),
                isDisplayed()
        );
        Log.d("TEST_LOG", "准备执行 onView 查找 btnConfirm");
        onView(confirmMatcher).perform(click());
        Log.d("TEST_LOG", "===== btnConfirmByAudio 执行完成，click 调用完毕 =====");
    }

    /**
     * 关闭界面,点击左上角按钮
     * 相册界面
     */
    private void closeByCameraFragment() {
        Matcher<View> imgClose = allOf(
                withId(R.id.imgClose),
                isInFragment(BaseCameraFragment.class),
                isDisplayed()
        );
        onView(imgClose).perform(click());
    }

    /**
     * 通用工具：获取view屏幕中心点坐标，返回 [x,y]
     */
    private float[] getViewScreenCenter(Matcher<View> viewMatcher) throws InterruptedException {
        final float[] pos = new float[2];
        onView(viewMatcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "获取View屏幕中心点";
            }

            @Override
            public void perform(UiController uiController, View view) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                pos[0] = location[0] + view.getWidth() / 2f;
                pos[1] = location[1] + view.getHeight() / 2f;
            }
        });
        return pos;
    }

    /**
     * Matcher：判断该View属于指定Fragment类
     */
    public static Matcher<View> isInFragment(final Class<? extends Fragment> fragmentClass) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                Fragment fragment = findFragmentContainingView(view);
                return fragmentClass.isInstance(fragment);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("view is inside fragment " + fragmentClass.getName());
            }
        };
    }

    /**
     * 根据View查找所属Fragment
     */
    private static Fragment findFragmentContainingView(View view) {
        return FragmentManager.findFragment(view);
    }

    public static Matcher<View> hasAncestor(final Matcher<View> ancestorMatcher) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                while (parent != null) {
                    // 关键：只有parent是View才做匹配；ViewRootImpl直接跳出循环
                    if (parent instanceof View) {
                        View parentView = (View) parent;
                        if (ancestorMatcher.matches(parentView)) {
                            return true;
                        }
                    } else {
                        // 遇到ViewRootImpl，到达视图树顶端，终止遍历
                        break;
                    }
                    parent = parent.getParent();
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has ancestor matches ");
                ancestorMatcher.describeTo(description);
            }
        };
    }


}
