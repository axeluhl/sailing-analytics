package com.sap.sailing.kiworesultimport.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.kiworesultimport.Boat;
import com.sap.sailing.kiworesultimport.BoatResultInRace;
import com.sap.sailing.kiworesultimport.RaceSummary;

public class RaceSummaryImpl implements RaceSummary {
    private final String boatClassName;
    private final Map<Boat, BoatResultInRace> boatResults;
    private final Map<String, Boat> sailingNumberToBoat;
    private final Iterable<String> fleetNames;
    private final int raceNumber;
    
    public RaceSummaryImpl(String boatClassName, Map<Boat, BoatResultInRace> boatResults, Iterable<String> fleetNames, int raceNumber) {
        super();
        this.boatClassName = boatClassName;
        this.boatResults = boatResults;
        this.raceNumber = raceNumber;
        this.fleetNames = fleetNames;
        this.sailingNumberToBoat = new HashMap<String, Boat>();
        for (Boat boat : boatResults.keySet()) {
            sailingNumberToBoat.put(boat.getSailingNumber(), boat);
        }
    }

    @Override
    public String getBoatClassName() {
        return boatClassName;
    }

    @Override
    public int getRaceNumber() {
        return raceNumber;
    }

    @Override
    public Iterable<Boat> getBoats() {
        return boatResults.keySet();
    }

    @Override
    public BoatResultInRace getBoatResults(Boat boat) {
        return boatResults.get(boat);
    }
    
    @Override
    public Boat getBoat(String sailingNumber) {
        return sailingNumberToBoat.get(sailingNumber);
    }

    @Override
    public Iterable<String> getFleetNames() {
        return fleetNames;
    }
}
