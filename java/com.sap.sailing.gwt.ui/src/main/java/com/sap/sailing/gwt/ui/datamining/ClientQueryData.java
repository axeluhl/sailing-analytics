package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.shared.SelectionType;

public class ClientQueryData {
    
    private Map<SelectionType, Collection<?>> selection;
    private int times;
    private int currentRun;

    public ClientQueryData(Map<SelectionType, Collection<?>> selection, int times, int currentRun) {
        this.selection = selection;
        this.times = times;
        this.currentRun = currentRun;
    }

    public Map<SelectionType, Collection<?>> getSelection() {
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