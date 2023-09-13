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

    /**
     * 将字母转换成数字
     * @param input 任意字符串
     * @return 转成数字
     */
    public static Long stringToNum(String input) {
        String reg = "[a-zA-Z]";
        String regNum = "^[0-9]*$";
        StringBuilder strBuf = new StringBuilder();
        // 转换为小写
        input = input.toLowerCase().trim();
        if (!"".equals(input)) {
            for (char c : input.toCharArray()) {
                if (String.valueOf(c).matches(reg)) {
                    // 如果是字母则加入
                    strBuf.append(c - 96);
                } else if (String.valueOf(c).matches(regNum)) {
                    // 如果是数字直接加入
                    strBuf.append(c);
                } else {
                    // 如果是别的则直接转成9
                    strBuf.append("9");
                }
            }
            return Long.parseLong(strBuf.toString());
        } else {
            return 0L;
        }
    }

}
