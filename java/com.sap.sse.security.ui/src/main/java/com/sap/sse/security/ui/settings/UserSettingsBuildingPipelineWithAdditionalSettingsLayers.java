package com.sap.sse.security.ui.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.support.SettingsUtil;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.ComponentUtils;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsRepresentationTransformer;
import com.sap.sse.gwt.client.shared.settings.StorableRepresentationOfDocumentAndUserSettings;
import com.sap.sse.gwt.client.shared.settings.StorableSettingsRepresentation;

/**
 * Specialization of {@link UserSettingsBuildingPipeline} which offers multiple hooks for adding of additional settings
 * layers.
 * 
 * 
 * @author Vladislav Chumak
 *
 */
public class UserSettingsBuildingPipelineWithAdditionalSettingsLayers extends UserSettingsBuildingPipeline {

    private SettingsPatches layersSettingsPatches = new SettingsPatches();

    /**
     * Constructs an instance with a custom conversion helper between settings objects and its storable representation.
     * 
     * @param settingsRepresentationTransformer
     *            The custom conversion helper
     */
    public UserSettingsBuildingPipelineWithAdditionalSettingsLayers(
            SettingsRepresentationTransformer settingsRepresentationTransformer) {
        super(settingsRepresentationTransformer);
    }

    /**
     * Constructs the settings object of the root perspective/component by means of provided
     * {@code systemDefaultSettings} and stored representation of all settings. This method implements the settings
     * construction pipeline for a settings object which is used for settings loading operations. The settings object is
     * constructed on top of provided systemdDefaultSettings considering settings layers in the following order:
     * <ul>
     * <li>Additional settings layers hooked in {@link PipelineLevel PipelineLevel.SYSTEM_DEFAULTS}</li>
     * <li>User Settings</li>
     * <li>Additional settings layers hooked in {@link PipelineLevel PipelineLevel.USER_DEFAULTS}</li>
     * <li>Document Settings</li>
     * <li>Additional settings layers hooked in {@link PipelineLevel PipelineLevel.DOCUMENT_DEFAULTS}</li>
     * <li>URL Settings</li>
     * </ul>
     * 
     * @param systemDefaultSettings
     *            The basic settings to be used
     * @param settingsRepresentation
     *            The stored representations of User Settings and Document Settings
     * @return The constructed settings object
     */
    @Override
    public <CS extends Settings> CS getSettingsObject(CS systemDefaultSettings,
            StorableRepresentationOfDocumentAndUserSettings settingsRepresentations) {
        List<String> rootPath = new ArrayList<>();
        CS effectiveSettings = applyPatchesForPipelineLevel(systemDefaultSettings, PipelineLevel.SYSTEM_DEFAULTS,
                rootPath, layersSettingsPatches);
        if (settingsRepresentations.hasStoredUserSettings()) {
            effectiveSettings = settingsRepresentationTransformer.mergeSettingsObjectWithStorableRepresentation(
                    effectiveSettings, settingsRepresentations.getUserSettingsRepresentation());
        }
        effectiveSettings = applyPatchesForPipelineLevel(effectiveSettings, PipelineLevel.USER_DEFAULTS, rootPath,
                layersSettingsPatches);
        if (settingsRepresentations.hasStoredDocumentSettings()) {
            effectiveSettings = settingsRepresentationTransformer.mergeSettingsObjectWithStorableRepresentation(
                    effectiveSettings, settingsRepresentations.getDocumentSettingsRepresentation());
        }

        effectiveSettings = applyPatchesForPipelineLevel(effectiveSettings, PipelineLevel.DOCUMENT_DEFAULTS, rootPath,
                layersSettingsPatches);
        effectiveSettings = settingsRepresentationTransformer.mergeSettingsObjectWithUrlSettings(effectiveSettings);
        return effectiveSettings;
    }

