package com.zhongjh.albumcamerarecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
     * 权限申请自定义码
     */
    protected final int GET_PERMISSION_REQUEST = 100;

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
    /**
     * 是否初始化完毕
     */
    boolean mIsInit;

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
        requestPermissions();
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
        if (mSpec.isCutscenes) {
            //关闭窗体动画显示
            this.overridePendingTransition(0, R.anim.activity_close);
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_PERMISSION_REQUEST) {
            requestPermissions();
        }
    }

    /**
     * 初始化，在权限全部通过后才进行该初始化
     */
    private void init() {
        if (!mIsInit) {
            mVpPager = findViewById(R.id.viewPager);
            mTabLayout = findViewById(R.id.tableLayout);
            adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, mSpec);
            mVpPager.setAdapter(adapterViewPager);
            mVpPager.setOffscreenPageLimit(3);
            // 根据配置默认选第几个
            mVpPager.setCurrentItem(mDefaultPosition);
            // 判断只有一个的时候
            if (adapterViewPager.getCount() <= 1) {
                // 则隐藏底部
                mTabLayout.setVisibility(View.GONE);
            } else {
                mTabLayout.setVisibility(View.VISIBLE);
                mTabLayout.setupWithViewPager(mVpPager);
            }
            mIsInit = true;
        }
    }

    /**
     * 请求权限
     */
    private void requestPermissions() {
        // 判断权限，权限通过才可以初始化相关
        ArrayList<String> needPermissions = getNeedPermissions();
        if (needPermissions.size() > 0) {
            for (String item : needPermissions) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                switch (item) {
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        // 弹窗提示为什么要请求这个权限
                        builder.setTitle("提示");
                        builder.setMessage("使用该功能需要用到文件读写权限，否则无法正常运行，接下来会向您申请读写设备上的照片及文件权限");
                        builder.setPositiveButton("好的", (dialog, which) -> {
                            dialog.dismiss();
                            // 请求权限
                            requestPermissions2(item);
                        });
                        builder.setNegativeButton("取消", (dialog, which) -> {
                            dialog.dismiss();
                            MainActivity.this.finish();
                        });
                        builder.create().show();
                        return;
                    case Manifest.permission.RECORD_AUDIO:
                        // 弹窗提示为什么要请求这个权限
                        builder.setTitle("提示");
                        builder.setMessage("使用该功能需要用到录音权限，否则无法正常运行，接下来会向您申请本地录音相关权限");
                        builder.setPositiveButton("好的", (dialog, which) -> {
                            dialog.dismiss();
                            // 请求权限
                            requestPermissions2(item);
                        });
                        builder.setNegativeButton("取消", (dialog, which) -> {
                            dialog.dismiss();
                            MainActivity.this.finish();
                        });
                        builder.create().show();
                        return;
                    case Manifest.permission.CAMERA:
                        // 弹窗提示为什么要请求这个权限
                        builder.setTitle("提示");
                        builder.setMessage("使用该功能需要用到录制权限，否则无法正常运行，接下来会向您申请拍摄照片和录制视频相关权限");
                        builder.setPositiveButton("好的", (dialog, which) -> {
                            dialog.dismiss();
                            // 请求权限
                            requestPermissions2(item);
                        });
                        builder.setNegativeButton("取消", (dialog, which) -> {
                            dialog.dismiss();
                            MainActivity.this.finish();
                        });
                        builder.create().show();
                        return;
                    default:
                        return;
                }
            }
        } else {
            // 没有所需要请求的权限，就进行初始化
            init();
        }
    }

    /**
     * 获取目前需要请求的权限
     */
    protected ArrayList<String> getNeedPermissions() {
        // 需要请求的权限列表
        ArrayList<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 存储功能必须验证
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            // 判断如果有录音功能则验证录音
            if (SelectableUtils.recorderValid()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager
                        .PERMISSION_GRANTED) {
                    if (!permissions.contains(Manifest.permission.RECORD_AUDIO)) {
                        permissions.add(Manifest.permission.RECORD_AUDIO);
                    }
                }
            }
            // 判断如果有录制功能则验证录音、录制
            if (SelectableUtils.cameraValid()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager
                        .PERMISSION_GRANTED) {
                    if (!permissions.contains(Manifest.permission.RECORD_AUDIO)) {
                        permissions.add(Manifest.permission.RECORD_AUDIO);
                    }
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager
                        .PERMISSION_GRANTED) {
                    if (!permissions.contains(Manifest.permission.CAMERA)) {
                        permissions.add(Manifest.permission.CAMERA);
                    }
                }
            }
        }
        return permissions;
    }

    /**
     * 请求权限
     *
     * @param permission 权限
     */
    private void requestPermissions2(String permission) {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, GET_PERMISSION_REQUEST);
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

            // 根据相关配置做相应的初始化，相册生效
            if (SelectableUtils.albumValid()) {
                numItems++;
                mTitles.add(getString(R.string.z_multi_library_album));
            }
            // 相机生效
            if (SelectableUtils.cameraValid()) {
                if (defaultPositionType == CAMERA) {
                    mDefaultPosition = numItems;
                }
                numItems++;
                mTitles.add(getString(R.string.z_multi_library_take_photos));
            }
            // 录音生效
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
