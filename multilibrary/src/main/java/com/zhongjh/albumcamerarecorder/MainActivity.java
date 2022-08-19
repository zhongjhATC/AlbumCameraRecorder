package com.zhongjh.albumcamerarecorder;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.zhongjh.albumcamerarecorder.album.MainFragment;
import com.zhongjh.albumcamerarecorder.camera.CameraFragment;
import com.zhongjh.albumcamerarecorder.recorder.SoundRecordingFragment;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.utils.HandleBackUtil;
import com.zhongjh.albumcamerarecorder.utils.HandleOnKeyUtil;
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils;
import com.zhongjh.common.utils.AppUtils;
import com.zhongjh.common.utils.StatusBarUtils;

import java.util.ArrayList;

/**
 * 包含三大fragment
 *
 * @author zhongjh
 * @date 2018/8/22
 */
public class MainActivity extends AppCompatActivity {

    private MyPagerAdapter adapterViewPager;

    private final static int ALBUM = 0;
    private final static int CAMERA = 1;
    private final static int RECORDER = 2;
    /**
     * 权限申请自定义码
     */
    protected static final int GET_PERMISSION_REQUEST = 100;
    /**
     * 跳转到设置界面
     */
    private static final int REQUEST_CODE_SETTING = 101;

    /**
     * 底部控件
     */
    private TabLayout mTabLayout;
    /**
     * 底部控件用于关联viewPager2的
     */
    private TabLayoutMediator mLayoutMediator;
    /**
     * viewPager
     */
    private ViewPager2 mVpPager;
    /**
     * 默认索引
     */
    private int mDefaultPosition;

