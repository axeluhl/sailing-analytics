package com.sap.sse.security.datamining.data;

import com.sap.sse.common.Util;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.impl.User;

public interface HasUserContext {
    User getUser();
    
    SecurityService getSecurityService();

    @Dimension(messageKey="User")
    default String getName() {
        return getUser().getName();
    }
    
    @Dimension(messageKey="Locale")
    default String getLocale() {
        return getUser().getLocale() != null ? getUser().getLocale().getDisplayName() : "";
    }
    
    @Dimension(messageKey="HasFullName")
    default boolean hasFullName() {
        return Util.hasLength(getUser().getFullName());
    }
    
    @Dimension(messageKey="HasCompany")
    default boolean hasCompany() {
        return Util.hasLength(getUser().getCompany());
    }
    
    @Dimension(messageKey="Company")
    default String getCompany() {
        return getUser().getCompany();
    }
    
    @Dimension(messageKey="HasEmail")
    default boolean hasEmail() {
        return Util.hasLength(getUser().getEmail());
    }
    
    @Dimension(messageKey="IsEmailValidated")
    default boolean isEmailValidated() {
        return getUser().isEmailValidated();
    }
    
    @Statistic(messageKey="NumberOfPermissions")
    default int getNumberOfPermissions() {
        return Util.size(getUser().getPermissions());
    }

    @Statistic(messageKey="NumberOfPreferences")
    default int getNumberOfPreferences() {
        return getSecurityService().getAllPreferences(getUser().getName()).size();
    }
}
