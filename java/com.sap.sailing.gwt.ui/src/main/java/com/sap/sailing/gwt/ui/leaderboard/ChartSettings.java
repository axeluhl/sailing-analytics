package com.sap.sailing.gwt.ui.leaderboard;

public class ChartSettings {
    private final int stepsToLoad;
    
    public ChartSettings(int stepsToLoad) {
        this.stepsToLoad = stepsToLoad;
    }

    /**
     * Copy-constructor
     */
    public ChartSettings(ChartSettings superResult) {
        this(superResult.getStepsToLoad());
    }

    public int getStepsToLoad() {
        return stepsToLoad;
    }
}
