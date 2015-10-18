package ru.noties.parcelable.obj;

import ru.noties.parcelable.ParcelGen;

/**
 * Created by Dimitry Ivanov on 18.10.2015.
 */
@ParcelGen
public class SomeParcelableSibling2 extends SomeParcelableSibling {

    private transient long someLong;
    private String someString;

    @Override
    public SomeParcelableSibling2 setSomeLong(long someLong) {
        this.someLong = someLong;
        return this;
    }

    @Override
    public SomeParcelableSibling2 setSomeString(String someString) {
        this.someString = someString;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SomeParcelableSibling2 that = (SomeParcelableSibling2) o;

        if (someLong != that.someLong) return false;
        return !(someString != null ? !someString.equals(that.someString) : that.someString != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (someLong ^ (someLong >>> 32));
        result = 31 * result + (someString != null ? someString.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SomeParcelableSibling2{" +
                "someLong=" + someLong +
                ", someString='" + someString + '\'' +
                '}';
    }
}
