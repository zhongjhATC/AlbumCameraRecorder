package gaode.zhongjh.com.common.listener;

import android.view.View;

/**
 * 防抖动点击
 *
 * @author zhongjh
 * @date 2021/10/14
 */
public abstract class OnMoreClickListener implements View.OnClickListener {

    public static final int MIN_CLICK_DELAY_TIME = 1000;

    private long lastTime = 0;

    private int btnId = 0;

    /**
     * 防止抖动事件
     * @param v 控件
     */
    public abstract void onMoreClickListener(View v);

    @Override
    public void onClick(View v) {
        long currentTime = System.currentTimeMillis();
        if (btnId != v.getId()) {
            lastTime = 0;
        }
        if (currentTime - lastTime > MIN_CLICK_DELAY_TIME) {
            btnId = v.getId();
            lastTime = currentTime;
            onMoreClickListener(v);
        }

    }

}
