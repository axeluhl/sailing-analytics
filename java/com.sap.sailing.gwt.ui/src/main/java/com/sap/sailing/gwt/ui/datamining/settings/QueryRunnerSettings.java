package com.sap.sailing.gwt.ui.datamining.settings;

import com.sap.sse.common.settings.AbstractSettings;

public class QueryRunnerSettings extends AbstractSettings {
    
    private boolean runAutomatically;
    
    public QueryRunnerSettings() {
        this(true);
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
