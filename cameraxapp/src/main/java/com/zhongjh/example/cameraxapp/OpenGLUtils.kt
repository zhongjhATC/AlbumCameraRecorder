package com.zhongjh.example.cameraxapp

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class OpenGLUtils {

    fun readRawTextFile(context: Context, rawId: Int): String {
        val text = context.resources.openRawResource(rawId)
        val br = BufferedReader(InputStreamReader(text))
        var line: String?
        val sb = StringBuilder()
        while (br.readLine().also { line = it } != null) {
            sb.append(line)
            sb.append("\n")
        }
        br.close()
        return sb.toString()
    }

}