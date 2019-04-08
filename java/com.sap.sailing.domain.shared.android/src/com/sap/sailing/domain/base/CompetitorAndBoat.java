package com.sap.sailing.domain.base;

import java.io.Serializable;

/** 
 * A helper interface to combine a competitor and a boat
 * @author fmittag
 *
 */
public interface CompetitorAndBoat extends Serializable {
    Competitor getCompetitor();
    Boat getBoat();
}
