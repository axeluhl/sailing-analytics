package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Series;

/**
 * Obtains flees and medal information from the {@link Series} to which it is connected at construction time.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RaceColumnInSeries extends AbstractRaceColumn {
    private static final long serialVersionUID = -2199678838624406645L;
    private final Series series;

    public RaceColumnInSeries(String name, Series series) {
        super(name);
        this.series = series;
    }

    @Override
    public Iterable<? extends Fleet> getFleets() {
        return series.getFleets();
    }

    @Override
    public boolean isMedalRace() {
        return series.isMedal();
    }
}
