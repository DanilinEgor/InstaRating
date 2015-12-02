package ru.egor_d.instarating.model;

import android.os.Parcel;
import android.os.Parcelable;

public class InstagramUser implements Parcelable {
    public String username;
    public String bio;
    public String website;
    public String profile_picture;
    public String full_name;
    public InstagramCounts counts;
    public String id;

    protected InstagramUser(Parcel in) {
        username = in.readString();
        bio = in.readString();
        website = in.readString();
        profile_picture = in.readString();
        full_name = in.readString();
        id = in.readString();
    }

    public static final Creator<InstagramUser> CREATOR = new Creator<InstagramUser>() {
        @Override
        public InstagramUser createFromParcel(Parcel in) {
            return new InstagramUser(in);
        }

        @Override
        public InstagramUser[] newArray(int size) {
            return new InstagramUser[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(username);
        dest.writeString(bio);
        dest.writeString(website);
        dest.writeString(profile_picture);
        dest.writeString(full_name);
        dest.writeString(id);
    }
}
