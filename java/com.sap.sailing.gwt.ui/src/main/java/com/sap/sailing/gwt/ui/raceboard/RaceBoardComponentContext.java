package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.settings.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.settings.ComponentContextWithSettingsStorage;
import com.sap.sse.security.ui.settings.ComponentContextWithSettingsStorageAndAdditionalSettingsLayers;
import com.sap.sse.security.ui.settings.StoredSettingsLocation;

/**
 * A specialization of {@link ComponentContextWithSettingsStorageAndAdditionalSettingsLayers} which is specially
 * designed for handling of RaceModes dependent default settings. This implementation offers convenience methods that
 * can be used by RaceModes to add Mode-specific default settings. Mode-specific default settings are treated as an
 * additional settings layer - mode settings layer. The layer is located between User Settings and Document Settings
 * layers.
 * 
 * 
 * @author Vladislav Chumak
 * @see ComponentContextWithSettingsStorageAndAdditionalSettingsLayers
 * @see ComponentContextWithSettingsStorage
 *
 */
public class RaceBoardComponentContext extends
        ComponentContextWithSettingsStorageAndAdditionalSettingsLayers<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>> {

    /**
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     * @param userService
     *            The service which is used for server-side settings storage
     * @param storageDefinition
     *            The definition for User Settings and Document Settings storage keys
     */
    public RaceBoardComponentContext(RaceBoardPerspectiveLifecycle rootLifecycle, UserService userService,
            StoredSettingsLocation storageDefinition) {
        super(rootLifecycle, userService, storageDefinition);
    }

    /**
     * Adds a mode settings layer with provided layer settings to the corresponding component. A component may have
     * multiple mode layer settings. The effective settings are patched by the mode settings layer in the following way:
     * All settings values of provided {@code additionalLayerSettings} parameter, which are set to a non-default value
     * will override the resulting settings values. When the settings value type is a collection, the values of
     * {@code modeSettings} and the resulting settings will be merged as following (read carefully):
     * <ul>
     * <li>If {@code modeSettings} <b>default values</b> contain a <i>value</i>, which is not contained in the
     * <b>values</b> of {@code modeSettings}, the not contained <i>value</i> gets removed from the <i>resulting settings
     * values</i></li>
     * <li>If {@code additiveSettings} <b>values</b> contain a <i>value</i>, which is not contained in the <b>values</b>
     * of the resulting settings, the not contained value gets added to the <i>resulting settings values</i>
     * </ul>
     * The additional settings layer shows its effect by further calls of:
     * <ul>
     * <li>{@link #getInitialSettings(OnSettingsLoadedCallback)}</li>
     * <li>{@link #getInitialSettingsForComponent(Component, OnSettingsLoadedCallback)}</li>
     * <li>{@link #makeSettingsDefault(Component, Settings, com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback)}</li>
     * <li>{@link #storeSettingsForContext(Component, Settings, com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback)}</li>
     * </ul>
     * 
     * @param component
     *            The targeted component which the provided layer settings belong to
     * @param modeSettings
     *            The mode layer settings to apply
     * @param reloadedCallback
     *            The callback used to accept the reloaded settings after adding the mode settings layer
     */
    public <CS extends GenericSerializableSettings> void addModesPatching(Component<CS> component, CS modeSettings,
            OnSettingsReloadedCallback<CS> patchCallback) {
        super.addAdditionalSettingsLayerForComponent(component, PipelineLevel.SYSTEM_DEFAULTS, modeSettings,
                patchCallback);
    }

}
