package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sap.sailing.android.tracking.app.utils.AppPreferences;

public abstract class BaseFragment extends Fragment {

    protected AppPreferences prefs;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = new AppPreferences(getActivity());
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
