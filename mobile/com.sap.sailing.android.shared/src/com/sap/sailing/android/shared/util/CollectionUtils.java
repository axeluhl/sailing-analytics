package com.sap.sailing.android.shared.util;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sse.common.Util;

public class CollectionUtils {

    public static <T> ArrayList<T> newArrayList(final Iterable<T> iterable) {
        ArrayList<T> list = new ArrayList<T>();
        Util.addAll(iterable, list);
        return list;
    }

    public static boolean isEqualCollection(final Collection<?> left, final Collection<?> right) {
        if (left.size() != right.size()) {
            return false;
        }

        // There may be better implementations ;-)
        return left.containsAll(right) && right.containsAll(left);
    }

}
