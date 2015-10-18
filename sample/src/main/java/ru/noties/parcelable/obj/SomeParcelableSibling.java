package ru.noties.parcelable.obj;

import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

import ru.noties.parcelable.BundleUtils;
import ru.noties.parcelable.ParcelGen;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
@ParcelGen
public class SomeParcelableSibling extends SomeAnnotatedParcelable {

    private List<SomeParcelable> typedList;
    private Bundle bundle;
    private CharSequence charSequence;
    private CharSequence[] charSequenceArray;

    public SomeParcelableSibling setTypedList(List<SomeParcelable> typedList) {
        this.typedList = typedList;
        return this;
    }

    public SomeParcelableSibling setBundle(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    public SomeParcelableSibling setCharSequence(CharSequence charSequence) {
        this.charSequence = charSequence;
        return this;
    }

    public SomeParcelableSibling setCharSequenceArray(CharSequence[] charSequenceArray) {
        this.charSequenceArray = charSequenceArray;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SomeParcelableSibling that = (SomeParcelableSibling) o;

        if (typedList != null ? !typedList.equals(that.typedList) : that.typedList != null)
            return false;
        if (bundle != null ? !BundleUtils.equals(bundle, that.bundle) : that.bundle != null) return false;
        if (charSequence != null ? !charSequence.equals(that.charSequence) : that.charSequence != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(charSequenceArray, that.charSequenceArray);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (typedList != null ? typedList.hashCode() : 0);
        result = 31 * result + (bundle != null ? bundle.hashCode() : 0);
        result = 31 * result + (charSequence != null ? charSequence.hashCode() : 0);
        result = 31 * result + (charSequenceArray != null ? Arrays.hashCode(charSequenceArray) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SomeParcelableSibling{" +
                "typedList=" + typedList +
                ", bundle=" + bundle +
                ", charSequence=" + charSequence +
                ", charSequenceArray=" + Arrays.toString(charSequenceArray) +
                '}';
    }
}
