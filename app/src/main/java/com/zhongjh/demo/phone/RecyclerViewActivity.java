package com.zhongjh.demo.phone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.demo.R;
import com.zhongjh.demo.databinding.ActivityMainUpperLimitBinding;
import com.zhongjh.demo.databinding.ActivityRecyclerviewBinding;
import com.zhongjh.demo.phone.adapter.RecyclerAdapter;

/**
 * TODO 该方面功能仍在完善中
 * 演示RecyclerView下操作
 *
 * @author zhongjh
 * @date 2021/8/6
 */
public class RecyclerViewActivity extends AppCompatActivity {

    private final String TAG = RecyclerViewActivity.this.getClass().getSimpleName();

    ActivityRecyclerviewBinding mBinding;

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, RecyclerViewActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityRecyclerviewBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        initRecyclerView();
    }

    /**
     * 初始化RecyclerView以及数据
     */
    private void initRecyclerView() {
        // 创建线性布局
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // 垂直方向
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        // 给RecyclerView设置布局管理器
        mBinding.recyclerview.setLayoutManager(layoutManager);
        // 创建适配器，并且设置
        RecyclerAdapter adapter = new RecyclerAdapter(this);
        mBinding.recyclerview.setAdapter(adapter);
    }

}
