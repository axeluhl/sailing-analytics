package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.datamining.data.HasPreferenceOfUserContext;
import com.sap.sse.security.datamining.data.HasUserContext;

public class PreferenceOfUserWithContext extends AbstractPreferenceWithContext implements HasPreferenceOfUserContext {
    private final HasUserContext userContext;
    
    public PreferenceOfUserWithContext(HasUserContext userContext, String preferenceName, String preferenceValue) {
        super(preferenceName, preferenceValue);
        this.userContext = userContext;
    }
    
    @Override
    public HasUserContext getUserContext() {
        return userContext;
    }
}
