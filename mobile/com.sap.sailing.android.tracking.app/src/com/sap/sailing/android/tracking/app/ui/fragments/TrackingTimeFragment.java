package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sap.sailing.android.tracking.app.R;

public class TrackingTimeFragment extends BaseFragment {

    private final static String SIS_TRACKING_TIMER = "savedInstanceTrackingTimer";
    private TextView timerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_time, container, false);
        timerView = (TextView) view.findViewById(R.id.tracking_time_label);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (timerView != null) {
            outState.putString(SIS_TRACKING_TIMER, timerView.getText().toString());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && timerView != null) {
            timerView.setText(savedInstanceState.getString(SIS_TRACKING_TIMER));
        }
    }
}
