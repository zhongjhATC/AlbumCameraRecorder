package com.zhongjh.albumcamerarecorder.preview;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;

/**
 * 一个容器，容纳PreviewFragment,一切都是为了过渡动画
 *
 * @author zhongjh
 * @date 2022/8/29
 */
public class ContainerViewActivity extends AppCompatActivity {

    protected GlobalSpec mGlobalSpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mGlobalSpec = GlobalSpec.INSTANCE;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_containerview_zjh);
    }

    @Override
    public void finish() {
        super.finish();
        //关闭窗体动画显示
        if (mGlobalSpec.getCutscenesEnabled()) {
            this.overridePendingTransition(0, R.anim.activity_close_zjh);
        }
    }

}
