package com.sap.sailing.domain.abstractlog.orc.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCCertificateAssignmentEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventDataImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sse.common.TimePoint;

public class RaceLogORCCertificateAssignmentEventImpl extends BaseORCCertificateAssignmentEventImpl<RaceLogEventVisitor> implements RaceLogORCCertificateAssignmentEvent {
    private static final long serialVersionUID = 3506411337361892600L;
    private final RaceLogEventData raceLogEventData;

    public RaceLogORCCertificateAssignmentEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, int passId, ORCCertificate certificate, Competitor competitor) {
        super(createdAt, logicalTimePoint, author, pId, certificate, competitor);
        this.raceLogEventData = new RaceLogEventDataImpl(/* involvedBoats */ null, passId);
    }

    public RaceLogORCCertificateAssignmentEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, int passId, ORCCertificate certificate, Serializable competitorID) {
        super(createdAt, logicalTimePoint, author, pId, certificate, competitorID);
        this.raceLogEventData = new RaceLogEventDataImpl(/* involvedBoats */ null, passId);
    }
    
    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int getPassId() {
        return raceLogEventData.getPassId();
    }

    @Override
    public <T extends Competitor> List<T> getInvolvedCompetitors() {
        return raceLogEventData.getInvolvedCompetitors();
    }
}
