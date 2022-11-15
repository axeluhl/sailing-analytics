package com.sap.sailing.racecommittee.app.ui.fragments.lists.selection;

import android.support.v4.app.Fragment;

public interface ItemSelectedListener<T> {
    void itemSelected(Fragment sender, T item);
}
