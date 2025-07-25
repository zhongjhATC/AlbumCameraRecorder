package com.zhongjh.multimedia.preview

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.settings.GlobalSpec

/**
 * 一个容器，容纳PreviewFragment,一切都是为了过渡动画
 *
 * @author zhongjh
 * @date 2022/8/29
 */
class PreviewActivity : AppCompatActivity() {
    private var mGlobalSpec = GlobalSpec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_containerview_zjh)
        initFragment()
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@PreviewActivity.finish()
            }
        })
    }

    override fun finish() {
        super.finish()
        // 关闭窗体动画显示
        if (mGlobalSpec.cutscenesEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                this.overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, R.anim.activity_close_zjh)
            } else {
                this.overridePendingTransition(0, R.anim.activity_close_zjh)
            }
        }
    }

    /**
     * 实例化Fragment,根据不同的类型实例化不同的Fragment
     */
    private fun initFragment() {
        val fragment: Fragment = PreviewFragment()
        handleExtra(fragment)
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragmentContainerView, fragment)
            .commit()
    }

    /**
     * 处理参数
     * 上一个Activity传递的参数传给fragment
     */
    private fun handleExtra(fragment: Fragment) {
        val bundle = intent.extras
        fragment.arguments = bundle
    }
}
