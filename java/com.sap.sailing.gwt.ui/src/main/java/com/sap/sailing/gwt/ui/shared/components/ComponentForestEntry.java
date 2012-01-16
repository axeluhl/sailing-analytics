package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.user.client.ui.Widget;

public interface ComponentForestEntry {
    /**
     * @return the name to display to a user for quick navigation to this component
     */
    String getLocalizedShortName();
    
    Widget getEntryWidget();
}
