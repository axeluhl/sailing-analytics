package com.sap.sse.security.shared;

/**
 * Settings regarding a user's login preferences may be stored in the user store using the preferences storage. This
 * interface provides the keys to be used for those values. See, e.g., {@code SecurityService.setPreference(...)}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface SecurityPreferences {
    String LAST_LOGON_TENANT_ID_AS_STRING_PREFERENCE_KEY = "sse.security.last_logon_tenant_id";
}
