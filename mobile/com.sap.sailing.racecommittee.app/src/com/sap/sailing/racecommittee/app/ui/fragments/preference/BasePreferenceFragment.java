package com.sap.sailing.racecommittee.app.ui.fragments.preference;

import com.sap.sailing.racecommittee.app.utils.MultiplePreferenceChangeListener;

import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by I074137 on 18.09.13.
 */
public class BasePreferenceFragment extends PreferenceFragment {

    protected static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
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

            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    protected static void bindPreferenceSummaryToValue(Preference preference) {
        addOnPreferenceChangeListener(preference, sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
    
    protected static void bindPreferenceSummaryToInteger(Preference preference) {
        addOnPreferenceChangeListener(preference, sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getInt(preference.getKey(), 0));
    }

    protected static void bindPreferenceToCheckbox(CheckBoxPreference checkboxPreference, final Preference target) {
        target.setEnabled(checkboxPreference.isChecked());
        addOnPreferenceChangeListener(checkboxPreference, new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                target.setEnabled((Boolean) value);
                return true;
            }
        });
    }
    
    protected static void addOnPreferenceChangeListener(Preference preference, OnPreferenceChangeListener newListener) {
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
