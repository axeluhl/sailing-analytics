package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;

import android.content.Context;

public class PreferencesDeviceConfigurationLoader implements ConfigurationLoader<DeviceConfiguration> {

    private static final String TAG = PreferencesDeviceConfigurationLoader.class.getSimpleName();

    private final DeviceConfigurationImpl configuration;
    private final AppPreferences preferences;
    private final ConfigurationLoader<RegattaConfiguration> regattaConfigurationLoader;

    public static PreferencesDeviceConfigurationLoader wrap(DeviceConfiguration configuration,
            AppPreferences preferences) {
        RegattaConfiguration regattaConfiguration = configuration.getRegattaConfiguration();
        if (regattaConfiguration == null) {
            regattaConfiguration = new RegattaConfigurationImpl();
        }
        return new PreferencesDeviceConfigurationLoader(configuration,
                new PreferencesRegattaConfigurationLoader(regattaConfiguration, preferences), preferences);
    }

    private PreferencesDeviceConfigurationLoader(DeviceConfiguration configuration,
            PreferencesRegattaConfigurationLoader regattaLoader, AppPreferences preferences) {
        if (!(configuration instanceof DeviceConfigurationImpl)) {
            throw new IllegalArgumentException("configuration");
        }
        this.configuration = (DeviceConfigurationImpl) configuration;
        this.preferences = preferences;
        this.regattaConfigurationLoader = regattaLoader;
    }

    @Override
    public DeviceConfiguration load() {
        if (regattaConfigurationLoader != null) {
            configuration.setRegattaConfiguration(regattaConfigurationLoader.load());
        }

        configuration.setAllowedCourseAreaNames(preferences.getManagedCourseAreaNames());
        configuration.setResultsMailRecipient(preferences.getMailRecipient());
        configuration.setByNameDesignerCourseNames(preferences.getByNameCourseDesignerCourseNames());
        return configuration.copy();
    }

    @Override
    public void store() {
        Context context = preferences.getContext();
        ExLog.i(context, TAG, "Storing new device configuration.");

        if (regattaConfigurationLoader != null) {
            regattaConfigurationLoader.store();
            logApply(context, "regatta configuration", "[object]");
        }

        if (configuration.getAllowedCourseAreaNames() != null) {
            preferences.setManagedCourseAreaNames(configuration.getAllowedCourseAreaNames());
            logApply(context, "course areas", configuration.getAllowedCourseAreaNames());
        }
        if (configuration.getResultsMailRecipient() != null) {
            preferences.setMailRecipient(configuration.getResultsMailRecipient());
            logApply(context, "mail recipient", configuration.getResultsMailRecipient());
        }
        if (configuration.getByNameCourseDesignerCourseNames() != null) {
            preferences.setByNameCourseDesignerCourseNames(configuration.getByNameCourseDesignerCourseNames());
            logApply(context, "by name course designer course names",
                    configuration.getByNameCourseDesignerCourseNames());
        }

        preferences.setNeedConfigRefresh(false);
    }

    private static void logApply(Context context, String configurationName, Object value) {
        ExLog.i(context, TAG, String.format("Applied '%s' configuration: %s.", configurationName,
                value == null ? "null" : value.toString()));
    }
}
