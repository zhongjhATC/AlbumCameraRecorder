package com.zhongjh.videomerge

import android.util.Log
import com.coremedia.iso.boxes.Container
import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import com.googlecode.mp4parser.authoring.tracks.AppendTrack
import com.zhongjh.common.coordinator.VideoMergeCoordinator
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.util.*

/**
 * 视频合并管理
 *
 * @author zhongjh
 */
class VideoMergeManager : VideoMergeCoordinator {

    /**
     * 对Mp4文件集合进行追加合并(按照顺序一个一个拼接起来)
     *
     * @param mp4PathList 想要被合成的视频列表，List<String>
     * @param outPutPath  输出合成后视频的文件输出绝对路径  (),包含文件名称+后缀名
     */
    @Throws(IOException::class)
    override fun merge(mp4PathList: List<String>, outPutPath: String) {
        val tag = "拼接视频->> "
        // Movie对象集合[输入]
        val mp4MovieList: MutableList<Movie> = ArrayList()
        // 将每个文件路径都构建成一个Movie对象
        for (mp4Path in mp4PathList) {
            Log.d(tag, "视频路径: $mp4Path")
            mp4MovieList.add(MovieCreator.build(mp4Path))
        }
        // 音频通道集合
        val audioTracks: MutableList<Track> = LinkedList()
        // 视频通道集合
        val videoTracks: MutableList<Track> = LinkedList()
        for (mp4Movie in mp4MovieList) {
            // 对Movie对象集合进行循环
            for (inMovieTrack in mp4Movie.tracks) {
                // 从Movie对象中取出音频通道
                if ("soun" == inMovieTrack.handler) {
                    audioTracks.add(inMovieTrack)
                }
                // 从Movie对象中取出视频通道
                if ("vide" == inMovieTrack.handler) {
                    videoTracks.add(inMovieTrack)
                }
            }
        }
        // 结果Movie对象[输出]
        val resultMovie = Movie()
        // 将所有音频通道追加合并
        if (audioTracks.isNotEmpty()) {
            resultMovie.addTrack(AppendTrack(*audioTracks.toTypedArray()))

        }
        // 将所有视频通道追加合并
        if (videoTracks.isNotEmpty()) {
            resultMovie.addTrack(AppendTrack(*videoTracks.toTypedArray()))
        }
        // 将结果Movie对象封装进容器
        val outContainer: Container = DefaultMp4Builder().build(resultMovie)
        val fileChannel: FileChannel = RandomAccessFile(outPutPath, "rwd").channel
        // 将容器内容写入磁盘
        outContainer.writeContainer(fileChannel)
        fileChannel.close()
    }


}