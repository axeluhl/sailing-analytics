package com.sap.sailing.domain.swisstimingadapter;

public interface StartList {
    String getRaceID();
    
    Iterable<Competitor> getCompetitors();
}
