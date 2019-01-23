package com.zhongjh.albumcamerarecorder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * 分别显示相册、录像、录音
 * Created by zhongjh on 2018/8/22.
 */
public class FragmentAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments;

    public FragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {//必须实现
        return mFragments.size();
    }



}
