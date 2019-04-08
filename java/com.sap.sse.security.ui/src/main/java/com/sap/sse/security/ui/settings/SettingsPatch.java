package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.support.SettingsUtil;

/**
 * Interface for a settings patch which may be applied to settings during its construction in
 * {@link UserSettingsBuildingPipelineWithPatching}. The patch may partially or completely modify the resulting settings
 * object in order to provide the desired behavior of default settings for a dynamic environment, e.g. RaceBoard which
 * determines its default settings regarding to RaceModes, PlayModes and etc.
 * 
 * @author Vladislav Chumak
 *
 * @param <S>
 *            The type of the settings to patch
 * 
 * @see ComponentContextWithSettingsStorageAndPatching
 */
public interface SettingsPatch<S extends Settings> {

    /**
     * Implements the logic about how the settings should be modified/patched during its construction inside the
     * settings building pipeline. The settings instance may be modified directly by using
     * {@link SettingsUtil}.
     * 
     * @param settingsToPatch
     *            Current settings in the settings building pipeline
     * @return Modified/patched settings with that the settings building pipeline should continue the settings object
     *         construction
     */
    S patchSettings(S settingsToPatch);

}
