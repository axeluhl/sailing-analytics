package com.sap.sailing.android.tracking.app.ui.fragments;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.TrackingActivity;
import com.sap.sailing.domain.common.Speed;

public class SpeedFragment extends BaseFragment {

    // private String TAG = SpeedFragment.class.getName();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_speed, container, false);
        return view;
    }

    public void setSpeed(Speed speedInMetersPerSecond) {
        if (isAdded()) {
            String speedIndicatorText;
            TextView speedText = (TextView) getActivity().findViewById(R.id.speed_text_view);
            if (speedInMetersPerSecond != null) {
                String formattedSpeed;
                double speedInKnots = speedInMetersPerSecond.getKnots();

                NumberFormat df = DecimalFormat.getInstance();
                df.setMinimumFractionDigits(0);
                df.setMaximumFractionDigits(2);
                df.setRoundingMode(RoundingMode.HALF_UP);
                formattedSpeed = df.format(speedInKnots);

                speedIndicatorText = getString(R.string.knots, formattedSpeed);
            } else {
                speedIndicatorText = getString(R.string.knots, getString(R.string.initial_hyphen));
            }
            speedText.setText(speedIndicatorText);
            TrackingActivity activity = (TrackingActivity) getActivity();

            if (activity != null) {
                activity.lastSpeedIndicatorText = speedIndicatorText;
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView speedText = (TextView) getActivity().findViewById(R.id.speed_text_view);

        TrackingActivity activity = (TrackingActivity) getActivity();
        if (activity != null) {
            speedText.setText(getString(R.string.knots,activity.lastSpeedIndicatorText));
        } else {
            speedText.setText(getString(R.string.knots,"0"));
        }

    }
}