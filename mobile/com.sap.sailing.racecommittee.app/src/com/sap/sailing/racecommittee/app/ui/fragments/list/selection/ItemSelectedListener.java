package com.sap.sailing.racecommittee.app.ui.fragments.list.selection;

import android.app.Fragment;

public interface ItemSelectedListener<T> {
	public void itemSelected(Fragment sender, T item);
}
