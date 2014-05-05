package com.sap.sailing.gwt.ui.datamining.client.settings;

public class QueryRunnerSettings {
    
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
