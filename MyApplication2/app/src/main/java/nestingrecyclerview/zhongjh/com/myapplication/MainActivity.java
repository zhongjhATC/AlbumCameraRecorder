package nestingrecyclerview.zhongjh.com.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.graphics.Bitmap.createBitmap;

public class MainActivity extends AppCompatActivity {

    private final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码

    private FrameLayout camera_preview;
    private MySurfaceView cameraSurfaceView;
    private Camera camera;
    private Point mScreenResolution;//屏幕分辨率
    private Point previewSizeOnScreen;//相机预览尺寸
    private Point pictureSizeOnScreen;//图片尺寸
    private Bitmap bitmapCamera = null;
    private ImageView img_camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera3);

        camera_preview = (FrameLayout) findViewById(R.id.camera_preview);
        img_camera = (ImageView) findViewById(R.id.img_camera);
        findViewById(R.id.btn_start2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager
                            .PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager
                                    .PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager
                                    .PERMISSION_GRANTED) {
                        initCamera();
                        init();
                    } else {
                        //不具有获取权限，需要进行权限申请
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA}, GET_PERMISSION_REQUEST);
                    }
                } else {
                    initCamera();
                    init();
                }
            }
        });
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (camera != null) {
                    // 控制摄像头自动对焦后才拍摄
                    //关闭声音
                    CameraUtils.setCameraSound(true, MainActivity.this);
                    camera.takePicture(null, null, jpeg);
                }
            }
        });

    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_PERMISSION_REQUEST) {
            int size = 0;
            if (grantResults.length >= 1) {
                int writeResult = grantResults[0];
                //读写内存权限
                boolean writeGranted = writeResult == PackageManager.PERMISSION_GRANTED;//读写内存权限
                if (!writeGranted) {
                    size++;
                }
                //录音权限
                int recordPermissionResult = grantResults[1];
                boolean recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!recordPermissionGranted) {
                    size++;
                }
                //相机权限
                int cameraPermissionResult = grantResults[2];
                boolean cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!cameraPermissionGranted) {
                    size++;
                }
                if (size == 0) {
                    initCamera();
                    init();
                } else {
                    Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initCamera() {
        // 此处默认打开后置摄像头
        // 通过传入参数可以打开前置摄像头
        //判断系统版本大于23，即24（7.0）和以上版本提示打开权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.CAMERA};
            int check = ContextCompat.checkSelfPermission(MainActivity.this, permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (check == PackageManager.PERMISSION_GRANTED) {
                //调用相机
                camera = CameraUtils.openFrontFacingCameraGingerbread();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        } else {
            camera = CameraUtils.openFrontFacingCameraGingerbread();
        }
        if (camera == null) {
            Toast.makeText(this, "摄像头被占用,摄像头权限没打开！", Toast.LENGTH_SHORT).show();
            return;
        }
        setCameraParameters(camera, camera.getParameters());
    }

    private void init() {
        cameraSurfaceView = new MySurfaceView(this, camera);
        //设置界面展示大小
        Point point = CameraUtils.calculateViewSize(previewSizeOnScreen, mScreenResolution);
        System.out.println(point.x + "," + point.y);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(point.x, point.y);
        layoutParams.gravity = Gravity.CENTER;
        cameraSurfaceView.setLayoutParams(layoutParams);
        camera_preview.addView(cameraSurfaceView);
    }

    private void setCameraParameters(Camera camera, Camera.Parameters parameters) {
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point theScreenResolution = new Point();
        display.getSize(theScreenResolution);//得到屏幕的尺寸，单位是像素
        mScreenResolution = theScreenResolution;
        previewSizeOnScreen = CameraUtils.findBestPreviewSizeValue(parameters, theScreenResolution);//通过相机尺寸、屏幕尺寸来得到最好的展示尺寸，此尺寸为相机的
        parameters.setPreviewSize(previewSizeOnScreen.x, previewSizeOnScreen.y);
        pictureSizeOnScreen = CameraUtils.findBestPictureSizeValue(parameters, theScreenResolution);//通过相机尺寸、屏幕尺寸来得到最好的展示尺寸，此尺寸为相机的
        parameters.setPictureSize(pictureSizeOnScreen.x, pictureSizeOnScreen.y);
        boolean isScreenPortrait = mScreenResolution.x < mScreenResolution.y;
        boolean isPreviewSizePortrait = previewSizeOnScreen.x < previewSizeOnScreen.y;
        if (isScreenPortrait != isPreviewSizePortrait) {//相机与屏幕一个方向，则使用相机尺寸
            previewSizeOnScreen = new Point(previewSizeOnScreen.y, previewSizeOnScreen.x);//否则翻个
        }
        // 设置照片的格式
        parameters.setPictureFormat(ImageFormat.JPEG);
        CameraUtils.setFocus(parameters, true, false, true);//设置相机对焦模式
        CameraUtils.setBarcodeSceneMode(parameters, Camera.Parameters.SCENE_MODE_BARCODE);//设置相机场景模式
        CameraUtils.setBestPreviewFPS(parameters);//设置相机帧数
        camera.setParameters(parameters);
        // 系统相机默认是横屏的，我们要旋转90°
        camera.setDisplayOrientation(90);
    }

    //创建jpeg图片回调数据对象
    Camera.PictureCallback jpeg = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            try {
                bitmapCamera = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix matrix = new Matrix();
                matrix.setRotate(270);
                matrix.postScale(-1, 1);
                bitmapCamera = createBitmap(bitmapCamera, 0, 0, bitmapCamera.getWidth(), bitmapCamera.getHeight(), matrix, true);

                zipBitmap();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            camera.stopPreview();//关闭预览 处理数据
            CameraUtils.setCameraSound(false, MainActivity.this);
        }
    };

    private void zipBitmap() {
        try {
            //bitmapCamera = BitmapUtil.zipBitmap(bitmapCamera, 100);
            img_camera.setImageBitmap(bitmapCamera);
            String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "imagetest";
            File file = new File(storagePath);
            if (file != null) {
                FileOutputStream fileOutStream = null;
                fileOutStream = new FileOutputStream(file);
                //把位图输出到指定的文件中
                bitmapCamera.compress(Bitmap.CompressFormat.JPEG, 100, fileOutStream);
                fileOutStream.close();
                System.out.println("**********图片保存成功***********");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //回收数据
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        if (bitmapCamera != null) {
            bitmapCamera.recycle();//回收bitmap空间
            bitmapCamera = null;
        }
    }

}
