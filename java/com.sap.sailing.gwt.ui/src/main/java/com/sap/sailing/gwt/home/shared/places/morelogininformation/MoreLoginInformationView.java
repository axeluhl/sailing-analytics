package com.sap.sailing.gwt.home.shared.places.morelogininformation;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface for views containing information about benefits of signing in on SAP Sailing Analytics.
 */
public interface MoreLoginInformationView extends IsWidget {

    /**
     * Show or hide the control leading to register form (usually based on login state).
     * 
     * @param visible
     *            <code>true</code> to show register control, <code>false</code> to hide
     */
    void setRegisterControlVisible(boolean visible);
}
