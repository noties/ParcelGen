package ru.noties.parcelable;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(typedList);
        dest.writeBundle(bundle);
        TextUtils.writeToParcel(charSequence, dest, flags);
        if (charSequenceArray == null) {
            dest.writeInt(-1);
        } else {
            final int size = charSequenceArray.length;
            dest.writeInt(size);
            for (CharSequence aCharSequenceArray : charSequenceArray) {
                TextUtils.writeToParcel(aCharSequenceArray, dest, flags);
            }
        }
    }

    public ToCheck() {
    }

    protected ToCheck(Parcel in) {
        this.typedList = in.createTypedArrayList(SomeParcelable.CREATOR);
        this.bundle = in.readBundle();
        this.charSequence = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        final int charSequenceArraySize = in.readInt();
        if (charSequenceArraySize == -1) {
            this.charSequenceArray = null;
        } else {
            this.charSequenceArray = new CharSequence[charSequenceArraySize];
            for (int i = 0; i < charSequenceArraySize; i++) {
                this.charSequenceArray[i] = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            }
        }
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
