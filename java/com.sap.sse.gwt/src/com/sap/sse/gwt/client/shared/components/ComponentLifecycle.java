package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

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
public interface ComponentLifecycle<S extends Settings> {
    SettingsDialogComponent<S> getSettingsDialogComponent(S settings);

    S createDefaultSettings();
    
    /**
     * @return the display name of the component
     */
    String getLocalizedShortName();

    /**
     * @return the id of the component
     */
    String getComponentId();

    /**
     * @return true if the component has settings that a user may change. 
     */
    boolean hasSettings();

    /**
     * Extracts User Settings from provided {@link Settings}.
     * 
     * @param settings The settings which belong to the component maintained by this lifecycle
     * @return User Settings extracted from the provided settings
     * @see ComponentContext
     */
    default S extractUserSettings(S settings) {
        return settings;
    }

    /**
     * Extracts Document Settings from provided {@link Settings}.
     * 
     * @param settings The settings which belong to the component maintained by this lifecycle
     * @return Document Settings extracted from the provided settings
     * @see ComponentContext
     */
    default S extractDocumentSettings(S settings) {
        return settings;
    }

}