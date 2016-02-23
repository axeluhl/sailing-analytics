package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;

/**
 * @author Frank
 * A ComponentLifecycle decouples the lifecycle of a component from the corresponding component instance.
 * This way we can create e.g. settings for a component with a settings dialog without the need to instantiate the component.
 * @param <C> the component type
 * @param <S> the settings type
 * @param <SDC> the settings dialog component type
 */
public interface ComponentLifecycle<C extends Component<S>, S extends Settings, SDC extends SettingsDialogComponent<S>> {

    SDC getSettingsDialogComponent(S settings);

    S createDefaultSettings();

    S cloneSettings(S settings);
    
    /**
     * @return the display name of the component
     */
    String getLocalizedShortName();
    
    /**
     * @return whether this component has settings that a user may change. 
     */
    boolean hasSettings();

}