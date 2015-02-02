package com.sap.sailing.racecommittee.app.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sap.sailing.android.shared.application.StringContext;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;

public class WelcomeFragment extends Fragment implements OnClickListener {

    private static final String COURSE = "course";
    private static final String EVENT = "event";
    private static final String AUTHOR = "eventAuthor";

    public WelcomeFragment() { }

    public static WelcomeFragment newInstance(EventBase event, CourseArea course,
                              AbstractLogEventAuthor raceLogEventAuthor) {
        WelcomeFragment fragment = new WelcomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(EVENT, event);
        args.putSerializable(COURSE, course);
        args.putSerializable(AUTHOR, raceLogEventAuthor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        RacingActivity activity = (RacingActivity) getActivity();
        if (activity != null) {
            activity.logout();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.welcome_fragment, container, false);

        if (getArguments() != null) {
            TextView event_name = ViewHolder.get(view, R.id.event_name);
            if (event_name != null && getArguments().getSerializable(EVENT) != null) {
                event_name.setText(((EventBase) getArguments().getSerializable(EVENT)).getName());
            }
            TextView area_name = ViewHolder.get(view, R.id.area_name);
            if (area_name != null && getArguments().getSerializable(COURSE) != null) {
                area_name.setText(((CourseArea) getArguments().getSerializable(COURSE)).getName());
            }
            TextView position = ViewHolder.get(view, R.id.position);
            if (position != null && getArguments().getSerializable(AUTHOR) != null) {
                position.setText(((AbstractLogEventAuthor) getArguments().getSerializable(AUTHOR)).getName());
            }
        }

        Button logout = ViewHolder.get(view, R.id.logout);
        if (logout != null) {
            logout.setOnClickListener(this);
        }

        return view;
    }
}
