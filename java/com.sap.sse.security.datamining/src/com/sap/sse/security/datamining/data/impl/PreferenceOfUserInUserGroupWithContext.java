package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.datamining.data.HasPreferenceOfUserInUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserInUserGroupContext;

public class PreferenceOfUserInUserGroupWithContext extends AbstractPreferenceWithContext implements HasPreferenceOfUserInUserGroupContext {
    private final HasUserInUserGroupContext userInUserGroupContext;
    
    public PreferenceOfUserInUserGroupWithContext(HasUserInUserGroupContext userInUserGroupContext, String preferenceName, String preferenceValue) {
        super(preferenceName, preferenceValue);
        this.userInUserGroupContext = userInUserGroupContext;
    }
    
    @Override
    public HasUserInUserGroupContext getUserInUserGroupContext() {
        return userInUserGroupContext;
    }
}
