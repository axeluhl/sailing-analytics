package com.sap.sailing.datamining;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface GPSFixContext {

    public Event getEvent();

    public TrackedRace getRace();

    public Competitor getCompetitor();

}