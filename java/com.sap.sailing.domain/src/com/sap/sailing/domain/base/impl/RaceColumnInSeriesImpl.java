package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;

/**
 * Obtains flees and medal information from the {@link Series} to which it is connected at construction time.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RaceColumnInSeriesImpl extends AbstractRaceColumn implements RaceColumnInSeries {
    private static final long serialVersionUID = -2199678838624406645L;
    private final Series series;
    private final TrackedRegattaRegistry trackedRegattaRegistry;

    public RaceColumnInSeriesImpl(String name, Series series, TrackedRegattaRegistry trackedRegattaRegistry) {
        super(name);
        this.series = series;
        this.trackedRegattaRegistry = trackedRegattaRegistry;
    }

    @Override
    public Regatta getRegatta() {
        return getSeries().getRegatta();
    }

    @Override
    public Iterable<? extends Fleet> getFleets() {
        return series.getFleets();
    }

    @Override
    public boolean isMedalRace() {
        return series.isMedal();
    }

    @Override
    public Series getSeries() {
        return series;
    }

    /**
     * In addition to associating the tracked race to this column for the fleet specified, this method also assures that
     * <code>trackedRace</code> belongs to the {@link TrackedRegatta} of this column's series'
     * {@link Series#getRegatta() regatta} and that the corresponding {@link TrackedRace#getRace() RaceDefinition} is owned
     * by this column's series' {@link Series#getRegatta() regatta}.
     */
    @Override
    public void setTrackedRace(Fleet fleet, TrackedRace trackedRace) {
        RaceDefinition race = trackedRace.getRace();
        TrackedRegatta trackedRegatta = trackedRace.getTrackedRegatta();
        Regatta regatta = trackedRegatta.getRegatta();
        if (regatta != getRegatta()) {
            // re-associate:
            regatta.removeRace(race);
            getRegatta().addRace(race);
            trackedRegatta.removeTrackedRace(trackedRace);
            trackedRegattaRegistry.getOrCreateTrackedRegatta(getRegatta()).addTrackedRace(trackedRace);
        }
        super.setTrackedRace(fleet, trackedRace);
    }

}
