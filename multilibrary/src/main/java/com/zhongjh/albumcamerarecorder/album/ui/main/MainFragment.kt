package com.zhongjh.albumcamerarecorder.album.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zhongjh.albumcamerarecorder.BaseFragment
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.album.ui.MatissFragment

/**
 * 一个容器，容纳 MatissFragment和别的例如 PreviewFragment,一切都是为了过渡动画
 * @author zhongjh
 * @date 2022/7/26
 */
class MainFragment : BaseFragment() {

    companion object {
        /**
         * @param marginBottom 底部间距
         */
        fun newInstance(marginBottom: Int): MainFragment {
            val mainFragment = MainFragment()
            val args = Bundle()
            mainFragment.arguments = args
            args.putInt(MatissFragment.ARGUMENTS_MARGIN_BOTTOM, marginBottom)
            return mainFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_containerview_zjh, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val matissFragment = MatissFragment.newInstance(
            arguments?.getInt(MatissFragment.ARGUMENTS_MARGIN_BOTTOM)
                ?: 0
        )
        childFragmentManager
            .beginTransaction()
            .add(R.id.fragmentContainerView, matissFragment)
            .commit()
    }

}