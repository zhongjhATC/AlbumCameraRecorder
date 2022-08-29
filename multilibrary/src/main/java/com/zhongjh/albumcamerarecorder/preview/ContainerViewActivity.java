package com.zhongjh.albumcamerarecorder.preview;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zhongjh.albumcamerarecorder.R;

/**
 * 一个容器，容纳PreviewFragment,一切都是为了过渡动画
 *
 * @author zhongjh
 * @date 2022/8/29
 */
public class ContainerViewActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_containerview_zjh);
    }
}
