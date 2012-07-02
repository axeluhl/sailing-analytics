package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.impl.NamedImpl;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;

public class SeriesImpl extends NamedImpl implements Series, RaceColumnListener {
    private static final long serialVersionUID = -1640404303144907381L;
    private final Map<String, Fleet> fleetsByName;
    private final List<Fleet> fleetsInAscendingOrder;
    private final List<RaceColumnInSeries> raceColumns;
    private boolean isMedal;
    private Regatta regatta;
    private Set<RaceColumnListener> raceColumnListeners;
    
    /**
     * @param fleets must be non-empty
     */
    public SeriesImpl(String name, boolean isMedal, Iterable<? extends Fleet> fleets, Iterable<String> raceColumnNames,
            TrackedRegattaRegistry trackedRegattaRegistry) {
        super(name);
        if (fleets == null || Util.isEmpty(fleets)) {
            throw new IllegalArgumentException("Series must have at least one fleet");
        }
        this.fleetsByName = new HashMap<String, Fleet>();
        for (Fleet fleet : fleets) {
            this.fleetsByName.put(fleet.getName(), fleet);
        }
        fleetsInAscendingOrder = new ArrayList<Fleet>(fleetsByName.values());
        Collections.sort(fleetsInAscendingOrder);
        List<RaceColumnInSeries> myRaceColumns = new ArrayList<RaceColumnInSeries>();
        this.raceColumns = myRaceColumns;
        this.isMedal = isMedal;
        this.raceColumnListeners = new HashSet<RaceColumnListener>();
        for (String raceColumnName : raceColumnNames) {
            addRaceColumn(raceColumnName, trackedRegattaRegistry);
        }
    }

    @Override
    public void addRaceColumnListener(RaceColumnListener listener) {
        raceColumnListeners.add(listener);
    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
        raceColumnListeners.remove(listener);
    }
    
    private void notifyListenersAboutTrackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        for (RaceColumnListener listener : raceColumnListeners) {
            listener.trackedRaceLinked(raceColumn, fleet, trackedRace);
        }
    }

    private void notifyListenersAboutTrackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        for (RaceColumnListener listener : raceColumnListeners) {
            listener.trackedRaceUnlinked(raceColumn, fleet, trackedRace);
        }
    }

    @Override
    public Regatta getRegatta() {
        return regatta;
    }

    @Override
    public void setRegatta(Regatta regatta) {
        this.regatta = regatta;
    }

    public Iterable<? extends Fleet> getFleets() {
        return fleetsInAscendingOrder;
    }

    @Override
    public Fleet getFleetByName(String fleetName) {
        return fleetsByName.get(fleetName);
    }

    @Override
    public Iterable<? extends RaceColumnInSeries> getRaceColumns() {
        return raceColumns;
    }
    
    @Override
    public RaceColumnInSeries addRaceColumn(String raceColumnName, TrackedRegattaRegistry trackedRegattaRegistry) {
        final RaceColumnInSeriesImpl result = createRaceColumn(raceColumnName, trackedRegattaRegistry);
        result.addRaceColumnListener(this);
        raceColumns.add(result);
        return result;
    }

    private RaceColumnInSeriesImpl createRaceColumn(String raceColumnName, TrackedRegattaRegistry trackedRegattaRegistry) {
        RaceColumnInSeriesImpl result = new RaceColumnInSeriesImpl(raceColumnName, this, trackedRegattaRegistry);
        return result;
    }

    @Override
    public void moveRaceColumnUp(String raceColumnName) {
        // start at second element because first can't be moved up
        for (int i=1; i<raceColumns.size(); i++) {
            RaceColumnInSeries rc = raceColumns.get(i);
            if (rc.getName().equals(raceColumnName)) {
                raceColumns.remove(i);
                raceColumns.add(i-1, rc);
                break;
            }
        }
    }

    @Override
    public void moveRaceColumnDown(String raceColumnName) {
        // end at second-last element because last can't be moved down
        for (int i=0; i<raceColumns.size()-1; i++) {
            RaceColumnInSeries rc = raceColumns.get(i);
            if (rc.getName().equals(raceColumnName)) {
                raceColumns.remove(i);
                raceColumns.add(i+1, rc);
                break;
            }
        }
    }

    @Override
    public void removeRaceColumn(String raceColumnName) {
        RaceColumnInSeries rc = getRaceColumnByName(raceColumnName);
        if (rc != null) {
            raceColumns.remove(rc);
            rc.removeRaceColumnListener(this);
        }
    }

    @Override
    public RaceColumnInSeries getRaceColumnByName(String columnName) {
        for (RaceColumnInSeries raceColumn : getRaceColumns()) {
            if (raceColumn.getName().equals(columnName)) {
                return raceColumn;
            }
        }
        return null;
    }

    @Override
    public boolean isMedal() {
        return isMedal;
    }

    @Override
    public void setIsMedal(boolean isMedal) {
        this.isMedal = isMedal;
    }

    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        notifyListenersAboutTrackedRaceLinked(raceColumn, fleet, trackedRace);
        
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        notifyListenersAboutTrackedRaceUnlinked(raceColumn, fleet, trackedRace);
    }

}
