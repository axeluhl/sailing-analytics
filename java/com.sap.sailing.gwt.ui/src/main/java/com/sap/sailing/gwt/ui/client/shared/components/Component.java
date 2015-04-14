package com.sap.sailing.gwt.ui.client.shared.components;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.settings.Settings;

public interface Component<SettingsType extends Settings> {
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
     * @return whether this component has settings that a user may change; if so, 
     */
    boolean hasSettings();
    
    /**
     * If this component {@link #hasSettings has settings}, this method may return a component for the settings dialog.
     * It will be used to obtain a widget shown in the settings dialog, a validator for the component-specific settings,
     * as well as to produce a result from the widget's state when the settings dialog wants to validate or return the
     * settings.
     */
    SettingsDialogComponent<SettingsType> getSettingsDialogComponent();
    
    /**
     * Updates the settings of this component. Expected to be called when a settings dialog using this component's
     * {@link #getSettingsDialogComponent()} has been confirmed.
     */
    void updateSettings(SettingsType newSettings);

    /**
     * If the component wants to add its specific styling rules, e.g., for the expand/collapse toggle buttons or
     * the dragger handle, it should return a non-<code>null</code> value from this method which is then used as
     * a dependent CSS class name for those components.
     */
    String getDependentCssClassName();
}
