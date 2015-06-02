package com.sap.sailing.domain.common;

public interface LegIdentifier extends RegattaAndRaceIdentifier {
    RaceIdentifier getRaceIdentifier();
    String getLegName();
    int getLegNumber();
}
