package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;

/**
 * Obtains flees and medal information from the {@link Series} to which it is connected at construction time.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RaceColumnInSeriesImpl extends AbstractRaceColumn implements RaceColumnInSeries {
    private static final long serialVersionUID = -2199678838624406645L;
    private final Series series;

    public RaceColumnInSeriesImpl(String name, Series series) {
        super(name);
        this.series = series;
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

}
