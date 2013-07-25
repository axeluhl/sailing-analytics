package com.sap.sailing.gwt.ui.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.shared.SelectionType;

public class ClientQueryData {
    private SelectionType selectionType;
    private Collection<String> selection;
    private int times;
    private int currentRun;

    public ClientQueryData(SelectionType selectionType, Collection<String> selection, int times, int currentRun) {
        this.selectionType = selectionType;
        this.selection = new ArrayList<String>(selection);
        this.times = times;
        this.currentRun = currentRun;
    }

    public SelectionType getSelectionType() {
        return selectionType;
    }

    public Collection<String> getSelection() {
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