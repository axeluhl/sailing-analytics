package com.sap.sailing.racecommittee.app.ui.fragments.preference;

import com.sap.sailing.android.shared.ui.fragments.preference.BasePreferenceFragment;
import com.sap.sailing.android.shared.ui.views.EditSetPreference;
import com.sap.sailing.racecommittee.app.R;

import android.os.Bundle;

public class CourseDesignerPreferenceFragment extends BasePreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_course_designer);
        
        setupCourseDesignerByNameCourseNames();
        
        bindPreferenceSummaryToSet(findPreference(R.string.preference_course_designer_by_name_course_names_key));
    }

    private void setupCourseDesignerByNameCourseNames() {
        EditSetPreference preference = findPreference(R.string.preference_course_designer_by_name_course_names_key);
        preference.setExampleValues(getResources().getStringArray(R.array.preference_course_designer_by_name_course_names_example));
    }
}
