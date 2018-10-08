package com.zhongjh.cameraviewsoundrecorder;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.album.MatissFragment;
import com.zhongjh.cameraviewsoundrecorder.album.entity.SelectionSpec;
import com.zhongjh.cameraviewsoundrecorder.camera.CameraFragment;
import com.zhongjh.cameraviewsoundrecorder.soundrecording.SoundRecordingFragment;

/**
 * Created by zhongjh on 2018/8/22.
 */
public class MainActivity extends AppCompatActivity {

    private SelectionSpec mSpec;
    FragmentPagerAdapter adapterViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSpec = SelectionSpec.getInstance();
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
        ViewPager vpPager = findViewById(R.id.viewPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setOffscreenPageLimit(3);

        // 底部
        TabLayout tabLayout = findViewById(R.id.tableLayout);
//        tabLayout.addTab(tabLayout.newTab().setText("相册"));
//        tabLayout.addTab(tabLayout.newTab().setText("拍照"));
//        tabLayout.addTab(tabLayout.newTab().setText("录音"));
        tabLayout.setupWithViewPager(vpPager);
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
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return CameraFragment.newInstance(1, "拍照");
                case 2: // Fragment # 1 - This will show SecondFragment
                    return SoundRecordingFragment.newInstance(2, "录音");
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
