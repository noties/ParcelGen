package ru.noties.parcelable;

import android.os.Bundle;
import android.os.Parcel;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by Dimitry Ivanov on 17.10.2015.
 */
public class BundleUtils {

    private BundleUtils() {}

    // no support for arrays of any type
    public static boolean equals(Bundle left, Bundle right) {

        if (left == right) {
            return true;
        }

        if (left == null
                || right == null) {
            return false;
        }

        final Set<String> leftSet   = left.keySet();
        final Set<String> rightSet  = right.keySet();

        if (leftSet.size() != rightSet.size()) {
            return false;
        }

        Object leftValue;
        Object rightValue;

        for (String key: leftSet) {

            if (!rightSet.contains(key)) {
                return false;
            }

            leftValue   = left.get(key);
            rightValue  = right.get(key);

            if (leftValue instanceof Bundle) {

                if (!(rightValue instanceof Bundle)) {
                    return false;
                }

                if (!BundleUtils.equals((Bundle) leftValue, (Bundle) rightValue)) {
                    return false;
                }
            }

            if (leftValue != null ? !leftValue.equals(rightValue) : rightValue != null) {
                return false;
            }

        }

        return true;
    }

    public static String dump(Bundle bundle) {
        if (bundle == null) {
            return "null";
        }

        final StringBuilder builder = new StringBuilder("Bundle{");

        boolean isFirst = true;
        Object val;

        for (String key: bundle.keySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(", ");
            }
            builder.append(key)
                    .append(": ");
            val = bundle.get(key);
            if (val instanceof Bundle) {
                builder.append(BundleUtils.dump((Bundle) val));
            } else {
                builder.append(String.valueOf(val));
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static Bundle mutate(Bundle bundle) {

        if (bundle == null) {
            return null;
        }

        final Parcel in = Parcel.obtain();
        in.writeBundle(bundle);
        final byte[] bytes = in.marshall();

        final Parcel out = Parcel.obtain();
        out.unmarshall(bytes, 0, bytes.length);
        out.setDataPosition(0);

        try {
            return out.readBundle();
        } finally {
            in.recycle();
            out.recycle();
        }
    }
}
