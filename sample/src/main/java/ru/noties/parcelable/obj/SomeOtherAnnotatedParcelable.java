package ru.noties.parcelable.obj;

import ru.noties.parcelable.ParcelGen;

/**
 * Created by Dimitry Ivanov on 12.10.2015.
 */
@ParcelGen
public class SomeOtherAnnotatedParcelable {

    private long someLong;

    public long getSomeLong() {
        return someLong;
    }

    public SomeOtherAnnotatedParcelable setSomeLong(long someLong) {
        this.someLong = someLong;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SomeOtherAnnotatedParcelable that = (SomeOtherAnnotatedParcelable) o;

        return someLong == that.someLong;

    }

    @Override
    public int hashCode() {
        return (int) (someLong ^ (someLong >>> 32));
    }
}
