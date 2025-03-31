package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;

/**
 * The interface represents a series and holds its races. The interface abstracts from the server internal differences 
 * between Leaderboards and Regattas for any app since Leaderboards do not have any series, but Regattas.
 */
public interface SeriesWithRows extends SeriesBase {
    
    /**
     * All {@link RaceRow}s of this series.
     */
    Iterable<RaceRow> getRaceRows();

    RaceRow getRaceRow(Fleet fleet);
}
