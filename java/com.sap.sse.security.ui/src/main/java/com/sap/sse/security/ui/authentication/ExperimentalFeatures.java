package com.sap.sse.security.ui.authentication;

public interface ExperimentalFeatures {
    /**
     * When a user changes the locale preference setting on the user profile page, all pages will reload with the new
     * locale applied. This is currently inactive due to the fact that users will lose state on refresh. This
     * implementation should be finished when we can serialize state to the URL.
     */
    public static final boolean REFRESH_ON_LOCALE_CHANGE_IN_USER_PROFILE = false;
}
