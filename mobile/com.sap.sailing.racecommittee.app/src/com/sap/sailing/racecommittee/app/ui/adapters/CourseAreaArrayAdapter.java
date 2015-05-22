package com.sap.sailing.racecommittee.app.ui.adapters;

import android.content.Context;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.racecommittee.app.AppPreferences;

import java.util.List;

public class CourseAreaArrayAdapter extends NamedArrayAdapter<CourseArea> {

    public CourseAreaArrayAdapter(Context context, List<CourseArea> namedList) {
        super(context, namedList);
    }

    @Override
    public boolean isEnabled(int position) {
        CourseArea courseArea = getItem(position);
        AppPreferences preferences = AppPreferences.on(getContext());
        if (preferences.getManagedCourseAreaNames().contains(courseArea.getName())) {
            return true;
        } else if (preferences.getManagedCourseAreaNames().contains("*")) {
            return true;
        } else {
            return false;
        }
    }
}
