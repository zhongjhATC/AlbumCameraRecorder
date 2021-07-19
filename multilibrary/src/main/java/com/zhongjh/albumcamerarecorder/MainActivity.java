package com.zhongjh.albumcamerarecorder;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.tabs.TabLayout;
import com.zhongjh.albumcamerarecorder.album.MatissFragment;
import com.zhongjh.albumcamerarecorder.camera.CameraFragment;
import com.zhongjh.albumcamerarecorder.recorder.SoundRecordingFragment;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.utils.HandleBackUtil;
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils;
import com.zhongjh.albumcamerarecorder.widget.NoScrollViewPager;

import java.util.ArrayList;

import gaode.zhongjh.com.common.utils.StatusBarUtils;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

/**
 * 包含三大fragment
 *
 * @author zhongjh
 * @date 2018/8/22
 */
public class MainActivity extends AppCompatActivity {

    private FragmentPagerAdapter adapterViewPager;

    private final static int ALBUM = 0;
    private final static int CAMERA = 1;
    private final static int RECORDER = 2;

    /**
     * 底部控件
     */
    private TabLayout mTabLayout;
    /**
     * viewPager
     */
    private NoScrollViewPager mVpPager;
    /**
     * 默认索引
     */
    private int mDefaultPosition;

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
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, mSpec);
        mVpPager.setAdapter(adapterViewPager);
        mVpPager.setOffscreenPageLimit(3);
        // 根据配置默认选第几个
        mVpPager.setCurrentItem(mDefaultPosition);
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
        {
            this.overridePendingTransition(0, R.anim.activity_close);
        }
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
                setTabLayoutScroll(true);
            } else {
                mTabLayout.setVisibility(View.GONE);
                setTabLayoutScroll(false);
            }
        }
    }

    /**
     * 设置tabLayout是否可以滑动
     *
     * @param isScroll 是否滑动
     */
    public void setTabLayoutScroll(boolean isScroll) {
        // 判断只有一个的时候
        if (adapterViewPager.getCount() <= 1) {
            // 则隐藏底部
            mTabLayout.setVisibility(View.GONE);
        } else {
            if (isScroll) {
                // 设置可以滑动
                mVpPager.setScroll();
                mTabLayout.setVisibility(View.VISIBLE);
            } else {
                // 禁滑viewPager
                mVpPager.setScroll();
                mTabLayout.setVisibility(View.GONE);
            }
        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        int numItems;// 数量

        ArrayList<String> mTitles = new ArrayList<>(); // 标题

        public MyPagerAdapter(@NonNull FragmentManager fm, int behavior, GlobalSpec mSpec) {
            super(fm, behavior);

            int defaultPositionType = ALBUM;// 默认选择谁的类型

            if (mSpec.defaultPosition == RECORDER) {
                // 默认语音
                defaultPositionType = RECORDER;
            } else if (mSpec.defaultPosition == CAMERA) {
                // 默认录制
                defaultPositionType = CAMERA;
            }

            // 根据相关配置做相应的初始化
            if (SelectableUtils.albumValid()) {
                numItems++;
                mTitles.add(getString(R.string.z_multi_library_album));
            }
            if (SelectableUtils.cameraValid()) {
                if (defaultPositionType == CAMERA) {
                    mDefaultPosition = numItems;
                }
                numItems++;
                mTitles.add(getString(R.string.z_multi_library_take_photos));
            }
            if (SelectableUtils.recorderValid()) {
                if (defaultPositionType == RECORDER) {
                    mDefaultPosition = numItems;
                }
                numItems++;
                mTitles.add(getString(R.string.z_multi_library_sound_recording));
            }

        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return numItems;
        }

        // Returns the fragment to display for that page
        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (mTitles.get(position).equals(getString(R.string.z_multi_library_album))) {
                if (adapterViewPager.getCount() <= 1) {
                    return MatissFragment.newInstance(0);
                }
                return MatissFragment.newInstance(50);
            } else if (mTitles.get(position).equals(getString(R.string.z_multi_library_sound_recording))) {
                return SoundRecordingFragment.newInstance();
            } else {
                return CameraFragment.newInstance();
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles.get(position);
        }

    }

}
