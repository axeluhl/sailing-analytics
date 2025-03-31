package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.SettingsRepresentationTransformer;
import com.sap.sse.gwt.client.shared.settings.StorableSettingsRepresentation;

public class AdditionalSettingsLayer<S extends Settings> implements SettingsPatch<S> {
    
    private final StorableSettingsRepresentation layerSettingsRepresentation;
    private final SettingsRepresentationTransformer settingsRepresentationTransformer;

    public AdditionalSettingsLayer(S layerSettings, SettingsRepresentationTransformer settingsRepresentationTransformer) {
        this.layerSettingsRepresentation = settingsRepresentationTransformer.convertToSettingsRepresentation(layerSettings);
        this.settingsRepresentationTransformer = settingsRepresentationTransformer;
    }

    @Override
    public S patchSettings(S settingsToPatch) {
        return settingsRepresentationTransformer.mergeSettingsObjectWithStorableRepresentation(settingsToPatch, layerSettingsRepresentation);
    }

}
