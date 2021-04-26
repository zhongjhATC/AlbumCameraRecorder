package com.zhongjh.albumcamerarecorder.camera.widget;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.View;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.albumcamerarecorder.widget.OperationLayout;

/**
 * 跟父类一样
 * Created by zhongjh on 2018/10/16.
 */
public class PhotoVideoLayout extends OperationLayout {


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

    public PhotoVideoLayout(@NonNull Context context) {
        super(context);
    }

    public PhotoVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public ViewHolder newViewHolder() {
        return new ViewHolder(View.inflate(getContext(), R.layout.layout_photovideo_operae, this));
    }

    @Override
    protected void initListener() {
        super.initListener();
        mViewHolder.tvSectionRecord.setOnClickListener(v -> {
            // 判断当前类型，默认是默认录音模式
            if (mViewHolder.tvSectionRecord.getTag() == null
                    || "0".equals(mViewHolder.tvSectionRecord.getTag())) {
                // 切换分段录制模式
                mViewHolder.tvSectionRecord.setTag("1");
                mViewHolder.tvSectionRecord.setText(getResources().getString(R.string.section_to_record));
                mViewHolder.btnClickOrLong.setSectionMode(true);
            } else {
                // 切换默认的快拍录制模式
                mViewHolder.tvSectionRecord.setTag("0");
                mViewHolder.tvSectionRecord.setText(getResources().getString(R.string.default_to_record));
                mViewHolder.btnClickOrLong.setSectionMode(false);
            }
            mRecordListener.sectionRecord(mViewHolder.tvSectionRecord.getTag().toString());
        });
    }
}
