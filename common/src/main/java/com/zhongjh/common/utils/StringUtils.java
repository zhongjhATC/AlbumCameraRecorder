package com.zhongjh.common.utils;

/**
 * @author Blankj
 * blog  : http://blankj.com
 * time  : 2016/08/16
 * desc  : utils about string
 */
public final class StringUtils {

    /**
     * Return whether the string is null or white space.
     *
     * @param s The string.
     * @return {@code true}: yes<br> {@code false}: no
     */
    public static boolean isSpace(final String s) {
        if (s == null) {
            return true;
        }
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
