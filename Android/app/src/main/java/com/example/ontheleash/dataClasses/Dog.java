package com.example.ontheleash.dataClasses;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Dog implements Parcelable {
    private int id;
    private String name;
    // 0 - male, 1 - female
    private int sex;
    private String birthday;
    private String breed;
    private int castration;
    private int ready_to_mate;
    private int for_sale;
    private String image;
    public boolean was_created = false;
    public Uri uri = null;
    public int delete_photo = 0;

    public Dog(){}

    protected Dog(Parcel in) {
        id = in.readInt();
        name = in.readString();
        sex = in.readInt();
        birthday = in.readString();
        breed = in.readString();
        castration = in.readInt();
        ready_to_mate = in.readInt();
        for_sale = in.readInt();
        image = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(sex);
        dest.writeString(birthday);
        dest.writeString(breed);
        dest.writeInt(castration);
        dest.writeInt(ready_to_mate);
        dest.writeInt(for_sale);
        dest.writeString(image);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Dog> CREATOR = new Creator<Dog>() {
        @Override
        public Dog createFromParcel(Parcel in) {
            return new Dog(in);
        }

        @Override
        public Dog[] newArray(int size) {
            return new Dog[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public int getSex() {
        return sex;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getBreed() {
        return breed;
    }

    public int getCastration() {
        return castration;
    }

    public int getReady_to_mate() {
        return ready_to_mate;
    }

    public int getFor_sale() {
        return for_sale;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public void setCastration(int castration) {
        this.castration = castration;
    }

    public void setReady_to_mate(int ready_to_mate) {
        this.ready_to_mate = ready_to_mate;
    }

    public void setFor_sale(int for_sale) {
        this.for_sale = for_sale;
    }

    @Override
    public String toString() {
        return "Dog{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                ", birthday='" + birthday + '\'' +
                ", breed='" + breed + '\'' +
                ", castration=" + castration +
                ", ready_to_mate=" + ready_to_mate +
                ", for_sale=" + for_sale +
                '}';
    }

    public void setId(int id) {
        this.id = id;
    }
}
