package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.CompetitorWithBoat;

public interface DynamicCompetitorWithBoat extends DynamicCompetitor, CompetitorWithBoat {
    DynamicBoat getBoat();
}
