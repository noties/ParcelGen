package ru.noties.parcelable.obj;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ru.noties.parcelable.BundleUtils;
import ru.noties.parcelable.ParcelGen;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
@ParcelGen
public class SomeParcelableSibling extends SomeAnnotatedParcelable {

    private List<SomeParcelable> typedList;
    private ArrayList<SomeParcelable> typedArrayList;
    private Bundle bundle;
    private CharSequence charSequence;
    private CharSequence[] charSequenceArray;
    private List<CharSequence> charSequenceList;
    private ArrayList<CharSequence> charSequenceArrayList;
    private Object object;
    private Map<String, String> map;

    private SomeParcelableSibling(int i) {
        Map<java.lang.String, java.lang.String> map;
    }

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

    public SomeParcelableSibling setCharSequenceList(List<CharSequence> charSequenceList) {
        this.charSequenceList = charSequenceList;
        return this;
    }

    public SomeParcelableSibling setCharSequenceArrayList(ArrayList<CharSequence> charSequenceArrayList) {
        this.charSequenceArrayList = charSequenceArrayList;
        return this;
    }

    @Override
    public String toString() {
        return "SomeParcelableSibling{" +
                "typedList=" + typedList +
                ", bundle=" + bundle +
                ", charSequence=" + charSequence +
                ", charSequenceArray=" + Arrays.toString(charSequenceArray) +
                ", charSequenceList=" + charSequenceList +
                '}';
    }

    // the only difference is BundleUtils call to `equals`
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SomeParcelableSibling sibling = (SomeParcelableSibling) o;

        if (typedList != null ? !typedList.equals(sibling.typedList) : sibling.typedList != null)
            return false;
        if (bundle != null ? !BundleUtils.equals(bundle, sibling.bundle) : sibling.bundle != null) return false;
        if (charSequence != null ? !charSequence.equals(sibling.charSequence) : sibling.charSequence != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(charSequenceArray, sibling.charSequenceArray)) return false;
        return !(charSequenceList != null ? !charSequenceList.equals(sibling.charSequenceList) : sibling.charSequenceList != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (typedList != null ? typedList.hashCode() : 0);
        result = 31 * result + (bundle != null ? bundle.hashCode() : 0);
        result = 31 * result + (charSequence != null ? charSequence.hashCode() : 0);
        result = 31 * result + (charSequenceArray != null ? Arrays.hashCode(charSequenceArray) : 0);
        result = 31 * result + (charSequenceList != null ? charSequenceList.hashCode() : 0);
        return result;
    }
}
