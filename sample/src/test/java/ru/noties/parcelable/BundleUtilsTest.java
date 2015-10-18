package ru.noties.parcelable;

import android.os.Bundle;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BundleUtilsTest extends TestCase {

    private Bundle mBundle;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        mBundle = new Bundle();
    }

    @Test
    public void testMutate() {

        mBundle.putInt("some_int", 99);
        mBundle.putString("String", "hdsf';sddjsd__ __---");

        final Bundle mutated = BundleUtils.mutate(mBundle);

        assertTrue(mBundle != mutated && BundleUtils.equals(mBundle, mutated));
    }

    @Test
    public void testNulls() {
        assertTrue(assertBundle(null));
    }

    @Test
    public void testBundleEquals1() {
        mBundle.putString("String", "hello");
        mBundle.putInt("int", 123123);
        mBundle.putBoolean("bool", true);
        mBundle.putByte("byte", (byte) 1);

        assertTrue(assertBundle(mBundle));
    }

    @Test
    public void testInnerBundle() {
        final Bundle inner = new Bundle();
        inner.putLong("long", 1234L);
        inner.putFloat("float", 33.3333F);

        mBundle.putString("string", "Stringgg-ngnghg");
        mBundle.putBundle("bundle", inner);

        assertTrue(assertBundle(mBundle));
    }

//    @Test
//    public void testArrays() {
//        mBundle.putFloatArray("float_array", new float[] { 1.F, 0.99999F, 0.0000000012F });
//        mBundle.putIntArray("int_array", new int[]{-1, 6, 7824, Integer.MAX_VALUE});
//
//        assertTrue(assertBundle(mBundle));
//    }

    @Test
    public void testBundleEquals2() {
        mBundle.putString("string", "some string");
        mBundle.putInt("int", 123);
        mBundle.putLong("long", 999L);
        mBundle.putFloat("asddd", .000001F);
        assertBundle(mBundle);
    }

    @Test
    public void testNotEquals() {
        final Bundle left = new Bundle();
        left.putInt("int", 9);
        left.putDouble("double", 1.D);

        final Bundle right = new Bundle();
        right.putInt("int2", 9);
        right.putDouble("double", 1.D);

        assertFalse(BundleUtils.equals(left, right));
    }

    private static boolean assertBundle(Bundle bundle) {
        final Bundle clone = BundleUtils.mutate(bundle);
        return BundleUtils.equals(bundle, clone);
    }
}
