package gaode.zhongjh.com.common.entity;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import gaode.zhongjh.com.common.enums.MimeType;
import gaode.zhongjh.com.common.enums.MultimediaTypes;

/**
 * 多媒体实体类
 *
 * @author zhongjh
 * @date 2019/1/22
 */
public class MultiMedia implements Parcelable {

    protected  long id;
    /**
     * 九宫格的当前图片索引，不计算视频和录音，因为这个position随便会改变，所以不加入hashCode,equals这些里面计算,也可以用作CameraLayout的索引
     */
    protected int position = -1;
    /**
     * 真实路径
     */
    protected String path;
    /**
     * 在线网址
     */
    protected String url;
    /**
     * 图片资源id
     */
    protected int drawableId = -1;
    /**
     * 这是一个封装在共享数据库ContentResolver的一个uri，只能通过ContentResolver.query查找相关信息
     */
    protected Uri mediaUri;
    /**
     * 以路径转换成的uri，专用于提供给progresslibrary使用
     */
    protected Uri uri;
    /**
     * 范围类型,0是图片,1是视频,2是音频,-1是添加功能 MultimediaTypes
     */
    @MultimediaTypes
    protected int type;
    /**
     * 具体类型，jpg,png,mp3等等
     */
    protected String mimeType;
    public long size;
    /**
     * only for video, in ms
     */
    public long duration;
    /**
     * 编辑前的真实路径
     */
    private String oldPath;
    /**
     * 编辑前的相册URI
     */
    private Uri oldMediaUri;
    /**
     * 编辑前的URI
     */
    private Uri oldUri;


    public MultiMedia() {

    }

    public MultiMedia(Uri mediaUri) {
        this.id = -1;
        this.mimeType = MimeType.JPEG.toString();
        this.mediaUri = mediaUri;
        this.size = -1;
        this.duration = -1;
    }

    public MultiMedia(Uri mediaUri, String url) {
        this.id = -1;
        this.mimeType = MimeType.JPEG.toString();
        this.mediaUri = mediaUri;
        this.url = url;
        this.size = -1;
        this.duration = -1;
    }

    private MultiMedia(long id, String mimeType, long size, long duration) {
        this.id = id;
        this.mimeType = mimeType;
        Uri contentUri;
        if (isImage()) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (isVideo()) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            // ?
            contentUri = MediaStore.Files.getContentUri("external");
        }
        this.mediaUri = ContentUris.withAppendedId(contentUri, id);
        this.size = size;
        this.duration = duration;
    }

    public void setType(@MultimediaTypes int multiMediaState) {
        this.type = multiMediaState;
    }

    public int getType() {
        return this.type;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Uri getMediaUri() {
        return mediaUri;
    }

    public void setMediaUri(Uri mediaUri) {
        this.mediaUri = mediaUri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public Uri getOldMediaUri() {
        return oldMediaUri;
    }

    public void setOldMediaUri(Uri oldMediaUri) {
        this.oldMediaUri = oldMediaUri;
    }

    public Uri getOldUri() {
        return oldUri;
    }

    public void setOldUri(Uri oldUri) {
        this.oldUri = oldUri;
    }

    /**
     * 重写equals，所以如果修改以下这些值，那么将会导致不相等
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MultiMedia)) {
            return false;
        }

        MultiMedia other = (MultiMedia) obj;
        return id == other.id && (mimeType != null && mimeType.equals(other.mimeType) || (mimeType == null && other.mimeType == null))
                && (mediaUri != null && mediaUri.equals(other.mediaUri) || (mediaUri == null && other.mediaUri == null))
                && (uri != null && uri.equals(other.uri) || (uri == null && other.uri == null))
                && size == other.size
                && duration == other.duration
                && position == other.position
                && drawableId == other.drawableId;

    }

    /**
     * 重写hashCode，所以如果修改以下这些值，那么将会它存于的hashmap找不到它
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Long.valueOf(id).hashCode();
        if (mimeType != null) {
            result = 31 * result + mimeType.hashCode();
        }
        if (mediaUri != null) {
            result = 31 * result + mediaUri.hashCode();
        }
        if (uri != null) {
            result = 31 * result + uri.hashCode();
        }
        result = 31 * result + Long.valueOf(size).hashCode();
        result = 31 * result + Long.valueOf(duration).hashCode();
        result = 31 * result + Long.valueOf(drawableId).hashCode();
        return result;
    }

    public boolean isImage() {
        if (mimeType == null) {
            return false;
        }
        return mimeType.equals(MimeType.JPEG.toString())
                || mimeType.equals(MimeType.PNG.toString())
                || mimeType.equals(MimeType.GIF.toString())
                || mimeType.equals(MimeType.BMP.toString())
                || mimeType.equals(MimeType.WEBP.toString());
    }

    public boolean isGif() {
        if (mimeType == null) {
            return false;
        }
        return mimeType.equals(MimeType.GIF.toString());
    }

    public boolean isMp3() {
        if (mimeType == null) {
            return false;
        }
        return mimeType.equals(MimeType.MP3.toString());
    }

    public boolean isVideo() {
        if (mimeType == null) {
            return false;
        }
        return mimeType.equals(MimeType.MPEG.toString())
                || mimeType.equals(MimeType.MP4.toString())
                || mimeType.equals(MimeType.QUICKTIME.toString())
                || mimeType.equals(MimeType.THREEGPP.toString())
                || mimeType.equals(MimeType.THREEGPP2.toString())
                || mimeType.equals(MimeType.MKV.toString())
                || mimeType.equals(MimeType.WEBM.toString())
                || mimeType.equals(MimeType.TS.toString())
                || mimeType.equals(MimeType.AVI.toString());
    }

    public static MultiMedia valueOf(Cursor cursor) {
        return new MultiMedia(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)),
                cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)),
                cursor.getLong(cursor.getColumnIndex("duration")));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.position);
        dest.writeString(this.path);
        dest.writeString(this.url);
        dest.writeInt(this.drawableId);
        dest.writeParcelable(this.mediaUri, flags);
        dest.writeParcelable(this.uri, flags);
        dest.writeInt(this.type);
        dest.writeString(this.mimeType);
        dest.writeLong(this.size);
        dest.writeLong(this.duration);
    }

    protected MultiMedia(Parcel in) {
        this.id = in.readLong();
        this.position = in.readInt();
        this.path = in.readString();
        this.url = in.readString();
        this.drawableId = in.readInt();
        this.mediaUri = in.readParcelable(Uri.class.getClassLoader());
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.type = in.readInt();
        this.mimeType = in.readString();
        this.size = in.readLong();
        this.duration = in.readLong();
    }

    public static final Creator<MultiMedia> CREATOR = new Creator<MultiMedia>() {
        @Override
        public MultiMedia createFromParcel(Parcel source) {
            return new MultiMedia(source);
        }

        @Override
        public MultiMedia[] newArray(int size) {
            return new MultiMedia[size];
        }
    };
}
