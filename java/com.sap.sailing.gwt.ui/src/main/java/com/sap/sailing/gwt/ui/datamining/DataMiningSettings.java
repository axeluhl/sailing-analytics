package com.sap.sailing.gwt.ui.datamining;

public class DataMiningSettings {
    
    private boolean runAutomatically;
    
    public DataMiningSettings() {
        this(true);
    }

    public DataMiningSettings(boolean runAutomatically) {
        this.runAutomatically = runAutomatically;
    }

    public DataMiningSettings(DataMiningSettings settings) {
        this(settings.isRunAutomatically());
    }

    public boolean isRunAutomatically() {
        return runAutomatically;
    }

    public void setRunAutomatically(boolean runAutomatically) {
        this.runAutomatically = runAutomatically;
    }

}
