package com.sap.sailing.android.tracking.app.ui.fragments;

import com.sap.sailing.android.tracking.app.utils.AppPreferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends com.sap.sailing.android.shared.ui.fragments.BaseFragment {

    protected AppPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = new AppPreferences(getActivity());
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
