package com.zhongjh.demo.phone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.zhongjh.multimedia.settings.GlobalSetting;
import com.zhongjh.multimedia.settings.MultiMediaSetting;
import com.zhongjh.common.entity.GridMedia;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.demo.R;
import com.zhongjh.demo.configuration.Glide4Engine;
import com.zhongjh.demo.databinding.ActivityMainListBinding;
import com.zhongjh.demo.phone.custom.MainCustomCameraLayoutActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * list配置
 *
 * @author zhongjh
 * @date 2019/4/25
 */
public class MainListActivity extends AppCompatActivity {

    ActivityMainListBinding mBinding;

    /**
     * 独立预览的回调
     */
    protected final ActivityResultLauncher<Intent> requestLauncherPreview = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainListBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // 简单版
        mBinding.btnSimple.setOnClickListener(v -> MainSimpleActivity.newInstance(MainListActivity.this));

        mBinding.btnSuperSimple.setOnClickListener(v -> MainSuperSimpleActivity.newInstance(MainListActivity.this));

        // 配置版
        mBinding.btnConfigure.setOnClickListener(v -> MainActivity.newInstance(MainListActivity.this));

        // 多种样式版
        mBinding.btnTheme.setOnClickListener(v -> MainThemeActivity.newInstance(MainListActivity.this));

        // 默认有数据的
        mBinding.btnOpenSee.setOnClickListener(v -> MainSeeActivity.newInstance(MainListActivity.this));

        // 默认有数据的 - 本地
        mBinding.btnOpenSeeLocal.setOnClickListener(v -> MainSeeLocalActivity.newInstance(MainListActivity.this));

        // 独立预览相片功能
        mBinding.btnPreview.setOnClickListener(v -> {
            // 提前设置GlobalSetting的imageEngine,也可以在application设置
            GlobalSetting globalSetting = MultiMediaSetting.from(MainListActivity.this).choose(MimeType.ofAll());
            globalSetting.imageEngine(new Glide4Engine());
            ArrayList<Integer> list = new ArrayList<>();
            list.add(R.drawable.ic_failed);
            list.add(R.drawable.ic_loading);
            ArrayList<GridMedia> listNew = new ArrayList<>();
            for (Integer id : list) {
                copyFilesFromRaw(getApplicationContext(), id, id.toString(), getApplicationContext().getFilesDir().getAbsolutePath() + "/resource");
                GridMedia gridMedia = new GridMedia();
                gridMedia.setAbsolutePath(getApplicationContext().getFilesDir().getAbsolutePath() + "/resource/" + id);
                gridMedia.setPath(Uri.fromFile(new File(gridMedia.getAbsolutePath())).toString());
                listNew.add(gridMedia);
            }
            globalSetting.openPreviewData(MainListActivity.this, requestLauncherPreview, listNew, 0, false);
        });

        // 这是灵活配置能选择xx张图片,xx个视频，xx个音频的用法示例
        mBinding.btnUpperLimit.setOnClickListener(v -> MainUpperLimitActivity.newInstance(MainListActivity.this));

        // recyclerView版
        mBinding.btnRecyclerView.setOnClickListener(v -> RecyclerViewActivity.newInstance(MainListActivity.this));

        // 自定义CameraView
        mBinding.btnCustomCameraView.setOnClickListener(v -> MainCustomCameraViewActivity.newInstance(MainListActivity.this));

        // 自定义CameraLayout
        mBinding.btnCustomCameraLayout.setOnClickListener(v -> MainCustomCameraLayoutActivity.newInstance(MainListActivity.this));
    }

    // 路径分隔符
    private static final String SEPARATOR = File.separator;

    /**
     * 复制res/raw中的文件到指定目录
     *
     * @param context     上下文
     * @param id          资源ID
     * @param fileName    文件名
     * @param storagePath 目标文件夹的路径
     */

    public static void copyFilesFromRaw(Context context, int id, String fileName, String storagePath) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
        File file = new File(storagePath, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            Log.e("MainListActivity", "copyFilesFromRaw" + e.getMessage());
        }
        bitmap.recycle();
    }

}
