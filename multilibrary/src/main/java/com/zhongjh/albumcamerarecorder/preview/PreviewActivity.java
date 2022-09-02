package com.zhongjh.albumcamerarecorder.preview;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;

/**
 * 一个容器，容纳PreviewFragment,一切都是为了过渡动画
 *
 * @author zhongjh
 * @date 2022/8/29
 */
public class PreviewActivity extends AppCompatActivity {

    protected GlobalSpec mGlobalSpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mGlobalSpec = GlobalSpec.INSTANCE;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_containerview_zjh);
        initFragment();
    }

    @Override
    public void finish() {
        super.finish();
        //关闭窗体动画显示
        if (mGlobalSpec.getCutscenesEnabled()) {
            this.overridePendingTransition(0, R.anim.activity_close_zjh);
        }
    }

    /**
     * 实例化Fragment,根据不同的类型实例化不同的Fragment
     */
    private void initFragment() {
        Fragment fragment = new PreviewFragment();
        handleExtra(fragment);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainerView, fragment)
                .commit();
    }

    /**
     * 处理参数
     */
    private void handleExtra(Fragment fragment) {
        Bundle bundle = getIntent().getExtras();
        fragment.setArguments(bundle);
    }

}