    protected static <CS extends Settings> CS applyPatchesForPipelineLevel(CS currentSettings,
            PipelineLevel pipelineLevel, Iterable<String> absolutePathOfComponentWithSettings,
            SettingsPatches settingsPatchesToConsider) {
        CS effectiveSettings = currentSettings;
        for (Entry<List<String>, List<SettingsPatch<? extends Settings>>> entry : settingsPatchesToConsider
                .getSettingsPatches(pipelineLevel).entrySet()) {
            List<String> path = new ArrayList<>(entry.getKey());
            if (path.size() >= Util.size(absolutePathOfComponentWithSettings)
                    && path.subList(path.size() - Util.size(absolutePathOfComponentWithSettings), path.size())
                            .equals(absolutePathOfComponentWithSettings)) {
                path = path.subList(0, path.size() - Util.size(absolutePathOfComponentWithSettings));
                List<SettingsPatch<? extends Settings>> settingsPatches = entry.getValue();
                if (!settingsPatches.isEmpty()) {
                    Settings patchedComponentSettings = ComponentUtils
                            .determineComponentSettingsFromPerspectiveSettings(new ArrayList<>(path),
                                    effectiveSettings);
                    for (SettingsPatch<? extends Settings> settingsPatch : settingsPatches) {
                        patchedComponentSettings = patchSettings(patchedComponentSettings, settingsPatch);
                    }
                    effectiveSettings = ComponentUtils.patchSettingsTree(new ArrayList<>(path),
                            patchedComponentSettings, effectiveSettings);
                }
            }
        }
        return effectiveSettings;
    }

    @SuppressWarnings("unchecked")
    private static <CS extends Settings> CS patchSettings(Settings settings, SettingsPatch<CS> settingsPatch) {
        return settingsPatch.patchSettings((CS) settings);
    }

    /**
     * Converts the provided settings according to storable settings representation for User Settings. This method
     * implements the storable settings representation building pipeline which is used for settings storing operations.
     * The diff patch inside the returned settings representation is generated by diff between {@code values} of
     * effective settings after additional settings layers at {@code PipelineLevel.SYSTEM_DEFAULTS} and {@code values}
     * inside the provided {@code newSettings} object.
     * 
     * @param newSettings
     *            The settings to convert to storable representation
     * @param newInstance
     *            A fresh dummy instance of the settings type which will be used as temporary helper (defaultValues are
     *            required, if used for layer patching, values are completely ignored)
     * @param previousSettingsRepresentation
     *            The representation of settings which have been already stored (the whole settings tree)
     * @param path
     *            The settings tree path of provided settings (empty lists means the provided settings belong to the
     *            root component/perspective)
     * @return The storable settings representation of provided settings as User Settings
     */
    @Override
    public <CS extends Settings> StorableSettingsRepresentation getStorableRepresentationOfUserSettings(CS newSettings,
            CS newInstance, Iterable<String> path) {
        CS pipelinedSettings = applyPatchesForPipelineLevel(newInstance, PipelineLevel.SYSTEM_DEFAULTS, path,
                layersSettingsPatches);
        pipelinedSettings = SettingsUtil.copyDefaultsFromValues(pipelinedSettings, pipelinedSettings);

        pipelinedSettings = SettingsUtil.copyValues(newSettings, pipelinedSettings);

        return settingsRepresentationTransformer.convertToSettingsRepresentation(pipelinedSettings);
    }

    /**
     * Converts the provided settings according to storable settings representation for Document Settings. This method
     * implements the storable settings representation building pipeline which is used for settings storing operations.
     * The diff patch inside the returned settings representation is generated by diff between {@code values} of User
     * Settings from provided {@code previousSettingsRepresentation} and {@code values} inside the provided
     * {@code newSettings} object. The diff patch inside the returned settings representation is generated by diff
     * between {@code values} of effective settings after additional settings layers at
     * {@code PipelineLevel.USER_DEFAULTS} and {@code values} inside the provided {@code newSettings} object.
     * 
     * @param newSettings
     *            The settings to convert to storable representation
     * @param newInstance
     *            A fresh dummy instance of the settings type which will be used as temporary helper (defaultValues are
     *            required, if used for layer patching, values are completely ignored)
     * @param previousSettingsRepresentation
     *            The representation of settings which have been already stored (the whole settings tree)
     * @param path
     *            The settings tree path of provided settings (empty lists means the provided settings belong to the
     *            root component/perspective)
     * @return The storable settings representation of provided settings as Document Settings
     */
    @Override
    public <CS extends Settings> StorableSettingsRepresentation getStorableRepresentationOfDocumentSettings(
            CS newSettings, CS newInstance,
            StorableRepresentationOfDocumentAndUserSettings previousSettingsRepresentation, Iterable<String> path) {
        CS pipelinedSettings = applyPatchesForPipelineLevel(newInstance, PipelineLevel.SYSTEM_DEFAULTS, path,
                layersSettingsPatches);
        pipelinedSettings = SettingsUtil.copyDefaultsFromValues(pipelinedSettings, pipelinedSettings);

        if (previousSettingsRepresentation.hasStoredUserSettings()) {
            pipelinedSettings = settingsRepresentationTransformer.mergeSettingsObjectWithStorableRepresentation(
                    pipelinedSettings,
                    previousSettingsRepresentation.getUserSettingsRepresentation().getSubSettingsRepresentation(path));

            pipelinedSettings = SettingsUtil.copyDefaultsFromValues(pipelinedSettings, pipelinedSettings);
        }
        pipelinedSettings = applyPatchesForPipelineLevel(pipelinedSettings, PipelineLevel.USER_DEFAULTS, path,
                layersSettingsPatches);
        
        pipelinedSettings = SettingsUtil.copyDefaultsFromValues(pipelinedSettings, pipelinedSettings);
        pipelinedSettings = SettingsUtil.copyValues(newSettings, pipelinedSettings);
        return settingsRepresentationTransformer.convertToSettingsRepresentation(pipelinedSettings);
    }

