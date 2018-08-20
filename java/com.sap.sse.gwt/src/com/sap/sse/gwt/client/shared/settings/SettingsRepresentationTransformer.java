package com.sap.sse.gwt.client.shared.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.settings.SettingsToJsonSerializerGWT;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;

/**
 * A helper class for merging of settings objects with stored settings representations. The class is supposed to be used
 * by {@link SettingsBuildingPipeline} for transformation between settings object and stored settings representation.
 * 
 * @author Vladislav Chumak
 * 
 * 
 */
public class SettingsRepresentationTransformer {

    private final SettingsToUrlSerializer urlSerializer = new SettingsToUrlSerializer();
    private final SettingsToJsonSerializerGWT jsonSerializer = new SettingsToJsonSerializerGWT();

    /**
     * Takes the provided {@code settingsObject} and applies on it settings which are specified in current URL.
     * 
     * @param settingsObject
     *            The settings object with is going to be patched by settings from URL
     * @return The new settings object which has been merged with URL settings
     */
    @SuppressWarnings("unchecked")
    public <S extends Settings> S mergeSettingsObjectWithUrlSettings(S settingsObject) {
        S effectiveSettings = settingsObject;
        if (effectiveSettings instanceof GenericSerializableSettings) {
            effectiveSettings = (S) urlSerializer
                    .deserializeFromCurrentLocation((GenericSerializableSettings) effectiveSettings);
        } else if (effectiveSettings instanceof SettingsMap) {
            effectiveSettings = (S) urlSerializer
                    .deserializeSettingsMapFromCurrentLocation((SettingsMap) effectiveSettings);
        }
        return effectiveSettings;
    }

    /**
     * Takes the provided {@code settingsObject} and applies on it settings which are specified in the provided
     * {@code settingsRepresentation}.
     * 
     * @param settingsObject
     *            The settings object with is going to be patched by settings from provided
     *            {@code settingsRepresentation}
     * @param settingsRepresentation
     *            The settings representation which is used to patched the provided {@code settingsObject}
     * @return The new settings object which has been merged with provided {@code settingsRepresentation}
     */
    @SuppressWarnings("unchecked")
    public <S extends Settings> S mergeSettingsObjectWithStorableRepresentation(S settingsObject,
            StorableSettingsRepresentation settingsRepresentation) {
        S effectiveSettings = settingsObject;
        if (effectiveSettings instanceof GenericSerializableSettings) {
            effectiveSettings = (S) jsonSerializer.deserialize((GenericSerializableSettings) effectiveSettings,
                    settingsRepresentation.asJson());
        } else if (effectiveSettings instanceof SettingsMap) {
            effectiveSettings = (S) jsonSerializer.deserialize((SettingsMap) effectiveSettings,
                    settingsRepresentation.asJson());
        }
        return effectiveSettings;
    }

    /**
     * Transforms a settings object into a storable settings representation.
     * 
     * @param newSettings
     *            The settings object to transform
     * @return The transformed storable settings representation
     */
    public StorableSettingsRepresentation convertToSettingsRepresentation(Settings newSettings) {
        if (newSettings instanceof GenericSerializableSettings) {
            return new StorableSettingsRepresentation(
                    jsonSerializer.serialize((GenericSerializableSettings) newSettings));
        }
        throw new IllegalStateException("Requested save of settings that is not Serializable!");
    }

}
