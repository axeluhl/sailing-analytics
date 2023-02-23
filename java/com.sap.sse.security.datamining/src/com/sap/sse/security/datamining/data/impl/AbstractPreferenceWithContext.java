package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.datamining.data.HasPreferenceContext;

public abstract class AbstractPreferenceWithContext implements HasPreferenceContext {
    private final String preferenceName;
    private final String preferenceValue;
    
    public AbstractPreferenceWithContext(String preferenceName, String preferenceValue) {
        this.preferenceName = preferenceName;
        this.preferenceValue = preferenceValue;
    }

    @Override
    public String getPreferenceName() {
        return preferenceName;
    }

    @Override
    public String getPreferenceValue() {
        return preferenceValue;
    }
}
