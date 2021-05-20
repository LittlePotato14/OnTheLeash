package com.example.ontheleash.dataClasses;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class User implements Parcelable {
    private int id;
    private String name;
    private String username;
    private String description;
    private String avatar;
    private double latitude;
    private double longitude;
    private int is_location_set;
    private List<Dog> dogs;
    public int delete_photo = 0;

    protected User(Parcel in) {
        id = in.readInt();
        name = in.readString();
        username = in.readString();
        description = in.readString();
        avatar = in.readString();
        dogs = in.createTypedArrayList(Dog.CREATOR);
        latitude = in.readDouble();
        longitude = in.readDouble();
        is_location_set = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(username);
        dest.writeString(description);
        dest.writeString(avatar);
        dest.writeTypedList(dogs);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(is_location_set);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        return username;
    }

    public String getDescription() {
        return description;
    }

    public String getAvatar() {
        return avatar;
    }

    public List<Dog> getDogs() {
        return dogs;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", userName='" + username + '\'' +
                ", description='" + description + '\'' +
                ", dogs=" + dogs +
                '}';
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getIs_location_set() {
        return is_location_set;
    }
}

