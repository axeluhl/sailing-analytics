package com.sap.sailing.domain.racelog.tracking.events;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventImpl;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;

public class DenoteForTrackingEventImpl extends RaceLogEventImpl implements
DenoteForTrackingEvent {
    private static final long serialVersionUID = 6937741283401976385L;

    private final String raceName;
    private final BoatClass boatClass;

    public DenoteForTrackingEventImpl(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, int pPassId,
            String raceName, BoatClass boatClass) {
        super(createdAt, author, logicalTimePoint, pId, Collections.<Competitor>emptyList(), pPassId);
        this.raceName = raceName;
        this.boatClass = boatClass;
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

}
