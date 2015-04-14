package com.sap.sailing.gwt.ui.client.shared.components;

import com.sap.sailing.domain.common.settings.Settings;

/**
 * 
 * @author Axel Uhl (d043530)
 * 
 * @param <SettingsType>
 *            the type that describes this component's settings (if any). Use {@link Object} in case your components
 *            doesn't support any settings.
 */
public interface ComponentSettings<SettingsType extends Settings> {
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
}
