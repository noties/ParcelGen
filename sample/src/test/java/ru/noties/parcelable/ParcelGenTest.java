package ru.noties.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import ru.noties.parcelable.obj.SomeAnnotatedParcelable;
import ru.noties.parcelable.obj.SomeEnum;
import ru.noties.parcelable.obj.SomeOtherAnnotatedParcelable;
import ru.noties.parcelable.obj.SomeParcelable;

/**
 * Created by Dimitry Ivanov on 12.10.2015.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ParcelGenTest extends TestCase {

    SomeAnnotatedParcelable mParcelable;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        mParcelable = new SomeAnnotatedParcelable();
    }

    @Test
    public void testBytes() {
        mParcelable
                .setSomeByte((byte)2)
                .setSomeByteArray(new byte[]{3, 4, 1});
        assertParcelable();
    }

    @Test
    public void testInts() {
        mParcelable
                .setSomeInt(33)
                .setSomeIntArray(new int[]{11, 99, 101, 0, -19});
        assertParcelable();
    }

    @Test
    public void testLongs() {
        mParcelable
                .setSomeLong(4L)
                .setSomeLongArray(new long[]{13, -7, Long.MAX_VALUE});
        assertParcelable();
    }

    @Test
    public void testFloats() {
        mParcelable
                .setSomeFloat(66.F)
                .setSomeFloatArray(new float[]{33.3F, .0F, -16.F, 77.1F});
        assertParcelable();
    }

    @Test
    public void testDoubles() {
        mParcelable
                .setSomeDouble(10.4D)
                .setSomeDoubleArray(new double[]{12.D, .99D, 99.9999D, -100.D});
        assertParcelable();
    }

    @Test
    public void testBools() {
        mParcelable
                .setSomeBool(true)
                .setSomeBoolArray(new boolean[]{true, false, false, false, true});
        assertParcelable();
    }

    @Test
    public void testStrings() {
        mParcelable
                .setSomeString("someString it is")
                .setSomeStringArray(new String[]{"one", "eleven", "fifty five", "zero", "HEllo!"});
        assertParcelable();
    }

    @Test
    public void testEnums() {
        mParcelable
                .setSomeEnum(SomeEnum.DUNNO);
        assertParcelable();
    }

    @Test
    public void testEnums2() {
        mParcelable
                .setSomeEnum(SomeEnum.FALSE)
                .setSomeOtherEnum(SomeEnum.TRUE);
        assertParcelable();
    }

    @Test
    public void testParcelables() {
        mParcelable
                .setRealParcelable(new SomeParcelable().setSomeInt(Integer.MAX_VALUE))
                .setRealParcelableArray(new SomeParcelable[]{
                        new SomeParcelable().setSomeInt(11),
                        new SomeParcelable().setSomeInt(-99),
                        new SomeParcelable().setSomeInt(19999)
                });
        assertParcelable();
    }

    @Test
    public void testParcelables2() {
        mParcelable
                .setSomeOtherAnnotatedParcelable(new SomeOtherAnnotatedParcelable().setSomeLong(66L))
                .setSomeOtherAnnotatedParcelableArray(new SomeOtherAnnotatedParcelable[]{
                        new SomeOtherAnnotatedParcelable().setSomeLong(11),
                        new SomeOtherAnnotatedParcelable().setSomeLong(-8),
                        new SomeOtherAnnotatedParcelable().setSomeLong(Long.MIN_VALUE)
                });
        assertParcelable();
    }

    private void assertParcelable() {

        final Parcel in = Parcel.obtain();
        ((Parcelable) mParcelable).writeToParcel(in, 0);
        final byte[] bytes = in.marshall();
        in.recycle();

        final Parcel out = Parcel.obtain();
        out.unmarshall(bytes, 0, bytes.length);
        out.setDataPosition(0);

        final SomeAnnotatedParcelable unparcelled = callCreator(mParcelable, out);

        out.recycle();

        assertTrue(mParcelable.equals(unparcelled));
    }

    private <T> T callCreator(T object, Parcel parcel) {
        try {
            final Field creatorField = object.getClass().getField("CREATOR");
            final Method createFromParcel = creatorField.getType().getMethod("createFromParcel", Parcel.class);
            //noinspection unchecked
            return (T) createFromParcel.invoke(creatorField.get(object), parcel);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
}
