package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.TrackingActivity;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class SpeedFragment extends BaseFragment {

    //private String TAG = SpeedFragment.class.getName();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_speed, container, false);
        return view;
    }

    public void setSpeed(float speedInMetersPerSecond) {
        if (isAdded()) {
            float speedInKnots = speedInMetersPerSecond * 1.9438444924574f;

            NumberFormat df = DecimalFormat.getInstance();
            df.setMinimumFractionDigits(0);
            df.setMaximumFractionDigits(2);
            df.setRoundingMode(RoundingMode.HALF_UP);
            String formattedSpeed = df.format(speedInKnots);

            TextView speedText = (TextView) getActivity().findViewById(R.id.speed_text_view);
            speedText.setText(formattedSpeed);
            TrackingActivity activity = (TrackingActivity) getActivity();

            if (activity != null) {
                activity.lastSpeedIndicatorText = speedText.getText().toString();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView speedText = (TextView) getActivity().findViewById(R.id.speed_text_view);

        TrackingActivity activity = (TrackingActivity) getActivity();
        if (activity != null) {
            speedText.setText(activity.lastSpeedIndicatorText);
        } else {
            speedText.setText("0");
        }

    }
}