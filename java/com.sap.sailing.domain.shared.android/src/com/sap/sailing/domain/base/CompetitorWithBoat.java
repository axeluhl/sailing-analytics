package com.sap.sailing.domain.base;

import java.io.Serializable;

public interface CompetitorWithBoat extends Serializable {
    Competitor getCompetitor();
    Boat getBoat();
}
