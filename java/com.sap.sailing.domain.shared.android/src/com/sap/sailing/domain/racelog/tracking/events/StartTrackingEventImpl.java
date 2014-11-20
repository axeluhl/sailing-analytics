package com.sap.sailing.domain.racelog.tracking.events;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventImpl;
import com.sap.sailing.domain.racelog.tracking.StartTrackingEvent;
import com.sap.sse.common.TimePoint;

public class StartTrackingEventImpl extends RaceLogEventImpl implements
StartTrackingEvent {
    private static final long serialVersionUID = 6017954368580125221L;

    public StartTrackingEventImpl(TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, int pPassId) {
        super(createdAt, author, logicalTimePoint, pId, Collections.<Competitor>emptyList(), pPassId);
    }


    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
