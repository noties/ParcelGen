package ru.noties.parcelable.obj;


import java.util.Arrays;

import ru.noties.parcelable.ParcelGen;

/**
 * Created by Dimitry Ivanov on 11.10.2015.
 */
@ParcelGen
public class SomeAnnotatedParcelable {

    private byte someByte;
    private byte[] someByteArray;

    private int someInt;
    private int[] someIntArray;

    private long someLong;
    private long[] someLongArray;

    private float someFloat;
    private float[] someFloatArray;

    private double someDouble;
    private double[] someDoubleArray;

    private boolean someBool;
    private boolean[] someBoolArray;

    private String someString;
    private String[] someStringArray;

    private SomeEnum someEnum;
    private SomeEnum someOtherEnum;

    private SomeParcelable realParcelable;
    private SomeParcelable[] realParcelableArray;

    private SomeOtherAnnotatedParcelable someOtherAnnotatedParcelable;
    private SomeOtherAnnotatedParcelable[] someOtherAnnotatedParcelableArray;

    public byte getSomeByte() {
        return someByte;
    }

    public SomeAnnotatedParcelable setSomeByte(byte someByte) {
        this.someByte = someByte;
        return this;
    }

    public byte[] getSomeByteArray() {
        return someByteArray;
    }

    public SomeAnnotatedParcelable setSomeByteArray(byte[] someByteArray) {
        this.someByteArray = someByteArray;
        return this;
    }

    public int getSomeInt() {
        return someInt;
    }

    public SomeAnnotatedParcelable setSomeInt(int someInt) {
        this.someInt = someInt;
        return this;
    }

    public int[] getSomeIntArray() {
        return someIntArray;
    }

    public SomeAnnotatedParcelable setSomeIntArray(int[] someIntArray) {
        this.someIntArray = someIntArray;
        return this;
    }

    public long getSomeLong() {
        return someLong;
    }

    public SomeAnnotatedParcelable setSomeLong(long someLong) {
        this.someLong = someLong;
        return this;
    }

    public long[] getSomeLongArray() {
        return someLongArray;
    }

    public SomeAnnotatedParcelable setSomeLongArray(long[] someLongArray) {
        this.someLongArray = someLongArray;
        return this;
    }

    public float getSomeFloat() {
        return someFloat;
    }

    public SomeAnnotatedParcelable setSomeFloat(float someFloat) {
        this.someFloat = someFloat;
        return this;
    }

    public float[] getSomeFloatArray() {
        return someFloatArray;
    }

    public SomeAnnotatedParcelable setSomeFloatArray(float[] someFloatArray) {
        this.someFloatArray = someFloatArray;
        return this;
    }

    public double getSomeDouble() {
        return someDouble;
    }

    public SomeAnnotatedParcelable setSomeDouble(double someDouble) {
        this.someDouble = someDouble;
        return this;
    }

    public double[] getSomeDoubleArray() {
        return someDoubleArray;
    }

    public SomeAnnotatedParcelable setSomeDoubleArray(double[] someDoubleArray) {
        this.someDoubleArray = someDoubleArray;
        return this;
    }

    public boolean isSomeBool() {
        return someBool;
    }

    public SomeAnnotatedParcelable setSomeBool(boolean someBool) {
        this.someBool = someBool;
        return this;
    }

    public boolean[] getSomeBoolArray() {
        return someBoolArray;
    }

    public SomeAnnotatedParcelable setSomeBoolArray(boolean[] someBoolArray) {
        this.someBoolArray = someBoolArray;
        return this;
    }

    public String getSomeString() {
        return someString;
    }

    public SomeAnnotatedParcelable setSomeString(String someString) {
        this.someString = someString;
        return this;
    }

    public String[] getSomeStringArray() {
        return someStringArray;
    }

    public SomeAnnotatedParcelable setSomeStringArray(String[] someStringArray) {
        this.someStringArray = someStringArray;
        return this;
    }

    public SomeEnum getSomeEnum() {
        return someEnum;
    }

    public SomeAnnotatedParcelable setSomeEnum(SomeEnum someEnum) {
        this.someEnum = someEnum;
        return this;
    }

    public SomeParcelable getRealParcelable() {
        return realParcelable;
    }

    public SomeAnnotatedParcelable setRealParcelable(SomeParcelable realParcelable) {
        this.realParcelable = realParcelable;
        return this;
    }

    public SomeParcelable[] getRealParcelableArray() {
        return realParcelableArray;
    }

    public SomeAnnotatedParcelable setRealParcelableArray(SomeParcelable[] realParcelableArray) {
        this.realParcelableArray = realParcelableArray;
        return this;
    }

