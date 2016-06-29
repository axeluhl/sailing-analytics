package com.sap.sailing.gwt.home.desktop.app;

import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.ResettableNavigationPathDisplay;

/**
 * In sailing there are two kinds of activities: Ones with header and ones without. Because the header has a gray
 * background, the {@link NavigationPathDisplay} needs to be set to the exact same background. Activities without header
 * have white background, so we need to switch this based on the Activity type.
 */
public interface DesktopResettableNavigationPathDisplay extends ResettableNavigationPathDisplay {
    void setWithHeader(boolean withHeader);
}
