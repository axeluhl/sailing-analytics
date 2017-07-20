package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.settings.SettingsRepresentationTransformer;
import com.sap.sse.gwt.settings.SettingsToStringSerializer;
import com.sap.sse.security.ui.client.UserService;

/**
 * Specialization of {@link ComponentContextWithSettingsStorage} which reimplements URL deserialization. This
 * implementation handles URL deserialization in such a way, that only the provided {@code serializedSettings}
 * {@code String} is considered as URL, and not the URL in the browser.
 * 
 * @author Vladislav Chumak
 * 
 * @see ComponentContextWithSettingsStorage
 *
 */
public class PlaceBasedComponentContextWithSettingsStorage<S extends Settings>
        extends ComponentContextWithSettingsStorageAndAdditionalSettingsLayers<S> {

    /**
     * Constructs a special case of {@link ComponentContextWithSettingsStorage} which considers the provided
     * {@code serializedSettings} as URL parameters for deserialization of URL Settings.
     * 
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     * @param userService
     *            The service which is used for server-side settings storage
     * @param storageDefinitionId
     *            The definition for User Settings and Document Settings storage keys
     * @param serializedSettings
     *            Contains URL parameters for serialization of URL Settings
     */
    public PlaceBasedComponentContextWithSettingsStorage(ComponentLifecycle<S> rootLifecycle, UserService userService,
            StoredSettingsLocation storageDefinitionId, final String serializedSettings) {
        this(rootLifecycle, userService, storageDefinitionId, new SettingsRepresentationTransformer() {

            private final SettingsToStringSerializer settingsToStringSerializer = new SettingsToStringSerializer();

            @SuppressWarnings("unchecked")
            @Override
            public <CS extends Settings> CS mergeSettingsObjectWithUrlSettings(CS defaultSettings) {
                final CS result;
                if (defaultSettings instanceof SettingsMap) {
                    result = (CS) settingsToStringSerializer.fromString(serializedSettings,
                            (SettingsMap) defaultSettings);
                } else if (defaultSettings instanceof GenericSerializableSettings) {
                    result = (CS) settingsToStringSerializer.fromString(serializedSettings,
                            (GenericSerializableSettings) defaultSettings);
                } else {
                    result = defaultSettings;
                }
                return result;
            }

        });
    }

    protected PlaceBasedComponentContextWithSettingsStorage(ComponentLifecycle<S> rootLifecycle,
            UserService userService, StoredSettingsLocation storageDefinitionId,
            SettingsRepresentationTransformer settingsRepresentationTransformer) {
        super(rootLifecycle, userService, storageDefinitionId, settingsRepresentationTransformer,
                new UserSettingsBuildingPipelineWithAdditionalSettingsLayers(settingsRepresentationTransformer));
    }
}
