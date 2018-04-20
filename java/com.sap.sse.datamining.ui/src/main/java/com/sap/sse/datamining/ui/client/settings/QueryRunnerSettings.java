package com.sap.sse.datamining.ui.client.settings;

import com.sap.sse.common.settings.AbstractSettings;

public class QueryRunnerSettings extends AbstractSettings {
    
    private boolean runAutomatically;
    
    public QueryRunnerSettings() {
        this(false);
    }

    public QueryRunnerSettings(boolean runAutomatically) {
        this.runAutomatically = runAutomatically;
    }

    public QueryRunnerSettings(QueryRunnerSettings settings) {
        this(settings.isRunAutomatically());
    }

    public boolean isRunAutomatically() {
        return runAutomatically;
    }

    protected void setRunAutomatically(boolean runAutomatically) {
        this.runAutomatically = runAutomatically;
    }

}
