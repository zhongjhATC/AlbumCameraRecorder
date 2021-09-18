package com.zhongjh.albumcamerarecorder.camera.util;

import android.os.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 文件工具类
 *
 * @author zhongjh
 */
public class FileUtil {

    public static boolean deleteFile(String url) {
        boolean result = false;
        File file = new File(url);
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }

    public static boolean deleteFile(File file) {
        boolean result = false;
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }

    /**
     * Copy the directory or file.
     *
     * @param src  The source.
     * @param dest The destination.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean copy(final File src,
                               final File dest) {
        return copy(src, dest, null, null);
    }

    /**
     * Copy the directory or file.
     *
     * @param src      The source.
     * @param dest     The destination.
     * @param listener The replace listener.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean copy(final File src,
                               final File dest,
                               final OnReplaceListener listener,
                               final FileIOUtils.OnProgressUpdateListener onProgressUpdateListener) {
        if (src == null) {
            return false;
        }
        if (src.isDirectory()) {
            return copyDir(src, dest, listener, onProgressUpdateListener);
        }
        return copyFile(src, dest, listener, onProgressUpdateListener);
    }

    /**
     * Copy the directory.
     *
     * @param srcDir   The source directory.
     * @param destDir  The destination directory.
     * @param listener The replace listener.
     * @return {@code true}: success<br>{@code false}: fail
     */
    private static boolean copyDir(final File srcDir,
                                   final File destDir,
                                   final OnReplaceListener listener,
                                   final FileIOUtils.OnProgressUpdateListener onProgressUpdateListener) {
        return copyOrMoveDir(srcDir, destDir, listener, onProgressUpdateListener, false);
    }

    /**
     * Copy the file.
     *
     * @param srcFile  The source file.
     * @param destFile The destination file.
     * @param listener The replace listener.
     * @return {@code true}: success<br>{@code false}: fail
     */
    private static boolean copyFile(final File srcFile,
                                    final File destFile,
                                    final OnReplaceListener listener,
                                    final FileIOUtils.OnProgressUpdateListener onProgressUpdateListener) {
        return copyOrMoveFile(srcFile, destFile, listener, onProgressUpdateListener, false);
    }

    private static boolean copyOrMoveDir(final File srcDir,
                                         final File destDir,
                                         final OnReplaceListener listener,
                                         final FileIOUtils.OnProgressUpdateListener onProgressUpdateListener,
                                         final boolean isMove) {
        if (srcDir == null || destDir == null) {
            return false;
        }
        // destDir's path locate in srcDir's path then return false
        String srcPath = srcDir.getPath() + File.separator;
        String destPath = destDir.getPath() + File.separator;
        if (destPath.contains(srcPath)) {
            return false;
        }
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            return false;
        }
        if (!createOrExistsDir(destDir)) {
            return false;
        }
        File[] files = srcDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                File oneDestFile = new File(destPath + file.getName());
                if (file.isFile()) {
                    if (!copyOrMoveFile(file, oneDestFile, listener, onProgressUpdateListener, isMove)) {
                        return false;
                    }
                } else if (file.isDirectory()) {
                    if (!copyOrMoveDir(file, oneDestFile, listener, onProgressUpdateListener, isMove)) {
                        return false;
                    }
                }
            }
        }
        return !isMove || deleteDir(srcDir);
    }

    private static boolean copyOrMoveFile(final File srcFile,
                                          final File destFile,
                                          final OnReplaceListener listener,
                                          final FileIOUtils.OnProgressUpdateListener onProgressUpdateListener,
                                          final boolean isMove) {
        if (srcFile == null || destFile == null) {
            return false;
        }
        // srcFile equals destFile then return false
        if (srcFile.equals(destFile)) {
            return false;
        }
        // srcFile doesn't exist or isn't a file then return false
        if (!srcFile.exists() || !srcFile.isFile()) {
            return false;
        }
        if (destFile.exists()) {
            // require delete the old file
            if (listener == null || listener.onReplace(srcFile, destFile)) {
                // unsuccessfully delete then return false
                if (!destFile.delete()) {
                    return false;
                }
            } else {
                return true;
            }
        }
        if (!createOrExistsDir(destFile.getParentFile())) {
            return false;
        }
        try {
            return FileIOUtils.writeFileFromIS(destFile, new FileInputStream(srcFile), false, onProgressUpdateListener)
                    && !(isMove && !deleteFile(srcFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create a directory if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * Delete the directory.
     *
     * @param dir The directory.
     * @return {@code true}: success<br>{@code false}: fail
     */
    private static boolean deleteDir(final File dir) {
        if (dir == null) {
            return false;
        }
        // dir doesn't exist then return true
        if (!dir.exists()) {
            return true;
        }
        // dir isn't a directory then return false
        if (!dir.isDirectory()) {
            return false;
        }
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.delete()) {
                        return false;
                    }
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

    /**
     * Return the file by path.
     *
     * @param filePath The path of file.
     * @return the file
     */
    public static File getFileByPath(final String filePath) {
        return StringUtils.isSpace(filePath) ? null : new File(filePath);
    }

    /**
     * Create a file if it doesn't exist, otherwise do nothing.
     *
     * @param filePath The path of file.
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsFile(final String filePath) {
        return createOrExistsFile(getFileByPath(filePath));
    }

    /**
     * Create a file if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsFile(final File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return file.isFile();
        }
        if (!createOrExistsDir(file.getParentFile())) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface OnReplaceListener {
        boolean onReplace(File srcFile, File destFile);
    }
}
