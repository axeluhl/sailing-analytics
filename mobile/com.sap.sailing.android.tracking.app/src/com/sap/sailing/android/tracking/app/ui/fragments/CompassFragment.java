package com.sap.sailing.android.tracking.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.TrackingActivity;
import com.sap.sse.common.Bearing;

public class CompassFragment extends BaseFragment {

    // private String TAG = CompassFragment.class.getName();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_compass, container, false);
        return view;
    }

    public void setBearing(Bearing bearing) {
        if (isAdded()) {
            TextView headingText = (TextView) getActivity().findViewById(R.id.compass_bearing_text_view);
            if (bearing != null) {
                headingText.setText(getString(R.string.course_over_ground_display, Math.round(bearing.getDegrees())));
            } else {
                headingText.setText(R.string.initial_hyphen_degrees);
            }

            TrackingActivity activity = (TrackingActivity) getActivity();

            if (activity != null) {
                activity.lastCompassIndicatorText = headingText.getText().toString();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TextView headingText = (TextView) getActivity().findViewById(R.id.compass_bearing_text_view);

        TrackingActivity activity = (TrackingActivity) getActivity();
        if (activity != null) {
            headingText.setText(activity.lastCompassIndicatorText);
        } else {
            headingText.setText("");
        }
    }
}
