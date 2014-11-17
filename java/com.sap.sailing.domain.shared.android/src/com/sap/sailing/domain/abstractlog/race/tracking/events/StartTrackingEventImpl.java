package com.sap.sailing.domain.abstractlog.race.tracking.events;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.StartTrackingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;

public class StartTrackingEventImpl extends RaceLogEventImpl implements
StartTrackingEvent {
    private static final long serialVersionUID = 6017954368580125221L;

    public StartTrackingEventImpl(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, int pPassId) {
        super(createdAt, author, logicalTimePoint, pId, Collections.<Competitor>emptyList(), pPassId);
    }


    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
