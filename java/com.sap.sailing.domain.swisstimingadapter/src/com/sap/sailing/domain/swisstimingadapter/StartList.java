package com.sap.sailing.domain.swisstimingadapter;

import java.io.Serializable;

public interface StartList extends Serializable {
    String getRaceID();
    
    Iterable<Competitor> getCompetitors();
}