    public SomeOtherAnnotatedParcelable getSomeOtherAnnotatedParcelable() {
        return someOtherAnnotatedParcelable;
    }

    public SomeAnnotatedParcelable setSomeOtherAnnotatedParcelable(SomeOtherAnnotatedParcelable someOtherAnnotatedParcelable) {
        this.someOtherAnnotatedParcelable = someOtherAnnotatedParcelable;
        return this;
    }

    public SomeOtherAnnotatedParcelable[] getSomeOtherAnnotatedParcelableArray() {
        return someOtherAnnotatedParcelableArray;
    }

    public SomeAnnotatedParcelable setSomeOtherAnnotatedParcelableArray(SomeOtherAnnotatedParcelable[] someOtherAnnotatedParcelableArray) {
        this.someOtherAnnotatedParcelableArray = someOtherAnnotatedParcelableArray;
        return this;
    }

    public SomeEnum getSomeOtherEnum() {
        return someOtherEnum;
    }

    public SomeAnnotatedParcelable setSomeOtherEnum(SomeEnum someOtherEnum) {
        this.someOtherEnum = someOtherEnum;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SomeAnnotatedParcelable that = (SomeAnnotatedParcelable) o;

        if (someByte != that.someByte) return false;
        if (someInt != that.someInt) return false;
        if (someLong != that.someLong) return false;
        if (Float.compare(that.someFloat, someFloat) != 0) return false;
        if (Double.compare(that.someDouble, someDouble) != 0) return false;
        if (someBool != that.someBool) return false;
        if (!Arrays.equals(someByteArray, that.someByteArray)) return false;
        if (!Arrays.equals(someIntArray, that.someIntArray)) return false;
        if (!Arrays.equals(someLongArray, that.someLongArray)) return false;
        if (!Arrays.equals(someFloatArray, that.someFloatArray)) return false;
        if (!Arrays.equals(someDoubleArray, that.someDoubleArray)) return false;
        if (!Arrays.equals(someBoolArray, that.someBoolArray)) return false;
        if (someString != null ? !someString.equals(that.someString) : that.someString != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(someStringArray, that.someStringArray)) return false;
        if (someEnum != that.someEnum) return false;
        if (someOtherEnum != that.someOtherEnum) return false;
        if (realParcelable != null ? !realParcelable.equals(that.realParcelable) : that.realParcelable != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(realParcelableArray, that.realParcelableArray)) return false;
        if (someOtherAnnotatedParcelable != null ? !someOtherAnnotatedParcelable.equals(that.someOtherAnnotatedParcelable) : that.someOtherAnnotatedParcelable != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(someOtherAnnotatedParcelableArray, that.someOtherAnnotatedParcelableArray);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) someByte;
        result = 31 * result + (someByteArray != null ? Arrays.hashCode(someByteArray) : 0);
        result = 31 * result + someInt;
        result = 31 * result + (someIntArray != null ? Arrays.hashCode(someIntArray) : 0);
        result = 31 * result + (int) (someLong ^ (someLong >>> 32));
        result = 31 * result + (someLongArray != null ? Arrays.hashCode(someLongArray) : 0);
        result = 31 * result + (someFloat != +0.0f ? Float.floatToIntBits(someFloat) : 0);
        result = 31 * result + (someFloatArray != null ? Arrays.hashCode(someFloatArray) : 0);
        temp = Double.doubleToLongBits(someDouble);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (someDoubleArray != null ? Arrays.hashCode(someDoubleArray) : 0);
        result = 31 * result + (someBool ? 1 : 0);
        result = 31 * result + (someBoolArray != null ? Arrays.hashCode(someBoolArray) : 0);
        result = 31 * result + (someString != null ? someString.hashCode() : 0);
        result = 31 * result + (someStringArray != null ? Arrays.hashCode(someStringArray) : 0);
        result = 31 * result + (someEnum != null ? someEnum.hashCode() : 0);
        result = 31 * result + (someOtherEnum != null ? someOtherEnum.hashCode() : 0);
        result = 31 * result + (realParcelable != null ? realParcelable.hashCode() : 0);
        result = 31 * result + (realParcelableArray != null ? Arrays.hashCode(realParcelableArray) : 0);
        result = 31 * result + (someOtherAnnotatedParcelable != null ? someOtherAnnotatedParcelable.hashCode() : 0);
        result = 31 * result + (someOtherAnnotatedParcelableArray != null ? Arrays.hashCode(someOtherAnnotatedParcelableArray) : 0);
        return result;
    }
}
