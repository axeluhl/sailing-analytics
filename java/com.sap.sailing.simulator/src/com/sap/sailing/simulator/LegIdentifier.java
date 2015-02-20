package com.sap.sailing.simulator;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public interface LegIdentifier extends RegattaAndRaceIdentifier {
    String getLegName();
    int getLegNumber();
}
