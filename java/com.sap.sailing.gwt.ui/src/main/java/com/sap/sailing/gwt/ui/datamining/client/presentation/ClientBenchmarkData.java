package com.sap.sailing.gwt.ui.datamining.client.presentation;


public class ClientBenchmarkData {
    
    private int times;
    private int currentRun;

    public ClientBenchmarkData(int times, int currentRun) {
        this.times = times;
        this.currentRun = currentRun;
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