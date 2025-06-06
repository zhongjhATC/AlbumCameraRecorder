package com.zhongjh.demo.phone.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.demo.R;
import com.zhongjh.gridview.widget.GridView;

import java.util.ArrayList;
import java.util.List;

/**
 * 适配器
 *
 * @author zhongjh
 * @date 2021/8/6
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private static final int SIZE_20 = 20;
    private final LayoutInflater mInflater;
    List<Data> data = new ArrayList<>();

    public RecyclerAdapter(Activity activity) {
        this.mInflater = LayoutInflater.from(activity);
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
        data.add(data1);

        // 模拟20条
        for (int i = 0; i < SIZE_20; i++) {
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
            this.data.add(data);
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
        holder.mplImageList.setImageUrls(data.get(position).getImageUrls(), false);
        if (!data.get(position).getAudioUrls().isEmpty()) {
            holder.mplImageList.setAudioUrls(data.get(position).getAudioUrls(), false);
        }
        if (!data.get(position).getVideoUrls().isEmpty()) {
            holder.mplImageList.setVideoUrls(data.get(position).getVideoUrls(), false);
        }
        holder.mplImageList.notifyDataSetChanged();
//        holder.mplImageList.setMaskProgressLayoutListener(new MaskProgressLayoutListener() {
//            @Override
//            public boolean onItemVideoStartDownload(@NonNull View view, @NonNull MultiMediaView multiMediaView, int position) {
//                return false;
//            }
//
//            @Override
//            public void onItemAudioStartUploading(@NonNull DisplayMedia displayMedia, @NonNull AudioAdapter.AudioHolder viewHolder) {
//
//            }
//
//            @Override
//            public void onAddDataSuccess(@NotNull List<DisplayMedia> displayMedia) {
//
//            }
//
//            @Override
//            public void onItemAdd(@NotNull View view, @NotNull DisplayMedia displayMedia, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
//                Toast.makeText(mActivity.getApplicationContext(), "这边写跳转相册代码获取到的数据填充该RecyclerView即可", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onItemClick(@NotNull View view, @NotNull DisplayMedia displayMedia) {
//
//            }
//
//            @Override
//            public void onItemStartUploading(@NonNull DisplayMedia displayMedia, @NonNull ImagesAndVideoAdapter.PhotoViewHolder viewHolder) {
//
//            }
//
//            @Override
//            public void onItemClose(@NotNull DisplayMedia displayMedia) {
//
//            }
//
//            @Override
//            public void onItemAudioStartDownload(@NotNull View view, @NotNull String url) {
//
//            }
//
//            @Override
//            public boolean onItemVideoStartDownload(@NotNull View view, @NotNull MultiMediaView multiMediaView) {
//                return false;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public GridView mplImageList;

        public ViewHolder(View view) {
            super(view);
            mplImageList = view.findViewById(R.id.gridView);
        }
    }
}
