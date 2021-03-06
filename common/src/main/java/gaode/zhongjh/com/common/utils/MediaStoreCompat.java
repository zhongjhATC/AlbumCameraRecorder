package gaode.zhongjh.com.common.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import gaode.zhongjh.com.common.entity.SaveStrategy;

/**
 * 有关多媒体的文件操作
 *
 * @author zhongjh
 * @date 2018/8/23
 */
public class MediaStoreCompat {

    private final WeakReference<Context> mContext;

    /**
     * 设置目录
     */
    private SaveStrategy mSaveStrategy;

    public MediaStoreCompat(Context context) {
        mContext = new WeakReference<>(context);
    }

    public SaveStrategy getSaveStrategy() {
        return mSaveStrategy;
    }

    /**
     * 检查设备是否具有相机特性。
     *
     * @param context 检查相机特征的上下文。
     * @return 如果设备具有相机特性，则为真。否则为假。
     */
    public static boolean hasCameraFeature(Context context) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 设置目录
     */
    public void setSaveStrategy(SaveStrategy strategy) {
        mSaveStrategy = strategy;
    }

    /**
     * 创建文件
     *
     * @param type 0是图片 1是视频 2是音频
     * @return 临时文件
     */
    public File createFile(int type) {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmssS", Locale.getDefault()).format(new Date());
        String fileName;
        switch (type) {
            case 0:
                fileName = String.format("JPEG_%s.jpg", timeStamp);
                break;
            case 1:
                fileName = String.format("VIDEO_%s.mp4", timeStamp);
                break;
            case 2:
            default:
                fileName = String.format("AUDIO_%s.mp3", timeStamp);
                break;
        }
        File storageDir = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 29以上的版本都必须是私有的
            switch (type) {
                case 0:
                    storageDir = mContext.get().getExternalFilesDir(Environment.DIRECTORY_PICTURES + File.separator + mSaveStrategy.directory);
                    break;
                case 1:
                    storageDir = mContext.get().getExternalFilesDir(Environment.DIRECTORY_MOVIES + File.separator + mSaveStrategy.directory);
                    break;
                case 2:
                default:
                    storageDir = mContext.get().getExternalFilesDir(Environment.DIRECTORY_MUSIC + File.separator + mSaveStrategy.directory);
                    break;
            }
        } else {
            if (mSaveStrategy.isPublic) {
                storageDir = Environment.getExternalStoragePublicDirectory(
                        mSaveStrategy.directory);
                assert storageDir != null;
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
            } else {
                storageDir = mContext.get().getExternalFilesDir(mSaveStrategy.directory);
            }
        }

        // Avoid joining path components manually
        File tempFile = new File(storageDir, fileName);

        // Handle the situation that user's external storage is not ready
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }

        return tempFile;

    }

    /**
     * 返回创建文件的路径
     *
     * @param type 0是图片 1是视频 2是音频
     * @return File
     */
    public File getFilePath(int type) {
        return createFile(type);
    }

    /**
     * 保存bitmap到file
     *
     * @return 返回file的路径
     */
    public File saveFileByBitmap(Bitmap bitmap) {
        File file;
        file = createFile(0);
        try {
            assert file != null;
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 绑定路径
     */
    public Uri getUri(String path) {
        return FileProvider.getUriForFile(mContext.get(), mSaveStrategy.authority, new File(path));
    }

    public Uri getUri() {
        return FileProvider.getUriForFile(mContext.get(), mSaveStrategy.authority, new File(mSaveStrategy.directory));
    }


}
