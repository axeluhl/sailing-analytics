package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.user.client.ui.Widget;

public interface Component<SettingsType> extends ComponentSettings<SettingsType> {
    /**
     * @return the name to display to a user for quick navigation to this component
     */
    String getLocalizedShortName();
    
    /**
     * A component may or may not have a {@link Widget} used to render its contents. Typically,
     * entries with sub-entries don't afford a widget of their own but let the framework choose a container
     * in which to aggregate their entries' widgets.
     */
    Widget getEntryWidget();
    
    boolean isVisible();
    
    void setVisible(boolean visibility);
}
