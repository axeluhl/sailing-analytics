package com.sap.sailing.gwt.ui.client.shared.components;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.settings.Settings;

public interface Component<SettingsType extends Settings> extends ComponentSettings<SettingsType> {
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
    
    /**
     * If the component wants to add its specific styling rules, e.g., for the expand/collapse toggle buttons or
     * the dragger handle, it should return a non-<code>null</code> value from this method which is then used as
     * a dependent CSS class name for those components.
     */
    String getDependentCssClassName();
}
