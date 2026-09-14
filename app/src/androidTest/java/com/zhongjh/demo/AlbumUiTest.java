package com.zhongjh.demo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.zhongjh.demo.phone.MainListActivity;
import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment;
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
    public void beforeTest() {// 获取当前被测App包名
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

        // 点击GridView第0项（第一个格子）
        clickGridViewItem(0);

        // 通过所有权限
        passAllPermissions();

        // 等待相册页面加载出来，给页面渲染时间
        Thread.sleep(2000);

        // 返回
        uiDevice.pressBack();

        // 勾选去掉相册功能
        onView(withId(R.id.cbAlbum))
                .check(matches(isChecked()))
                .perform(click());

        // 重新进去
        clickGridViewItem(0);

        // 拍满照片
        for (int i = 0; i < 5; i++) {
            takePhoto();
        }

        // 然后删光照片
        for (int i = 4; i >= 0; i--) {
            clickGridViewItemByDelete();
        }

        // 接着录像
        recordVideo();

        // 再录像
        // 满了后然后返回
        // 再录像一个点击确认

        // 授权完成，弹窗关闭,查看界面是否正常
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
    private void clickGridViewItemByDelete() {
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
    private void recordVideo() {
//        Matcher<View> pvLayoutMatcher = allOf(
//                withId(R.id.pvLayout),
//                isInFragment(BaseCameraFragment.class),
//                isDisplayed()
//        );
//
//        // 长按触发录像，保持录像3秒
//        onView(allOf(
//                isAssignableFrom(ClickOrLongButton.class),
//                hasAncestor(pvLayoutMatcher),
//                isDisplayed()
//        )).perform(longPressInstrumentation(3500));

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

        // ========== 1. 先通过Espresso找到按钮，设置最大录制时长15s，并且拿到按钮屏幕坐标 ==========
        final float[] touchPos = new float[2];
        onView(btnMatcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isEnabled();
            }

            @Override
            public String getDescription() {
                return "设置按钮录制上限并获取触摸坐标";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ClickOrLongButton btn = (ClickOrLongButton) view;
                // 设置最大录制时长15秒，大于长按总时长6500ms，命中onLongClickEnd
                btn.setDuration(15000);

                // 获取控件屏幕中心点
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                touchPos[0] = location[0] + view.getWidth() / 2f;
                touchPos[1] = location[1] + view.getHeight() / 2f;
            }
        });

        // ========== 2. Instrumentation 系统触摸注入（当前运行在Instrumentation测试线程，不会报主线程异常） ==========
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        float x = touchPos[0];
        float y = touchPos[1];
        long downTime = SystemClock.uptimeMillis();
        final long holdMs = 6500;
        long endTime = downTime + holdMs;

        // ACTION_DOWN 按下
        instrumentation.sendPointerSync(MotionEvent.obtain(
                downTime,
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                x, y, 0
        ));

        // 循环发送微小MOVE事件，维持触摸会话，驱动环形进度动画
        while (SystemClock.uptimeMillis() < endTime) {
            long currentTs = SystemClock.uptimeMillis();
            instrumentation.sendPointerSync(MotionEvent.obtain(
                    downTime,
                    currentTs,
                    MotionEvent.ACTION_MOVE,
                    x + 0.02f,
                    y + 0.02f,
                    0
            ));
            // 测试线程休眠50ms，APP主线程持续正常运行
            SystemClock.sleep(50);
        }

        // ACTION_UP 抬起手指
        instrumentation.sendPointerSync(MotionEvent.obtain(
                downTime,
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                x, y, 0
        ));

        // 等待录像保存
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
