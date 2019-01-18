package com.zhongjh.cameraviewsoundrecorder;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zhongjh.cameraviewsoundrecorder.album.MatissFragment;
import com.zhongjh.cameraviewsoundrecorder.settings.GlobalSpec;
import com.zhongjh.cameraviewsoundrecorder.camera.CameraFragment;
import com.zhongjh.cameraviewsoundrecorder.recorder.SoundRecordingFragment;
import com.zhongjh.cameraviewsoundrecorder.widget.NoScrollViewPager;

import java.lang.reflect.Field;

import static com.zhongjh.cameraviewsoundrecorder.album.model.SelectedItemCollection.COLLECTION_IMAGE;

/**
 * Created by zhongjh on 2018/8/22.
 */
public class MainActivity extends AppCompatActivity {

    private GlobalSpec mSpec;
    FragmentPagerAdapter adapterViewPager;

    // 底部控件
    TabLayout mTabLayout;
    // viewPager
    NoScrollViewPager mVpPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSpec = GlobalSpec.getInstance();
        setTheme(mSpec.themeId);
        super.onCreate(savedInstanceState);
        // @@确认是否进行了配置
        if (!mSpec.hasInited) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        setContentView(R.layout.activity_main_zjh);
        if (mSpec.needOrientationRestriction()) {
            setRequestedOrientation(mSpec.orientation);
        }
        mVpPager = findViewById(R.id.viewPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        mVpPager.setAdapter(adapterViewPager);
        mVpPager.setOffscreenPageLimit(3);

        // 底部
        mTabLayout = findViewById(R.id.tableLayout);
//        tabLayout.addTab(tabLayout.newTab().setText("相册"));
//        tabLayout.addTab(tabLayout.newTab().setText("拍照"));
//        tabLayout.addTab(tabLayout.newTab().setText("录音"));
        mTabLayout.setupWithViewPager(mVpPager);
    }

    /**
     * 显示或者隐藏底部
     *
     * @param isShow 是否显示
     */
    public void showHideTableLayout(boolean isShow) {
        if (isShow) {
            mTabLayout.setVisibility(View.VISIBLE);
            setTablayoutScroll(true);
        } else {
            mTabLayout.setVisibility(View.GONE);
            setTablayoutScroll(false);
        }
    }

    /**
     * 设置tablayout是否可以滑动
     *
     * @param isScroll 是否滑动
     */
    public void setTablayoutScroll(boolean isScroll) {
        if (isScroll) {
            // 设置可以滑动
            mVpPager.setScroll(true);
            mTabLayout.setVisibility(View.VISIBLE);
//            setTablayoutTouch(false);
        } else {
            // 禁滑viewPager
            mVpPager.setScroll(false);
            mTabLayout.setVisibility(View.GONE);
//            setTablayoutTouch(true);
        }
    }

    /**
     * 设置是否拦截
     * @param isTouch 是否拦截
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setTablayoutTouch(boolean isTouch){
        for (int i=0;i<mTabLayout.getTabCount();i++) {
            View view = getTabView(mTabLayout,i);
            if (view == null) continue;
            view.setTag(i);
            view.setOnTouchListener((v, event) -> isTouch);
        }
    }

    /**
     * 反射获取tabview
     *
     * @param tabLayout tabLayout
     * @param index     索引
     * @return view
     */
    private View getTabView(TabLayout tabLayout, int index) {
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        if (tab == null) return null;
        View tabView = null;
        Field view = null;
        try {
            view = TabLayout.Tab.class.getDeclaredField("mView");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        if (view != null) {
            view.setAccessible(true);
        }
        try {
            if (view != null) {
                tabView = (View) view.get(tab);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return tabView;
    }


    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        String[] mTitles = new String[]{"相册", "拍照", "录音"};

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MatissFragment.newInstance(0, "相册");
//                    return CameraFragment.newInstance(1, "Page # 2");
                case 1: // Fragment # 0 - This will show FirstFragment different title // 看看这个怎样把类型传过去，colltype
                    return CameraFragment.newInstance(1, "拍照",COLLECTION_IMAGE);
                case 2: // Fragment # 1 - This will show SecondFragment
                    return SoundRecordingFragment.newInstance();
//                    return CameraFragment.newInstance(1, "Page # 2");
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

    }

}
