package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.gwt.client.shared.settings.SettingsBuildingPipeline;
import com.sap.sse.gwt.client.shared.settings.SettingsRepresentationTransformer;
import com.sap.sse.gwt.client.shared.settings.StorableRepresentationOfDocumentAndUserSettings;
import com.sap.sse.gwt.client.shared.settings.StorableSettingsRepresentation;

/**
 * Settings building pipeline which is only capable of reading settings from URL. Conversion to stored settings
 * representation is not supported. This implementation is supposed to be used by {@link ComponentContext} which offers
 * read-only functionality for settings, and thus, does not provide persistence support.
 * 
 * 
 * @author Vladislav Chumak
 *
 */
public class UrlSettingsBuildingPipeline implements SettingsBuildingPipeline {

    /**
     * Conversion helper which is used by this instance for type conversion/serialization between settings objects and
     * storable settings representation.
     */
    protected final SettingsRepresentationTransformer settingsRepresentationTransformer;

    /**
     * Constructs an instance with a custom conversion helper between settings objects and its storable settings representation.
     * 
     * @param settingsRepresentationTransformer
     *            The custom conversion helper
     */
    public UrlSettingsBuildingPipeline(SettingsRepresentationTransformer settingsRepresentationTransformer) {
        this.settingsRepresentationTransformer = settingsRepresentationTransformer;
    }

    /**
     * Constructs a settings object by means of provided {@code systemDefaultSettings} and current URL.
     * 
     * @param systemDefaultSettings
     *            The basic settings to be used
     * @param settingsRepresentation
     *            The stored representation of Settings, which is ignored by this implementation
     * @return The constructed settings object
     */
    @Override
    public <S extends Settings> S getSettingsObject(S systemDefaultSettings,
            StorableRepresentationOfDocumentAndUserSettings settingsRepresentation) {
        return settingsRepresentationTransformer.mergeSettingsObjectWithUrlSettings(systemDefaultSettings);
    }

    /**
     * This implementation does not provide support for conversion of settings objects to storable settings
     * representation, because it is supposed to be used by read-only {@link ComponentContext} implementations.
     */
    @Override
    public <CS extends Settings> StorableSettingsRepresentation getStorableRepresentationOfUserSettings(CS newSettings, CS newInstance, Iterable<String> path) {
        throw new UnsupportedOperationException("This pipeline does not support JSON conversion");
    }
    
    /**
     * This implementation does not provide support for conversion of settings objects to storable settings
     * representation, because it is supposed to be used by read-only {@link ComponentContext} implementations.
     */
    @Override
    public <CS extends Settings> StorableSettingsRepresentation getStorableRepresentationOfDocumentSettings(CS newSettings, CS newInstance, StorableRepresentationOfDocumentAndUserSettings previousSettingsRepresentation, Iterable<String> path) {
        throw new UnsupportedOperationException("This pipeline does not support JSON conversion");
    }

}
