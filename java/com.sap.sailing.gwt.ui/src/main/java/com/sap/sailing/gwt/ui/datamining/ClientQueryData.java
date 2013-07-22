package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.datamining.shared.SelectorType;

public class ClientQueryData {
    private SelectorType selectorType;
    private String[] selectionIdentifiers;
    private int times;
    private int currentRun;

    public ClientQueryData(SelectorType selectorType, String[] selectionIdentifiers, int times, int currentRun) {
        this.selectorType = selectorType;
        this.selectionIdentifiers = selectionIdentifiers;
        this.times = times;
        this.currentRun = currentRun;
    }

    public SelectorType getSelectorType() {
        return selectorType;
    }

    public String[] getSelectionIdentifiers() {
        return selectionIdentifiers;
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