    GlobalSpec mSpec = GlobalSpec.INSTANCE;
    /**
     * 是否初始化完毕
     */
    boolean mIsInit;
    /**
     * 是否弹出提示多次拒绝权限的dialog
     */
    private boolean mIsShowDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (mSpec.needOrientationRestriction()) {
            setRequestedOrientation(mSpec.getOrientation());
        }
        setTheme(mSpec.getThemeId());
        StatusBarUtils.initStatusBar(MainActivity.this);
        super.onCreate(savedInstanceState);
        // 确认是否进行了配置
        if (!mSpec.getHasInited()) {
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (HandleOnKeyUtil.handleOnKey(this, keyCode, event)) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (mSpec.getCutscenesEnabled()) {
            //关闭窗体动画显示
            this.overridePendingTransition(0, R.anim.activity_close_zjh);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLayoutMediator != null) {
            mLayoutMediator.detach();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTING) {
            // 因为权限一直拒绝后，只能跑到系统设置界面调整，这个是系统设置界面返回后的回调，重新验证权限
            requestPermissions();
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!mIsShowDialog) {
            // 全部拒绝后就提示去到应用设置里面修改配置
            int permissionsLength = 0;
            for (int i = 0; i < grantResults.length; i++) {
                // 只有当用户同时点选了拒绝开启权限和不再提醒后才会true
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    if (grantResults[i] == PERMISSION_DENIED) {
                        permissionsLength++;
                    }
                }
            }
            // 至少一个不再提醒
            if (permissionsLength > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                builder.setPositiveButton(getString(R.string.z_multi_library_setting), (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    MainActivity.this.startActivityForResult(intent, REQUEST_CODE_SETTING);
                    mIsShowDialog = false;
                });
                builder.setNegativeButton(getString(R.string.z_multi_library_cancel), (dialog, which) -> {
                    dialog.dismiss();
                    MainActivity.this.finish();
                });

                // 获取app名称
                String appName = AppUtils.getAppName(getApplicationContext());
                if (TextUtils.isEmpty(appName)) {
                    builder.setMessage(getString(R.string.permission_has_been_set_and_will_no_longer_be_asked));
                } else {
                    StringBuilder toSettingTipStr = new StringBuilder();
                    toSettingTipStr.append(getString(R.string.z_multi_library_in_settings_apply));
                    toSettingTipStr.append(appName);
                    toSettingTipStr.append(getString(R.string.z_multi_library_enable_storage_and_camera_permissions_for_normal_use_of_related_functions));
                    builder.setMessage(toSettingTipStr.toString());
                }
                builder.setTitle(getString(R.string.z_multi_library_hint));
                builder.setOnDismissListener(dialog12 -> mIsShowDialog = false);
                Dialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnKeyListener((dialog1, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                        finish();
                    }
                    return false;
                });
                dialog.show();
                mIsShowDialog = true;
            }
        }
        if (!mIsShowDialog) {
            if (requestCode == GET_PERMISSION_REQUEST) {
                int permissionsLength = 0;
                for (int grantResult : grantResults) {
                    if (grantResult == PERMISSION_DENIED) {
                        // 如果拒绝后
                        permissionsLength++;
                    }
                }
                if (permissionsLength > 0) {
                    requestPermissionsDialog();
                } else {
                    requestPermissions();
                }
            }
        }
    }

    /**
     * 初始化，在权限全部通过后才进行该初始化
     */
    private void init() {
        if (!mIsInit) {
            mVpPager = findViewById(R.id.viewPager);
            mTabLayout = findViewById(R.id.tableLayout);
            adapterViewPager = new MyPagerAdapter(this, mSpec);
            mVpPager.setAdapter(adapterViewPager);
            mVpPager.setOffscreenPageLimit(3);
            // 根据配置默认选第几个
            mVpPager.setCurrentItem(mDefaultPosition, false);
            // 判断只有一个的时候
            if (adapterViewPager.getItemCount() <= 1) {
                // 则隐藏底部
                mTabLayout.setVisibility(View.GONE);
            } else {
                mTabLayout.setVisibility(View.VISIBLE);
                mLayoutMediator = new TabLayoutMediator(mTabLayout, mVpPager, false, true,
                        (tab, position) -> tab.setText(adapterViewPager.mTitles.get(position)));
                mLayoutMediator.attach();
                // 禁滑viewPager
                mVpPager.setUserInputEnabled(false);
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
            // 请求权限
            requestPermissions2(needPermissions);
        } else {
            // 没有所需要请求的权限，就进行初始化
            init();
        }
    }

    /**
     * 请求权限 - 如果曾经拒绝过，则弹出dialog
     */
    private void requestPermissionsDialog() {
        // 判断权限，权限通过才可以初始化相关
        ArrayList<String> needPermissions = getNeedPermissions();
        if (needPermissions.size() > 0) {
            // 动态消息
            StringBuilder message = new StringBuilder();
            message.append(getString(R.string.z_multi_library_to_use_this_feature));
            for (String item : needPermissions) {
                switch (item) {
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        message.append(getString(R.string.z_multi_library_file_read_and_write_permission_to_read_and_store_related_files));
                        break;
                    case Manifest.permission.RECORD_AUDIO:
                        // 弹窗提示为什么要请求这个权限
                        message.append(getString(R.string.z_multi_library_record_permission_to_record_sound));
                        break;
                    case Manifest.permission.CAMERA:
                        // 弹窗提示为什么要请求这个权限
                        message.append(getString(R.string.z_multi_library_record_permission_to_shoot));
                        break;
                    default:
                        break;
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
            // 弹窗提示为什么要请求这个权限
            builder.setTitle(getString(R.string.z_multi_library_hint));
            message.append(getString(R.string.z_multi_library_Otherwise_it_cannot_run_normally_and_will_apply_for_relevant_permissions_from_you));
            builder.setMessage(message.toString());
            builder.setPositiveButton(getString(R.string.z_multi_library_ok), (dialog, which) -> {
                dialog.dismiss();
                // 请求权限
                requestPermissions2(needPermissions);
            });
            builder.setNegativeButton(getString(R.string.z_multi_library_cancel), (dialog, which) -> {
                dialog.dismiss();
                MainActivity.this.finish();
            });
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener((dialog1, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    finish();
                }
                return false;
            });
            dialog.show();
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
            if (SelectableUtils.recorderValid() || SelectableUtils.videoValid()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager
                        .PERMISSION_GRANTED) {
                    if (!permissions.contains(Manifest.permission.RECORD_AUDIO)) {
                        permissions.add(Manifest.permission.RECORD_AUDIO);
                    }
                }
            }
            // 判断如果有录制功能则验证录音、录制
            if (SelectableUtils.cameraValid()) {
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
     * @param permissions 权限
     */
    private void requestPermissions2(ArrayList<String> permissions) {
        ActivityCompat.requestPermissions(MainActivity.this, permissions.toArray(new String[0]), GET_PERMISSION_REQUEST);
    }

    /**
     * 显示或者隐藏底部
     *
     * @param isShow 是否显示
     */
    public void showHideTableLayout(boolean isShow) {
        // 判断只有一个的时候
        if (adapterViewPager.getItemCount() <= 1) {
            // 则隐藏底部
            mTabLayout.setVisibility(View.GONE);
        } else {
            if (isShow) {
                mTabLayout.setVisibility(View.VISIBLE);
            } else {
                mTabLayout.setVisibility(View.GONE);
            }
        }
    }

    public void onDependentViewChanged(float translationY) {
        mTabLayout.setTranslationY(Math.abs(translationY));
    }

    public class MyPagerAdapter extends FragmentStateAdapter {

        /**
         * 数量
         */
        int numItems;

        /**
         * 标题
         */
        public ArrayList<String> mTitles = new ArrayList<>();

        public MyPagerAdapter(FragmentActivity fa, GlobalSpec mSpec) {
            super(fa);

            // 默认选择谁的类型
            int defaultPositionType = ALBUM;

            if (mSpec.getDefaultPosition() == RECORDER) {
                // 默认语音
                defaultPositionType = RECORDER;
            } else if (mSpec.getDefaultPosition() == CAMERA) {
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

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (mTitles.get(position).equals(getString(R.string.z_multi_library_album))) {
                if (numItems <= 1) {
                    return MainFragment.Companion.newInstance(0);
                }
                return MainFragment.Companion.newInstance(50);
            } else if (mTitles.get(position).equals(getString(R.string.z_multi_library_sound_recording))) {
                return SoundRecordingFragment.newInstance();
            } else {
                return CameraFragment.newInstance();
            }
        }

        @Override
        public int getItemCount() {
            return numItems;
        }
    }

}
