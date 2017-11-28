package com.sap.sailing.domain.common;

public interface LegIdentifier extends RegattaAndRaceIdentifier {
    RegattaAndRaceIdentifier getRaceIdentifier();
    String getLegName();
    int getLegNumber();
}
