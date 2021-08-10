package com.zhongjh.cameraapp.phone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.cameraapp.R;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 适配器
 *
 * @author zhongjh
 * @date 2021/8/6
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    List<Data> datas = new ArrayList<>();

    public RecyclerAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        initData();
    }

    /**
     * 构造数据
     */
    private void initData() {
        Data data1 = new Data();
        List<String> imageUrls1 = new ArrayList<>();
        imageUrls1.add("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg11.51tietu.net%2Fpic%2F2016-071418%2F20160714181543xyu10ukncwf221991.jpg&refer=http%3A%2F%2Fimg11.51tietu.net&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1631178701&t=bc3132a59d5252ef953c3204e0d96939");
        imageUrls1.add("https://img1.baidu.com/it/u=3766151103,2483188409&fm=26&fmt=auto&gp=0.jpg");
        data1.setImageUrls(imageUrls1);
        datas.add(data1);

        // 模拟20条
        for (int i = 0; i < 20; i++) {
            if (i == 2) {
                // 第三个跳过，尽量模拟真实情况
                continue;
            }
            Data data = new Data();
            List<String> imageUrls = new ArrayList<>();
            imageUrls.add("https://img.huoyunji.com/photo_20190221105726_Android_15181?imageMogr2/auto-orient/thumbnail/!280x280r/gravity/Center/crop/280x280/format/jpg/interlace/1/blur/1x0/quality/90");
            List<String> videoUrls = new ArrayList<>();
            videoUrls.add("https://img.huoyunji.com/video_20190221105749_Android_31228");
            List<String> audioUrls = new ArrayList<>();
            audioUrls.add("https://img.huoyunji.com/audio_20190221105823_Android_28360");
            data.setImageUrls(imageUrls);
            data.setAudioUrls(audioUrls);
            data.setVideoUrls(videoUrls);
            datas.add(data);
        }
    }

    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.mplImageList.setOperation(true);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mplImageList.reset();
        holder.mplImageList.setImageUrls(datas.get(position).getImageUrls());
        if (datas.get(position).getAudioUrls().size() > 0) {
            holder.mplImageList.setAudioUrls(datas.get(position).getAudioUrls());
        }
        if (datas.get(position).getVideoUrls().size() > 0) {
            holder.mplImageList.setVideoUrls(datas.get(position).getVideoUrls());
        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public MaskProgressLayout mplImageList;

        public ViewHolder(View view) {
            super(view);
            mplImageList = view.findViewById(R.id.mplImageList);
        }
    }
}
