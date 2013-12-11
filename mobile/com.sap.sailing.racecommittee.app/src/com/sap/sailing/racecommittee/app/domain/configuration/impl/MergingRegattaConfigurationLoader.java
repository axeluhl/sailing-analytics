package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;

public class MergingRegattaConfigurationLoader implements ConfigurationLoader<RegattaConfiguration> {

    private final RegattaConfiguration localConfiguration;
    private final ConfigurationLoader<RegattaConfiguration> globalConfigurationLoader;

    public MergingRegattaConfigurationLoader(RegattaConfiguration localConfiguration,
            ConfigurationLoader<RegattaConfiguration> globalConfigurationLoader) {
        this.localConfiguration = localConfiguration;
        this.globalConfigurationLoader = globalConfigurationLoader;
    }

    @Override
    public RegattaConfiguration load() {
        RegattaConfiguration base = globalConfigurationLoader.load();
        RegattaConfiguration update = localConfiguration.clone();
        return base.merge(update);
    }

    @Override
    public void store() {
        throw new UnsupportedOperationException();
    }
}
