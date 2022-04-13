package com.sakesnake.frankly.home.postfeeds.allPostFeeds;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseObject;

// This will only use in image, memes and quotes list (Full)
public class CustomParseObject implements Parcelable {

    private ParseObject parseObject;

    private boolean postLikedByMe = false;

    public CustomParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }


    public ParseObject getParseObject(){
        return this.parseObject;
    }

    // Only will be set if post is liked by me otherwise false
    public void setPostLikedByMe(final boolean isLiked){
        this.postLikedByMe = isLiked;
    }

    public boolean getIsLikedByMe(){
        return this.postLikedByMe;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.parseObject, flags);
        dest.writeByte(this.postLikedByMe ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.parseObject = source.readParcelable(ParseObject.class.getClassLoader());
        this.postLikedByMe = source.readByte() != 0;
    }

    protected CustomParseObject(Parcel in) {
        this.parseObject = in.readParcelable(ParseObject.class.getClassLoader());
        this.postLikedByMe = in.readByte() != 0;
    }

    public static final Parcelable.Creator<CustomParseObject> CREATOR = new Parcelable.Creator<CustomParseObject>() {
        @Override
        public CustomParseObject createFromParcel(Parcel source) {
            return new CustomParseObject(source);
        }

        @Override
        public CustomParseObject[] newArray(int size) {
            return new CustomParseObject[size];
        }
    };
}
