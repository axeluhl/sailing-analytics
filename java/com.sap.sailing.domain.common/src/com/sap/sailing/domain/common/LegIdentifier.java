package com.sap.sailing.domain.common;

public interface LegIdentifier extends RegattaAndRaceIdentifier {
    String getLegName();
    int getLegNumber();
}
