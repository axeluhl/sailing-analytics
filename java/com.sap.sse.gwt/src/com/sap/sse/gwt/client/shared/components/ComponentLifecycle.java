package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;

/**
 * A {@link ComponentLifecycle} decouples the lifecycle of a component from the corresponding component instance. This way we
 * can create, e.g., settings for a component with a settings dialog without the need to instantiate the component.
 * 
 * @param <S>
 *            the Settings type
 * @param <SDC>
 *            the SettingsDialogComponent type
 * @author Frank Mittag
 */
public interface ComponentLifecycle<S extends Settings, SDC extends SettingsDialogComponent<S>> {

    SDC getSettingsDialogComponent(S settings);

    S createDefaultSettings();

    S cloneSettings(S settings);
    
    /**
     * @return the display name of the component
     */
    String getLocalizedShortName();
    
    /**
     * @return true if the component has settings that a user may change. 
     */
    boolean hasSettings();

}