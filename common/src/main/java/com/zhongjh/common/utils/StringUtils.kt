package com.zhongjh.common.utils

import java.io.File

/**
 * @author Blankj
 * blog  : http://blankj.com
 * time  : 2016/08/16
 * desc  : utils about string
 */
object StringUtils {
    /**
     * Return whether the string is null or white space.
     *
     * @param s The string.
     * @return `true`: yes<br></br> `false`: no
     */
    @JvmStatic
    fun isSpace(s: String?): Boolean {
        if (s == null) {
            return true
        }
        var i = 0
        val len = s.length
        while (i < len) {
            if (!Character.isWhitespace(s[i])) {
                return false
            }
            ++i
        }
        return true
    }

    /**
     * 根据文件路径获取后缀名
     * @return 后缀名
     */
    @JvmStatic
    fun getSuffixByPath(path: String): String {
        // 获取文件名称
        val newFileName = path.substring(path.lastIndexOf(File.separator))
        val newFileNames = newFileName.split(".").toTypedArray()
        return if (newFileNames.size > 1) {
            // 返回后缀名
            newFileNames[newFileNames.size - 1]
        } else ""
    }

}
