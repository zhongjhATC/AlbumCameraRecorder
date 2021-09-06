package gaode.zhongjh.com.common.coordinator;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

/**
 * 协调main的代码，更加简化代码
 * @author zhongjh
 * @date 2021/9/6
 */
public class MainCoordinator {

    public static void combined() {

    }

    private void dialog(Activity activity){
        // 构造器
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // 设置标题
        builder.setTitle("提示");
        // 设置内容
        builder.setMessage("是否确认退出?");
        // 设置确定按钮
        builder.setPositiveButton("好的", (dialog, which) -> {
            // 关闭dialog
            dialog.dismiss();
        });
        // 设置取消按钮
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.dismiss();
        });
        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }
}
