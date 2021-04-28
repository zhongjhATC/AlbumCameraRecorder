package com.zhongjh.albumcamerarecorder.camera.widget;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.View;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.widget.BaseOperationLayout;

/**
 * 跟父类一样
 * Created by zhongjh on 2018/10/16.
 */
public class PhotoVideoLayoutBase extends BaseOperationLayout {


    private RecordListener mRecordListener;


    /**
     * 操作按钮的Listener
     */
    public interface RecordListener {

        /**
         *
         * @param tag 0代表当前是快拍默认录制模式，1代表当前是分段录制模式
         */
        void sectionRecord(String tag);

    }

    public void setRecordListener(RecordListener recordListener) {
        this.mRecordListener = recordListener;
    }

    public PhotoVideoLayoutBase(@NonNull Context context) {
        super(context);
    }

    public PhotoVideoLayoutBase(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoVideoLayoutBase(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public ViewHolder newViewHolder() {
        return new ViewHolder(View.inflate(getContext(), R.layout.layout_photovideo_operate, this));
    }

    @Override
    protected void initListener() {
        super.initListener();
        viewHolder.tvSectionRecord.setOnClickListener(v -> {
            // 判断当前类型，默认是默认录音模式
            if (viewHolder.tvSectionRecord.getTag() == null
                    || "0".equals(viewHolder.tvSectionRecord.getTag())) {
                // 切换分段录制模式
                viewHolder.tvSectionRecord.setTag("1");
                viewHolder.tvSectionRecord.setText(getResources().getString(R.string.section_to_record));
                viewHolder.btnClickOrLong.setSectionMode(true);
            } else {
                // 切换默认的快拍录制模式
                viewHolder.tvSectionRecord.setTag("0");
                viewHolder.tvSectionRecord.setText(getResources().getString(R.string.default_to_record));
                viewHolder.btnClickOrLong.setSectionMode(false);
            }
            mRecordListener.sectionRecord(viewHolder.tvSectionRecord.getTag().toString());
        });
    }
}
