package com.sap.sailing.domain.statistics.impl;

import com.sap.sailing.domain.statistics.Statistics;

public class StatisticsImpl implements Statistics {

    private int numberOfCompetitors;
    private int numberOfRegattas;
    private int numberOfRaces;
    private int numberOfTrackedRaces;
    private long numberOfGPSFixes;
    private long numberOfWindFixes;
    private double sailedMiles;

    @Override
    public int getNumberOfCompetitors() {
        return numberOfCompetitors;
    }

    public void setNumberOfCompetitors(int numberOfCompetitors) {
        this.numberOfCompetitors = numberOfCompetitors;
    }

    @Override
    public int getNumberOfRegattas() {
        return numberOfRegattas;
    }

    public void setNumberOfRegattas(int numberOfRegattas) {
        this.numberOfRegattas = numberOfRegattas;
    }

    @Override
    public int getNumberOfRaces() {
        return numberOfRaces;
    }

    public void setNumberOfRaces(int numberOfRaces) {
        this.numberOfRaces = numberOfRaces;
    }

    @Override
    public int getNumberOfTrackedRaces() {
        return numberOfTrackedRaces;
    }

    public void setNumberOfTrackedRaces(int numberOfTrackedRaces) {
        this.numberOfTrackedRaces = numberOfTrackedRaces;
    }

    @Override
    public long getNumberOfGPSFixes() {
        return numberOfGPSFixes;
    }

    public void setNumberOfGPSFixes(long numberOfGPSFixes) {
        this.numberOfGPSFixes = numberOfGPSFixes;
    }

    @Override
    public long getNumberOfWindFixes() {
        return numberOfWindFixes;
    }

    public void setNumberOfWindFixes(long numberOfWindFixes) {
        this.numberOfWindFixes = numberOfWindFixes;
    }

    @Override
    public double getSailedMiles() {
        return sailedMiles;
    }

    public void setSailedMiles(double sailedMiles) {
        this.sailedMiles = sailedMiles;
    }

}
