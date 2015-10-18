package ru.noties.parcelable;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import ru.noties.parcelable.obj.SomeParcelable;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
public class ToCheck implements Parcelable {

    private List<SomeParcelable> typedList;
    private Bundle bundle;
    private CharSequence charSequence;
    private CharSequence[] charSequenceArray;
    private List<CharSequence> charSequenceList;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(bundle);
        dest.writeTypedList(typedList);
//        dest.writeParcelable(this.charSequence, flags);
//        dest.writeParcelable(this.charSequenceArray, flags);
        dest.writeList(this.charSequenceList);
    }

    public ToCheck() {
    }

    protected ToCheck(Parcel in) {
        bundle = in.readBundle();
        this.typedList = in.createTypedArrayList(SomeParcelable.CREATOR);
//        this.charSequence = in.readParcelable(CharSequence.class.getClassLoader());
//        this.charSequenceArray = in.readParcelable(CharSequence[].class.getClassLoader());
        this.charSequenceList = new ArrayList<CharSequence>();
        in.readList(this.charSequenceList, List.class.getClassLoader());
    }

    public static final Creator<ToCheck> CREATOR = new Creator<ToCheck>() {
        public ToCheck createFromParcel(Parcel source) {
            return new ToCheck(source);
        }

        public ToCheck[] newArray(int size) {
            return new ToCheck[size];
        }
    };
}
