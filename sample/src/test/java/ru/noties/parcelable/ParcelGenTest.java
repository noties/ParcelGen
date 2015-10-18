package ru.noties.parcelable;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;

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
import ru.noties.parcelable.obj.SomeParcelableSibling;

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
                .setSomeByte((byte) 2)
                .setSomeByteArray(new byte[]{3, 4, 1});
        assertParcelable(mParcelable);
    }

    @Test
    public void testInts() {
        mParcelable
                .setSomeInt(33)
                .setSomeIntArray(new int[]{11, 99, 101, 0, -19});
        assertParcelable(mParcelable);
    }

    @Test
    public void testLongs() {
        mParcelable
                .setSomeLong(4L)
                .setSomeLongArray(new long[]{13, -7, Long.MAX_VALUE});
        assertParcelable(mParcelable);
    }

    @Test
    public void testFloats() {
        mParcelable
                .setSomeFloat(66.F)
                .setSomeFloatArray(new float[]{33.3F, .0F, -16.F, 77.1F});
        assertParcelable(mParcelable);
    }

    @Test
    public void testDoubles() {
        mParcelable
                .setSomeDouble(10.4D)
                .setSomeDoubleArray(new double[]{12.D, .99D, 99.9999D, -100.D});
        assertParcelable(mParcelable);
    }

    @Test
    public void testBools() {
        mParcelable
                .setSomeBool(true)
                .setSomeBoolArray(new boolean[]{true, false, false, false, true});
        assertParcelable(mParcelable);
    }

    @Test
    public void testStrings() {
        mParcelable
                .setSomeString("someString it is")
                .setSomeStringArray(new String[]{"one", "eleven", "fifty five", "zero", "HEllo!"});
        assertParcelable(mParcelable);
    }

    @Test
    public void testEnums() {
        mParcelable
                .setSomeEnum(SomeEnum.DUNNO);
        assertParcelable(mParcelable);
    }

    @Test
    public void testEnums2() {
        mParcelable
                .setSomeEnum(SomeEnum.FALSE)
                .setSomeOtherEnum(SomeEnum.TRUE);
        assertParcelable(mParcelable);
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
        assertParcelable(mParcelable);
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
        assertParcelable(mParcelable);
    }

    // this test is failing & I'm not sure why...
    // Seems that SpannableString.equals() is not returning true as expected
    @Test
    public void testCharSequence() {

        final Spannable.Factory factory = Spannable.Factory.getInstance();

        final Spannable single = factory.newSpannable("Hello Single Spannable");
        single.setSpan(new UnderlineSpan(), 0, single.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        final Spannable s1 = factory.newSpannable("S1");
        s1.setSpan(new SuperscriptSpan(), 0, s1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        final Spannable s2 = factory.newSpannable("S@_dskfjsjdhfkjf");
        s2.setSpan(new StrikethroughSpan(), 0, s2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        final SomeParcelableSibling sibling = new SomeParcelableSibling()
                .setCharSequence(single)
                .setCharSequenceArray(new CharSequence[] { s1, s2 });

        assertParcelable(sibling);
    }

    @Test
    public void testBundle() {

        // no support for arrays..
        final Bundle bundle = new Bundle();
        bundle.putString("string", "some string");
        bundle.putInt("int", 123);
        bundle.putLong("long", 999L);
        bundle.putFloat("asddd", .000001F);

        final SomeParcelableSibling sibling = new SomeParcelableSibling()
                .setBundle(bundle);

        assertParcelable(sibling);
    }

    @Test
    public void testTransient() {
        throw new IllegalStateException("Not implemented");
    }

    @Test
    public void testSuperCall() {
        throw new IllegalStateException("Not implemented");
    }

    private void assertParcelable(Object p) {

        final Parcel in = Parcel.obtain();
        ((Parcelable) p).writeToParcel(in, 0);
        final byte[] bytes = in.marshall();

        final Parcel out = Parcel.obtain();
        out.unmarshall(bytes, 0, bytes.length);
        out.setDataPosition(0);

        final Object unparcelled = callCreator(p, out);

        try {
            assertTrue(String.format("not equals, in: %s, out: %s", p, unparcelled), p.equals(unparcelled));
        } finally {
            in.recycle();
            out.recycle();
        }
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
