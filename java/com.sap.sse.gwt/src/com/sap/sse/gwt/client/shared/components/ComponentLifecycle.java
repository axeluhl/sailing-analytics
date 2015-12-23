package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;

/**
 * @author Frank
 *
 * @param <C> the component type
 * @param <S> the settings type
 * @param <W> the settings dialog component type
 */
public interface ComponentLifecycle<C extends Component<S>, /*A extends ComponentConstructorArgs<C, S>,*/ S extends Settings, SDC extends SettingsDialogComponent<S>> {

    SDC getSettingsDialogComponent(S settings);

//    C getOrCreateComponent(A contructorArgs);

    C getComponent();

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