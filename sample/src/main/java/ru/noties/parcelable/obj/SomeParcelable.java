package ru.noties.parcelable.obj;

import android.os.*;
import android.os.Parcelable;

import java.util.Map;

/**
 * Created by Dimitry Ivanov on 12.10.2015.
 */
public class SomeParcelable implements Parcelable {

    private SomeParcelable(int someInt) {

    }

    private int someInt;

    public int getSomeInt() {
        return someInt;
    }

    public SomeParcelable setSomeInt(int someInt) {
        this.someInt = someInt;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.someInt);
    }

    public SomeParcelable() {
    }

    protected SomeParcelable(Parcel in) {
        this.someInt = in.readInt();
        Map<String, String> map = (java.util.Map<java.lang.String, java.lang.String>) in.readValue(java.util.Map.class.getClassLoader());
    }

    public static final Parcelable.Creator<SomeParcelable> CREATOR = new Parcelable.Creator<SomeParcelable>() {
        public SomeParcelable createFromParcel(Parcel source) {
            return new SomeParcelable(source);
        }

        public SomeParcelable[] newArray(int size) {
            return new SomeParcelable[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SomeParcelable that = (SomeParcelable) o;

        return someInt == that.someInt;

    }

    @Override
    public int hashCode() {
        return someInt;
    }
}
