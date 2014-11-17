package com.sap.sailing.domain.abstractlog.race.tracking.events;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;

public class DenoteForTrackingEventImpl extends RaceLogEventImpl implements
DenoteForTrackingEvent {
    private static final long serialVersionUID = 6937741283401976385L;

    private final String raceName;
    private final BoatClass boatClass;
    private final Serializable raceId;

    public DenoteForTrackingEventImpl(TimePoint createdAt,
            AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int pPassId,
            String raceName, BoatClass boatClass, Serializable raceId) {
        super(createdAt, author, logicalTimePoint, pId, Collections.<Competitor>emptyList(), pPassId);
        this.raceName = raceName;
        this.boatClass = boatClass;
        this.raceId = raceId;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getRaceName() {
        return raceName;
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }
    
    @Override
    public Serializable getRaceId() {
        return raceId;
    }
}
