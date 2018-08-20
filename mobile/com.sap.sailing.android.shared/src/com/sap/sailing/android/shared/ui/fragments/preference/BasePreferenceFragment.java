package com.sap.sailing.android.shared.ui.fragments.preference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.android.shared.ui.utils.MultiplePreferenceChangeListener;
import com.sap.sailing.android.shared.ui.views.EditSetPreference;

import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

/**
 * Created by I074137 on 18.09.13.
 */
public class BasePreferenceFragment extends PreferenceFragment {
    
    @SuppressWarnings("unchecked")
    protected <T extends Preference> T findPreference(int resourceId) {
        return (T) findPreference(getString(resourceId));
    }
    
    protected Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof MultiSelectListPreference || preference instanceof EditSetPreference) {
                @SuppressWarnings("unchecked")
                Set<String> setValue = (Set<String>) value;
                List<String> listValue = new ArrayList<String>(setValue);
                Collections.sort(listValue);
                preference.setSummary(listValue.toString());
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    protected void bindPreferenceSummaryToValue(Preference preference) {
        addOnPreferenceChangeListener(preference, sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                getPreferenceManager().getSharedPreferences().getString(preference.getKey(), ""));
    }
    
    protected void bindPreferenceSummaryToInteger(Preference preference) {
        addOnPreferenceChangeListener(preference, sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                getPreferenceManager().getSharedPreferences().getInt(preference.getKey(), 0));
    }
    
    protected void bindPreferenceSummaryToSet(Preference preference) {
        addOnPreferenceChangeListener(preference, sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                getPreferenceManager().getSharedPreferences().getStringSet(preference.getKey(), new HashSet<String>()));
    }

    protected void bindPreferenceToCheckbox(CheckBoxPreference checkboxPreference, final Preference target) {
        target.setEnabled(checkboxPreference.isChecked());
        addOnPreferenceChangeListener(checkboxPreference, new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                target.setEnabled((Boolean) value);
                return true;
            }
        });
    }

    protected void bindPreferenceToListEntry(Preference preference, final String defaultValue) {
        final ListPreference listPreference = (ListPreference) preference;
        if (listPreference.getValue() == null) {
            listPreference.setValue(defaultValue);
        }
        listPreference.setSummary(listPreference.getEntry());
        addOnPreferenceChangeListener(preference, new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                int index = listPreference.findIndexOfValue(String.valueOf(newValue));
                if (index < 0) {
                    index = listPreference.findIndexOfValue(defaultValue);
                }
                String summary = listPreference.getEntries()[index].toString();
                pref.setSummary(summary);
                return true;
            }
        });
    }

    protected void addOnPreferenceChangeListener(Preference preference, OnPreferenceChangeListener newListener) {
        OnPreferenceChangeListener oldListener = preference.getOnPreferenceChangeListener();
        if (oldListener == null) {
            MultiplePreferenceChangeListener multiListener = new MultiplePreferenceChangeListener();
            multiListener.addOnPreferenceChangeListener(newListener);
            preference.setOnPreferenceChangeListener(multiListener);
        } else if (oldListener instanceof MultiplePreferenceChangeListener) {
            MultiplePreferenceChangeListener multiListener = (MultiplePreferenceChangeListener) oldListener;
            multiListener.addOnPreferenceChangeListener(newListener);
        } else {
            MultiplePreferenceChangeListener multiListener = new MultiplePreferenceChangeListener();
            multiListener.addOnPreferenceChangeListener(oldListener);
            multiListener.addOnPreferenceChangeListener(newListener);
            preference.setOnPreferenceChangeListener(multiListener);
        }
    }
}
