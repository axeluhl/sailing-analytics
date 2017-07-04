package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEventImpl extends
        RegattaLogSetCompetitorHandicapInfoEventImpl implements
        RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent {
    private static final long serialVersionUID = 1603450814971358782L;
    private final Duration timeOnDistanceAllowancePerNauticalMile;

    public RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEventImpl(TimePoint createdAt,
            TimePoint logicalTimePoint, AbstractLogEventAuthor author, Serializable pId, Competitor competitor,
            Duration timeOnDistanceAllowancePerNauticalMile) {
        super(createdAt, logicalTimePoint, author, pId, competitor);
        this.timeOnDistanceAllowancePerNauticalMile = timeOnDistanceAllowancePerNauticalMile;
    }

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Duration getTimeOnDistanceAllowancePerNauticalMile() {
        return timeOnDistanceAllowancePerNauticalMile;
    }

}
