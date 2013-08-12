package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Map;

public class ClientBenchmarkData<DimensionType> {
    
    private Map<DimensionType, Collection<?>> selection;
    private int times;
    private int currentRun;

    public ClientBenchmarkData(Map<DimensionType, Collection<?>> selection, int times, int currentRun) {
        this.selection = selection;
        this.times = times;
        this.currentRun = currentRun;
    }

    public Map<DimensionType, Collection<?>> getSelection() {
        return selection;
    }

    public int getTimes() {
        return times;
    }

    public int getCurrentRun() {
        return currentRun;
    }

    public void incrementCurrentRun() {
        currentRun++;
    }

    public boolean isFinished() {
        return getCurrentRun() == getTimes();
    }
}