    /**
     * Adds an additional settings layer with provided layer settings to the corresponding component. A component may have
     * multiple layer settings. The effective settings are patched by the additional settings layer in the following
     * way: All settings values of provided {@code additionalLayerSettings} parameter, which are set to a non-default
     * value will override the resulting settings values. When the settings value type is a collection, the values of
     * {@code additionalLayerSettings} and the resulting settings will be merged as following (read carefully):
     * <ul>
     * <li>If {@code additionalLayerSettings} <b>default values</b> contain a <i>value</i>, which is not contained in the
     * <b>values</b> of {@code additiveSettings}, the not contained <i>value</i> gets removed from the <i>resulting
     * settings values</i></li>
     * <li>If {@code additionalLayerSettings} <b>values</b> contain a <i>value</i>, which is not contained in the <b>values</b>
     * of the resulting settings, the not contained value gets added to the <i>resulting settings values</i>
     * </ul>
     * The additional settings layer shows its effect by further calls of:
     * <ul>
     * <li>{@link #getSettingsObject(Settings, StorableRepresentationOfDocumentAndUserSettings)}</li>
     * <li>{@link #getStorableRepresentationOfDocumentSettings(Settings, Settings, StorableRepresentationOfDocumentAndUserSettings, List)}</li>
     * <li>{@link #getStorableRepresentationOfUserSettings(Settings, Settings, StorableRepresentationOfDocumentAndUserSettings, List)}</li>
     * </ul>
     * 
     * @param component
     *            The targeted component which the provided layer settings belong to
     * @param afterSettingsLayer
     *            The pipeline level <b>AFTER</b> that the provided layer is going to apply
     * @param additionalLayerSettings
     *            The layer settings to apply on top the effective settings after the settings layer at provided level
     */
    public <CS extends GenericSerializableSettings> void addAdditionalSettingsLayer(Component<CS> component,
            PipelineLevel afterSettingsLayer, CS additionalLayerSettings) {
        layersSettingsPatches.addSettingsPatch(component.getPath(),
                new AdditionalSettingsLayer<Settings>(additionalLayerSettings, settingsRepresentationTransformer),
                afterSettingsLayer);
    }

    /**
     * Internal helper class for management of provided settings patches in relation to its pipeline levels and
     * components that provided patches belong to.
     * 
     * @author Vladislav Chumak
     *
     */
    private static class SettingsPatches {

        private Map<PipelineLevel, Map<List<String>, List<SettingsPatch<? extends Settings>>>> patchesForSettings = new HashMap<>();

        public void addSettingsPatch(Iterable<String> path, SettingsPatch<? extends Settings> settingsPatch,
                PipelineLevel pipelineLevel) {
            Map<List<String>, List<SettingsPatch<? extends Settings>>> pipelinePatches = patchesForSettings
                    .get(pipelineLevel);
            if (pipelinePatches == null) {
                pipelinePatches = new HashMap<>();
                patchesForSettings.put(pipelineLevel, pipelinePatches);
            }
            List<SettingsPatch<? extends Settings>> componentPatches = pipelinePatches.get(path);
            if (componentPatches == null) {
                componentPatches = new ArrayList<>();
                pipelinePatches.put(Util.asList(path), componentPatches);
            }
            componentPatches.add(settingsPatch);
        }

        public Map<List<String>, List<SettingsPatch<? extends Settings>>> getSettingsPatches(
                PipelineLevel pipelineLevel) {
            Map<List<String>, List<SettingsPatch<? extends Settings>>> pipelinePatches = patchesForSettings
                    .get(pipelineLevel);
            if (pipelinePatches == null) {
                return Collections.emptyMap();
            }
            return pipelinePatches;
        }
    }

}