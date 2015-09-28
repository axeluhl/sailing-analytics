package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactorEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RegattaLogSetCompetitorTimeOnTimeFactorEventImpl extends RegattaLogSetCompetitorHandicapInfoEventImpl implements RegattaLogSetCompetitorTimeOnTimeFactorEvent {
    private static final long serialVersionUID = -7891407213804859604L;
    private final Double timeOnTimeFactor;
    
    public RegattaLogSetCompetitorTimeOnTimeFactorEventImpl(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, Competitor competitor, Double timeOnTimeFactor) {
        super(createdAt, author, logicalTimePoint, pId, competitor);
        this.timeOnTimeFactor = timeOnTimeFactor;
    }

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Double getTimeOnTimeFactor() {
        return timeOnTimeFactor;
    }
}
