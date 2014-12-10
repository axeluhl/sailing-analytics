package com.sap.sailing.racecommittee.app.ui.fragments;

import java.io.Serializable;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataStore;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;

public class WelcomeFragment extends Fragment implements OnClickListener {

    private static CourseArea mCourseArea;
    private static EventBase mEvent;
    private static RaceLogEventAuthor mEventAuthor;

    public WelcomeFragment() {
        // needed for screen rotation
    }

    public WelcomeFragment(DataStore dataStore, Serializable courseAreaId, Serializable eventId,
            RaceLogEventAuthor raceLogEventAuthor) {
    	ExLog.i(getActivity(), this.getClass().toString(), "eventId: "+ eventId);
        mCourseArea = dataStore.getCourseArea(courseAreaId);
        mEvent = dataStore.getEvent(eventId);
        mEventAuthor = raceLogEventAuthor;
    }

    @Override
    public void onClick(View v) {
        ((RacingActivity) getActivity()).logout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.welcome_fragment, container, false);

        TextView event_name = ViewHolder.get(view, R.id.event_name);
        if (event_name != null) {
            event_name.setText(mEvent.getName());
        }
        TextView area_name = ViewHolder.get(view, R.id.area_name);
        if (area_name != null) {
            area_name.setText(mCourseArea.getName());
        }
        TextView position = ViewHolder.get(view, R.id.position);
        if (position != null) {
            position.setText(mEventAuthor.getName());
        }

        Button logout = ViewHolder.get(view, R.id.logout);
        if (logout != null) {
            logout.setOnClickListener(this);
        }

        return view;
    }
}
