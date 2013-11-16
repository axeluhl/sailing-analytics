package com.sap.sailing.racecommittee.app.ui.fragments.preference;


import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;

import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.racecommittee.app.R;

public class CourseDesignerPreferenceFragment extends BasePreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_course_designer);
        
        setupCourseDesignerTypePreferences();
        bindPreferenceSummaryToInteger(findPreference(getString(R.string.preference_course_designer_by_name_min_rounds_key)));
        bindPreferenceSummaryToInteger(findPreference(getString(R.string.preference_course_designer_by_name_max_rounds_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_course_designer_by_name_course_names_key)));
    }

    private void setupCourseDesignerTypePreferences() {
        CheckBoxPreference overrideCourseDesignerPreference = (CheckBoxPreference) 
                findPreference(getString(R.string.preference_course_designer_is_overridden_key));
        final ListPreference courseDesignerPreference = (ListPreference) 
                findPreference(getString(R.string.preference_course_designer_override_key));
        
        List<CharSequence> entries = new ArrayList<CharSequence>();
        List<CharSequence> entryValues = new ArrayList<CharSequence>();
        for (CourseDesignerMode type : CourseDesignerMode.validValues()) {
            entries.add(type.toString());
            entryValues.add(type.name());
        }
        
        courseDesignerPreference.setEntries(entries.toArray(new CharSequence[0]));
        courseDesignerPreference.setEntryValues(entryValues.toArray(new CharSequence[0]));
        
        bindPreferenceToCheckbox(overrideCourseDesignerPreference, courseDesignerPreference);
        bindPreferenceSummaryToValue(courseDesignerPreference);
    }
}
