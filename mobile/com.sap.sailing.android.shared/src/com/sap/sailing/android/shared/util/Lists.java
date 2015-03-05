package com.sap.sailing.android.shared.util;

import java.util.ArrayList;
import java.util.Collections;

import android.util.SparseArray;

public class Lists {

    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }

    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) {
        int capacity = (elements.length * 110) / 100 + 5;
        ArrayList<E> list = new ArrayList<E>(capacity);
        Collections.addAll(list, elements);
        return list;
    }

    /** Clones a SparseArray. */
    public static <E> SparseArray<E> cloneSparseArray(SparseArray<E> orig) {
        SparseArray<E> result = new SparseArray<E>();
        for (int i = 0; i < orig.size(); i++) {
            result.put(orig.keyAt(i), orig.valueAt(i));
        }
        return result;
    }
}
