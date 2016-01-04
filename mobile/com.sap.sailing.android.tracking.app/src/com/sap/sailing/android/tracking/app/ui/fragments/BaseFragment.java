package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sap.sailing.android.tracking.app.utils.AppPreferences;

public abstract class BaseFragment extends com.sap.sailing.android.ui.fragments.BaseFragment {

    protected AppPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = new AppPreferences(getActivity());
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
