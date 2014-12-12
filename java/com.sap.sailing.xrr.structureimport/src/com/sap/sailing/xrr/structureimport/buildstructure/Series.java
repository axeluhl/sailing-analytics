package com.sap.sailing.xrr.structureimport.buildstructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.xrr.schema.Race;

public class Series {
    private String series = "";
    private int maxRaces = 0;
    private int maxRacesIndex = -1;
    private List<String> raceNames = new ArrayList<String>();
    private List<Fleet> fleets = new ArrayList<Fleet>();
    
    public Series(String raceType) {
        this.series = raceType;
    }

    @SuppressWarnings("unchecked")
    public void addRace(Race race, int[] numberOfRaces) {
        boolean fleetEx = false;
        int raceNumber = race.getRaceNumber().intValue();
        String[] raceName = race.getRaceName().split(" ");
        String fleetColor = "";
        if (raceName.length > 1) {
            fleetColor = raceName[raceName.length - 1];
            for (Fleet fleet : fleets) {
                if (fleetColor.equals(fleet.getColor())) {
                    fleetEx = true;
                }
            }
        }
        if (numberOfRaces[raceNumber - 1] <= 1 && !fleetEx) {
            fleetColor = "";
        }
        boolean added = false;
        int counter = 0;
        for (Fleet fleet : fleets) {
            if (fleet.getColor().equals(fleetColor)) {
                fleet.addRace(race);
                added = true;
            }
            if (fleet.getNumRaces() > maxRaces) {
                maxRaces = fleet.getNumRaces();
                maxRacesIndex = counter;
            }
            counter++;
        }
        if (!added) {
            Fleet newFleet = new Fleet(fleetColor);
            newFleet.addRace(race);
            if (newFleet.getNumRaces() > maxRaces) {
                maxRaces = newFleet.getNumRaces();
                maxRacesIndex = fleets.size();
            }
            fleets.add(newFleet);
            Collections.sort(fleets);
        }
    }

    public char getFirstChar() {
        return series.charAt(0);
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public List<Fleet> getFleets() {
        return fleets;
    }

    public int getMaxRaces() {
        return maxRaces;
    }

    public int getMaxIndex() {
        return maxRacesIndex;
    }

    public boolean isMedal() {
        if (this.series.equals("Medal")) {
            return true;
        } else {
            return false;
        }
    }

    public List<String> getRaceNames() {
        return raceNames;
    }
    
    public void setRaceNames(List<String> raceNames){
        this.raceNames = raceNames;
    }

}
