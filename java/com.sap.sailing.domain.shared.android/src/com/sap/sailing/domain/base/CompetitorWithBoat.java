package com.sap.sailing.domain.base;

import com.sap.sse.datamining.annotations.Connector;

/**
 * A competitor having an assigned boat
 * This makes sense e.g. in the context of a race where a competitor needs a boat in order to compete 
 * @author fmittag
 *
 */
public interface CompetitorWithBoat extends Competitor {
    @Connector(messageKey = "Boat", ordinal = 10)
    Boat getBoat();
}
