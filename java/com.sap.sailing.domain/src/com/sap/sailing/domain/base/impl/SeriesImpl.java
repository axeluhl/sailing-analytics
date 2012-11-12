package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.impl.NamedImpl;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.util.impl.RaceColumnListeners;

public class SeriesImpl extends NamedImpl implements Series, RaceColumnListener {
    private static final long serialVersionUID = -1640404303144907381L;
    private final Map<String, Fleet> fleetsByName;
    private final List<Fleet> fleetsInAscendingOrder;
    private final List<RaceColumnInSeries> raceColumns;
    private boolean isMedal;
    private Regatta regatta;
    private final RaceColumnListeners raceColumnListeners;
    
    /**
     * @param fleets
     *            must be non-empty
     * @param trackedRegattaRegistry
     *            used to find the {@link TrackedRegatta} for this column's series' {@link Series#getRegatta() regatta}
     *            in order to re-associate a {@link TrackedRace} passed to {@link #setTrackedRace(Fleet, TrackedRace)}
     *            with this column's series' {@link TrackedRegatta}, and the tracked race's {@link RaceDefinition} with
     *            this column's series {@link Regatta}, respectively. If <code>null</code>, the re-association won't be
     *            carried out.
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
        this.raceColumnListeners = new RaceColumnListeners();
        for (String raceColumnName : raceColumnNames) {
            addRaceColumn(raceColumnName, trackedRegattaRegistry);
        }
    }

    @Override
    public void addRaceColumnListener(RaceColumnListener listener) {
        raceColumnListeners.addRaceColumnListener(listener);
    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
        raceColumnListeners.removeRaceColumnListener(listener);
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

    /**
     * @param trackedRegattaRegistry
     *            used to find the {@link TrackedRegatta} for this column's series' {@link Series#getRegatta() regatta}
     *            in order to re-associate a {@link TrackedRace} passed to {@link #setTrackedRace(Fleet, TrackedRace)}
     *            with this column's series' {@link TrackedRegatta}, and the tracked race's {@link RaceDefinition} with
     *            this column's series {@link Regatta}, respectively. If <code>null</code>, the re-association won't be
     *            carried out.
     */
    @Override
    public RaceColumnInSeries addRaceColumn(String raceColumnName, TrackedRegattaRegistry trackedRegattaRegistry) {
        RaceColumnInSeriesImpl result = createRaceColumn(raceColumnName, trackedRegattaRegistry);
        if (raceColumnListeners.canAddRaceColumnToContainer(result)) {
            result.addRaceColumnListener(this);
            raceColumns.add(result);
            raceColumnListeners.notifyListenersAboutRaceColumnAddedToContainer(result);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * @param trackedRegattaRegistry
     *            used to find the {@link TrackedRegatta} for this column's series' {@link Series#getRegatta() regatta}
     *            in order to re-associate a {@link TrackedRace} passed to {@link #setTrackedRace(Fleet, TrackedRace)}
     *            with this column's series' {@link TrackedRegatta}, and the tracked race's {@link RaceDefinition} with
     *            this column's series {@link Regatta}, respectively. If <code>null</code>, the re-association won't be
     *            carried out.
     */
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
                raceColumnListeners.notifyListenersAboutRaceColumnRemovedFromContainer(rc);
                raceColumns.add(i-1, rc);
                raceColumnListeners.notifyListenersAboutRaceColumnAddedToContainer(rc);
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
                raceColumnListeners.notifyListenersAboutRaceColumnRemovedFromContainer(rc);
                raceColumns.add(i+1, rc);
                raceColumnListeners.notifyListenersAboutRaceColumnAddedToContainer(rc);
                break;
            }
        }
    }

    @Override
    public void removeRaceColumn(String raceColumnName) {
        RaceColumnInSeries rc = getRaceColumnByName(raceColumnName);
        if (rc != null) {
            raceColumns.remove(rc);
            raceColumnListeners.notifyListenersAboutRaceColumnRemovedFromContainer(rc);
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
        raceColumnListeners.notifyListenersAboutTrackedRaceLinked(raceColumn, fleet, trackedRace);
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        raceColumnListeners.notifyListenersAboutTrackedRaceUnlinked(raceColumn, fleet, trackedRace);
    }

    @Override
    public void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
        raceColumnListeners.notifyListenersAboutIsMedalRaceChanged(raceColumn, newIsMedalRace);
    }

    /**
     * A series listens on its columns; individual columns, however, don't ask whether they can be added; the series itself does.
     * 
     * @see #addRaceColumn(String, TrackedRegattaRegistry)
     */
    @Override
    public boolean canAddRaceColumnToContainer(RaceColumn raceColumn) {
        return true;
    }

    @Override
    public void raceColumnAddedToContainer(RaceColumn raceColumn) {
        raceColumnListeners.notifyListenersAboutRaceColumnAddedToContainer(raceColumn);
    }

    @Override
    public void raceColumnRemovedFromContainer(RaceColumn raceColumn) {
        raceColumnListeners.notifyListenersAboutRaceColumnRemovedFromContainer(raceColumn);
    }

    @Override
    public void raceColumnMoved(RaceColumn raceColumn, int newIndex) {
        raceColumnListeners.notifyListenersAboutRaceColumnMoved(raceColumn, newIndex);
    }

    @Override
    public void factorChanged(RaceColumn raceColumn, Double oldFactor, Double newFactor) {
        raceColumnListeners.notifyListenersAboutFactorChanged(raceColumn, oldFactor, newFactor);
    }

    @Override
    public void competitorDisplayNameChanged(Competitor competitor, String oldDisplayName, String displayName) {
        raceColumnListeners.notifyListenersAboutCompetitorDisplayNameChanged(competitor, oldDisplayName, displayName);
    }

    @Override
    public boolean isTransient() {
        return false;
    }
}
