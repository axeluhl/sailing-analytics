package com.sap.sailing.android.shared.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import com.sap.sailing.android.shared.R;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.adapters.ArrayRemoveAdapter;

public class EditSetPreference extends DialogPreference {

    private static final String TAG = EditSetPreference.class.getName();
    
    private static final List<String> defaultFallbackValue = new ArrayList<String>();
    private List<String> currentValue;
    
    private List<String> exampleValues;
    
    public EditSetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.exampleValues = new ArrayList<String>();
        setDialogLayoutResource(R.layout.edit_set_preference);
    }

    public void setExampleValues(String[] values) {
        this.exampleValues = Arrays.asList(values);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        
        final AutoCompleteTextView inputView = (AutoCompleteTextView) view.findViewById(R.id.edit_set_preference_input);
        final Button addButton = (Button) view.findViewById(R.id.edit_set_preference_add);
        final ListView listView = (ListView) view.findViewById(R.id.edit_set_preference_list);
        
        // setup list view
        final ArrayRemoveAdapter<String> adapter = new ArrayRemoveAdapter<String>(view.getContext(), currentValue);
        listView.setEmptyView(view.findViewById(R.id.edit_set_preference_list_empty));
        listView.setAdapter(adapter);
        
        // setup inputView
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_dropdown_item_1line, exampleValues);
        inputView.setAdapter(autoCompleteAdapter);
        inputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                boolean invalid = s.length() == 0 || currentValue.contains(s.toString());
                addButton.setEnabled(!invalid);
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        });
        
        // setup add button
        addButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                Editable text = inputView.getText();
                adapter.add(text.toString());
                text.clear();
            }
        });
        
        return view;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            currentValue = getPersistedStringSet(defaultFallbackValue);
        } else {
            // Set default state from the XML attribute
            currentValue = new ArrayList<String>((Set<String>) defaultValue);
            persistStringSet(currentValue);
        }
    }
    
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        Set<String> defaultValue = new HashSet<String>();
        for (CharSequence chars : a.getTextArray(index)) {
            defaultValue.add(chars.toString());
        }
        return defaultValue;
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            if (callChangeListener(new HashSet<String>(currentValue))) {
                ExLog.i(getContext(), TAG, "Storing result...");
                persistStringSet(currentValue);
            }
        }
    }

    private boolean persistStringSet(List<String> value) {
        if (shouldPersist()) {
            // Shouldn't store null
            if (value == getPersistedStringSet(null)) {
                // It's already there, so the same as persisting
                return true;
            }
            
            SharedPreferences.Editor editor = getEditor();
            editor.putStringSet(getKey(), new HashSet<String>(value));
            if (shouldCommit()) {
                editor.commit();
            }
            
            return true;
        }
        return false;
    }
    
    protected List<String> getPersistedStringSet(List<String> defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        
        Set<String> fallbackDefault = defaultReturnValue == null ? null : new HashSet<String>(defaultReturnValue);
        Set<String> value = getSharedPreferences().getStringSet(getKey(), fallbackDefault);
        
        return value == null ? null : new ArrayList<String>(value);
    }


}
