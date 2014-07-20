package com.sap.sailing.domain.racelog.scoring.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventImpl;
import com.sap.sailing.domain.racelog.scoring.AdditionalScoringInformationEvent;

public class AdditionalScoringInformationEventImpl extends RaceLogEventImpl implements AdditionalScoringInformationEvent {
    private static final long serialVersionUID = -7627714111951381979L;
    
    public AdditionalScoringInformationEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, List<Competitor> competitors, int pPassId) {
        super(createdAt, author, logicalTimePoint, pId, competitors, pPassId);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
