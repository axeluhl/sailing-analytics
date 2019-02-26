package com.sap.sailing.android.shared.ui.fragments.preference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.android.shared.R;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.adapters.ArrayRemoveAdapter;
import com.sap.sailing.android.shared.ui.views.EditSetPreference;

import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

public class EditSetPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private static final String TAG = EditSetPreferenceDialogFragmentCompat.class.getName();

    private Set<String> currentValue;
    private Set<String> exampleValues;

    public static EditSetPreferenceDialogFragmentCompat newInstance(Preference preference) {
        EditSetPreferenceDialogFragmentCompat fragment = new EditSetPreferenceDialogFragmentCompat();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        final AutoCompleteTextView inputView = (AutoCompleteTextView) view.findViewById(R.id.edit_set_preference_input);
        final Button addButton = (Button) view.findViewById(R.id.edit_set_preference_add);
        final ListView listView = (ListView) view.findViewById(R.id.edit_set_preference_list);

        DialogPreference preference = getPreference();
        if (preference instanceof EditSetPreference) {
            currentValue = ((EditSetPreference) preference).getCurrentValue();
            exampleValues = ((EditSetPreference) preference).getExampleValues();
        }

        // setup list view
        final ArrayRemoveAdapter<String> adapter = new ArrayRemoveAdapter<String>(view.getContext(),
                new ArrayList<String>(currentValue));
        listView.setEmptyView(view.findViewById(R.id.edit_set_preference_list_empty));
        listView.setAdapter(adapter);

        // setup inputView
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_dropdown_item_1line, new ArrayList<String>(exampleValues));
        inputView.setAdapter(autoCompleteAdapter);
        inputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                boolean invalid = s.length() == 0 || currentValue.contains(s.toString());
                addButton.setEnabled(!invalid);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        // setup add button
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Editable text = inputView.getText();
                adapter.add(text.toString());
                text.clear();
            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        DialogPreference preference = getPreference();
        if (positiveResult && (preference instanceof EditSetPreference)
                && preference.callChangeListener(new HashSet<String>(currentValue))) {
            ExLog.i(getContext(), TAG, "Storing result...");
            preference.persistStringSet(currentValue);
        }
    }
}
