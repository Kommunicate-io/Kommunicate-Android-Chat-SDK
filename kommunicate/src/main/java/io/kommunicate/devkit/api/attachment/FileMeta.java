package io.kommunicate.devkit.api.attachment;

import android.os.Parcel;
import android.os.Parcelable;

import io.kommunicate.commons.json.JsonMarker;

/**
 * Created by adarsh on 4/10/14.
 */
public class FileMeta extends JsonMarker implements Parcelable {

    private String key;
    private String userKey;
    private String blobKey;
    private String thumbnailBlobKey;
    private String name;
    private String url;
    private int size;
    private String contentType;
    private String thumbnailUrl;
    private Long createdAtTime;

    public FileMeta(Parcel in) {
        key = in.readString();
        userKey = in.readString();
        blobKey = in.readString();
        thumbnailBlobKey = in.readString();
        name = in.readString();
        url = in.readString();
        size = in.readInt();
        contentType = in.readString();
        thumbnailUrl = in.readString();
        createdAtTime = in.readLong() != -1 ? in.readLong() : null;
    }

    public FileMeta() {
    }

    public String getKeyString() {
        return key;
    }

    public void setKeyString(String keyString) {
        this.key = keyString;
    }

    public String getSuUserKeyString() {
        return userKey;
    }

    public void setSuUserKeyString(String suUserKeyString) {
        this.userKey = suUserKeyString;
    }

    public String getBlobKeyString() {
        return blobKey;
    }

    public String getThumbnailBlobKey() {
        return thumbnailBlobKey;
    }

    public void setThumbnailBlobKey(String thumbnailBlobKey) {
        this.thumbnailBlobKey = thumbnailBlobKey;
    }

    public void setBlobKeyString(String blobKeyString) {
        this.blobKey = blobKeyString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(Long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSizeInReadableFormat() {
        String value = "0 KB";
        if (size / 1024 >= 1024) {
            value = String.valueOf(Math.round(size / (1024 * 1024))) + " MB";
        } else {
            value = String.valueOf(Math.round(size / 1024)) + " KB";
        }
        return value;
    }

    @Override
    public String toString() {
        return "FileMeta{" +
                "key='" + key + '\'' +
                ", userKey='" + userKey + '\'' +
                ", blobKey='" + blobKey + '\'' +
                ", thumbnailBlobKey='" + thumbnailBlobKey + '\'' +
                ", url=" + url +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", contentType='" + contentType + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", createdAtTime=" + createdAtTime +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(userKey);
        dest.writeString(blobKey);
        dest.writeString(thumbnailBlobKey);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeInt(size);
        dest.writeString(contentType);
        dest.writeString(thumbnailUrl);
        dest.writeLong(createdAtTime != null ? createdAtTime : -1);
    }

    public static final Creator<FileMeta> CREATOR = new Creator<FileMeta>() {
        @Override
        public FileMeta createFromParcel(Parcel in) {
            return new FileMeta(in);
        }

        @Override
        public FileMeta[] newArray(int size) {
            return new FileMeta[size];
        }
    };
}
