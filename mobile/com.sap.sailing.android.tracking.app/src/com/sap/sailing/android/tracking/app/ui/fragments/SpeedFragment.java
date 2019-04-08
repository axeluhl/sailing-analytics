package com.sap.sailing.android.tracking.app.ui.fragments;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.TrackingActivity;
import com.sap.sse.common.Speed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SpeedFragment extends BaseFragment {

    // private String TAG = SpeedFragment.class.getName();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_speed, container, false);
        return view;
    }

    public void setSpeed(Speed speedInMetersPerSecond) {
        if (isAdded()) {
            final String speedIndicatorTextWithoutUnit;
            TextView speedText = (TextView) getActivity().findViewById(R.id.speed_text_view);
            if (speedInMetersPerSecond != null) {
                double speedInKnots = speedInMetersPerSecond.getKnots();
                NumberFormat df = DecimalFormat.getInstance();
                df.setMinimumFractionDigits(0);
                df.setMaximumFractionDigits(2);
                df.setRoundingMode(RoundingMode.HALF_UP);
                speedIndicatorTextWithoutUnit = df.format(speedInKnots);
            } else {
                speedIndicatorTextWithoutUnit = getString(R.string.initial_hyphen);
            }
            final String speedIndicatorText = getString(R.string.knots, speedIndicatorTextWithoutUnit);
            speedText.setText(speedIndicatorText);
            TrackingActivity activity = (TrackingActivity) getActivity();
            if (activity != null) {
                activity.lastSpeedIndicatorTextWithoutUnit = speedIndicatorTextWithoutUnit;
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TextView speedText = (TextView) getActivity().findViewById(R.id.speed_text_view);
        TrackingActivity activity = (TrackingActivity) getActivity();
        if (activity != null) {
            speedText.setText(getString(R.string.knots, activity.lastSpeedIndicatorTextWithoutUnit));
        } else {
            speedText.setText(getString(R.string.knots, "0"));
        }
    }
}