package com.sap.sse.datamining.ui.client.settings;

import com.sap.sse.common.settings.AbstractSettings;

public class AdvancedDataMiningSettings extends AbstractSettings {
    
    public enum ChangeLossStrategy {
        /** Ask the user for the strategy to use */
        ASK,
        /** Display the query overwriting the current selection */
        DISCARD_CHANGES,
        /** Keep the current selection. The new query won't be displayed. */
        KEEP_CHANGES
    } 

    private boolean developerOptions;
    private ChangeLossStrategy changeLossStrategy;

    public AdvancedDataMiningSettings() {
        this(false, ChangeLossStrategy.ASK);
    }

    public AdvancedDataMiningSettings(boolean developerOptions, ChangeLossStrategy changeLossStrategy) {
        this.developerOptions = developerOptions;
        this.changeLossStrategy = changeLossStrategy;
    }

    public AdvancedDataMiningSettings(AdvancedDataMiningSettings settings) {
        this(settings.developerOptions, settings.changeLossStrategy);
    }

    public boolean isDeveloperOptions() {
        return developerOptions;
    }

    public void setDeveloperOptions(boolean developerOptions) {
        this.developerOptions = developerOptions;
    }

    public ChangeLossStrategy getChangeLossStrategy() {
        return changeLossStrategy;
    }

    public void setChangeLossStrategy(ChangeLossStrategy changeLossStrategy) {
        this.changeLossStrategy = changeLossStrategy;
    }

}
