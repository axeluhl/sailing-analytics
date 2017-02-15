package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

/**
 * Receives initial/default settings when {@link ComponentContextWithSettingsStorage} have been initialised.
 * 
 * @author Vladislav Chumak
 *
 * @param <S> The {@link Settings} type of the settings of the root component/perspective containing all the settings for itself and its subcomponents
 * @see ComponentContextWithSettingsStorage
 */
public interface SettingsReceiverCallback<S extends Settings> {
    void receiveSettings(S initialSettings);
}
