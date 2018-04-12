package com.sap.sse.datamining.ui.settings;

import com.sap.sse.common.settings.AbstractSettings;

public class AdvancedDataMiningSettings extends AbstractSettings {

    private boolean developerOptions;

    public AdvancedDataMiningSettings() {
        this(false);
    }

    public AdvancedDataMiningSettings(boolean developerOptions) {
        this.developerOptions = developerOptions;
    }

    public AdvancedDataMiningSettings(AdvancedDataMiningSettings settings) {
        this(settings.developerOptions);
    }

    public boolean isDeveloperOptions() {
        return developerOptions;
    }

    public void setDeveloperOptions(boolean developerOptions) {
        this.developerOptions = developerOptions;
    }

}
