package com.zhongjh.albumcamerarecorder;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zhongjh.albumcamerarecorder.album.MatissFragment;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.camera.CameraFragment;
import com.zhongjh.albumcamerarecorder.recorder.SoundRecordingFragment;
import com.zhongjh.albumcamerarecorder.widget.NoScrollViewPager;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by zhongjh on 2018/8/22.
 */
public class MainActivity extends AppCompatActivity {

    private FragmentPagerAdapter adapterViewPager;

    // 底部控件
    private TabLayout mTabLayout;
    // viewPager
    private NoScrollViewPager mVpPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalSpec mSpec = GlobalSpec.getInstance();
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
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), mSpec);
        mVpPager.setAdapter(adapterViewPager);
        mVpPager.setOffscreenPageLimit(3);

        // 底部
        mTabLayout = findViewById(R.id.tableLayout);
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
     *
     * @param isTouch 是否拦截
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setTablayoutTouch(boolean isTouch) {
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            View view = getTabView(mTabLayout, i);
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


    public class MyPagerAdapter extends FragmentPagerAdapter {

        private int numItems;// 数量

        ArrayList<String> mTitles = new ArrayList<>(); // 标题

        public MyPagerAdapter(FragmentManager fragmentManager, GlobalSpec mSpec) {
            super(fragmentManager);

            // 根据相关配置做相应的初始化
            if (mSpec.albumSetting != null) {
                numItems++;
                mTitles.add("相册");
            }
            if (mSpec.cameraSetting != null) {
                numItems++;
                mTitles.add("拍照");
            }
            if (mSpec.recorderSetting != null) {
                if (mSpec.maxAudioSelectable > 0) {
                    numItems++;
                    mTitles.add("录音");
                }
            }
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return numItems;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (mTitles.get(position)) {
                case "相册":
                    return MatissFragment.newInstance();
                case "拍照":
                    return CameraFragment.newInstance();
                case "录音":
                    return SoundRecordingFragment.newInstance();
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles.get(position);
        }

    }

}
