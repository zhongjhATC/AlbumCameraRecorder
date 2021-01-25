package com.zhongjh.albumcamerarecorder;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.tabs.TabLayout;
import com.zhongjh.albumcamerarecorder.album.MatissFragment;
import com.zhongjh.albumcamerarecorder.camera.CameraFragment;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.recorder.SoundRecordingFragment;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.utils.HandleBackUtil;
import com.zhongjh.albumcamerarecorder.utils.StatusBarUtils;
import com.zhongjh.albumcamerarecorder.widget.NoScrollViewPager;

import java.util.ArrayList;

/**
 * 包含三大fragment
 * Created by zhongjh on 2018/8/22.
 */
public class MainActivity extends AppCompatActivity {

    private FragmentPagerAdapter adapterViewPager;

    // 底部控件
    private TabLayout mTabLayout;
    // viewPager
    private NoScrollViewPager mVpPager;
    private int mDefaultPosition;// 默认索引

    GlobalSpec mSpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSpec = GlobalSpec.getInstance();
        setTheme(mSpec.themeId);

        StatusBarUtils.initStatusBar(MainActivity.this);

        super.onCreate(savedInstanceState);
        // @@确认是否进行了配置
        if (!mSpec.hasInited) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        setContentView(R.layout.activity_main_zjh);

        mVpPager = findViewById(R.id.viewPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), mSpec);
        mVpPager.setAdapter(adapterViewPager);
        mVpPager.setOffscreenPageLimit(3);
        mVpPager.setCurrentItem(mDefaultPosition); // 根据配置默认选第几个
        // 底部
        mTabLayout = findViewById(R.id.tableLayout);
        // 判断只有一个的时候
        if (adapterViewPager.getCount() <= 1) {
            // 则隐藏底部
            mTabLayout.setVisibility(View.GONE);
        } else {
            mTabLayout.setVisibility(View.VISIBLE);
            mTabLayout.setupWithViewPager(mVpPager);
        }
    }

    @Override
    public void onBackPressed() {
        if (!HandleBackUtil.handleBackPress(this)) {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (mSpec.isCutscenes)
            //关闭窗体动画显示
            this.overridePendingTransition(0, R.anim.activity_close);
    }

    /**
     * 显示或者隐藏底部
     *
     * @param isShow 是否显示
     */
    public void showHideTableLayout(boolean isShow) {
        // 判断只有一个的时候
        if (adapterViewPager.getCount() <= 1) {
            // 则隐藏底部
            mTabLayout.setVisibility(View.GONE);
        } else {
            if (isShow) {
                mTabLayout.setVisibility(View.VISIBLE);
                setTablayoutScroll(true);
            } else {
                mTabLayout.setVisibility(View.GONE);
                setTablayoutScroll(false);
            }
        }
    }

    /**
     * 设置tablayout是否可以滑动
     *
     * @param isScroll 是否滑动
     */
    public void setTablayoutScroll(boolean isScroll) {
        // 判断只有一个的时候
        if (adapterViewPager.getCount() <= 1) {
            // 则隐藏底部
            mTabLayout.setVisibility(View.GONE);
        } else {
            if (isScroll) {
                // 设置可以滑动
                mVpPager.setScroll(true);
                mTabLayout.setVisibility(View.VISIBLE);
            } else {
                // 禁滑viewPager
                mVpPager.setScroll(false);
                mTabLayout.setVisibility(View.GONE);
            }
        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        int numItems;// 数量

        ArrayList<String> mTitles = new ArrayList<>(); // 标题

        public MyPagerAdapter(FragmentManager fragmentManager, GlobalSpec mSpec) {
            super(fragmentManager);

            int defaultPositionType = 0;// 默认选择谁的类型

            if (mSpec.defaultPosition == 2) {
                // 默认语音
                defaultPositionType = 2;
            } else if (mSpec.defaultPosition == 1) {
                // 默认录制
                defaultPositionType = 1;
            }

            // 根据相关配置做相应的初始化
            if (mSpec.albumSetting != null) {
                if (mSpec.maxImageSelectable > 0 || mSpec.maxVideoSelectable > 0) {
                    numItems++;
                    mTitles.add("相册");

                }
            }
            if (mSpec.cameraSetting != null) {
                if (mSpec.maxImageSelectable > 0 || mSpec.maxVideoSelectable > 0) {
                    if (defaultPositionType == 1) {
                        mDefaultPosition = numItems;
                    }
                    numItems++;
                    mTitles.add("拍照");
                }
            }
            if (mSpec.recorderSetting != null) {
                if (mSpec.maxAudioSelectable > 0) {
                    if (defaultPositionType == 2) {
                        mDefaultPosition = numItems;
                    }
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
                    if (adapterViewPager.getCount() <= 1) {
                        return MatissFragment.newInstance(0);
                    }
                    return MatissFragment.newInstance(50);
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
