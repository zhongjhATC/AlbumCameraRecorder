package com.zhongjh.multimedia.camera.entity

import com.flyco.tablayout.listener.CustomTabEntity

class TabEntity(private val title: String, private val selectedIcon: Int, private val unSelectedIcon: Int) : CustomTabEntity {
    override fun getTabTitle(): String {
        return title
    }

    override fun getTabSelectedIcon(): Int {
        return selectedIcon
    }

    override fun getTabUnselectedIcon(): Int {
        return unSelectedIcon
    }
}
