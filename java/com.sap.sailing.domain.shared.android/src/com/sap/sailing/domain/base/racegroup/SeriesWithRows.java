package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.base.SeriesData;

/**
 * The interface represents a series and holds its races. The interface abstracts from the server internal differences 
 * between Leaderboards and Regattas for any app since Leaderboards do not have any series, but Regattas.
 */
public interface SeriesWithRows extends SeriesData {
    public Iterable<RaceRow> getRaceRows();
}
