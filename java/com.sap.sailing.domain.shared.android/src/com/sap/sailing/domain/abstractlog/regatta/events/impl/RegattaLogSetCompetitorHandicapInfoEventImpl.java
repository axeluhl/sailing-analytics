package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorHandicapInfoEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public abstract class RegattaLogSetCompetitorHandicapInfoEventImpl extends AbstractLogEventImpl<RegattaLogEventVisitor> implements
        RegattaLogSetCompetitorHandicapInfoEvent {
    private static final long serialVersionUID = -4865431949414981216L;

    private final Competitor competitor;

    protected RegattaLogSetCompetitorHandicapInfoEventImpl(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable pId, Competitor competitor) {
        super(createdAt, author, logicalTimePoint, pId);
        this.competitor = competitor;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }
}
