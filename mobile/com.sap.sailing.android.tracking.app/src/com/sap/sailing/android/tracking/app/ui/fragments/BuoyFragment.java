package com.sap.sailing.android.tracking.app.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.android.tracking.app.R;

public class BuoyFragment extends BaseFragment {

    private static String EXTRA_MARK_NAME = "mark_name";

    public static BuoyFragment newInstance(String markName) {
        BuoyFragment fragment = new BuoyFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_MARK_NAME, markName);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buoy, container, false);
        TextView markNameText = ViewHelper.get(view, R.id.buoy_Name);
        markNameText.setText(getArguments().getString(EXTRA_MARK_NAME));
        return view;
    }
}
