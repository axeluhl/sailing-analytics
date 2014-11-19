package com.sap.sailing.domain.racelog.scoring.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.impl.RaceLogEventImpl;
import com.sap.sailing.domain.racelog.scoring.AdditionalScoringInformationEvent;
import com.sap.sailing.domain.racelog.scoring.AdditionalScoringInformationType;
import com.sap.sse.common.TimePoint;

public class AdditionalScoringInformationEventImpl extends RaceLogEventImpl implements AdditionalScoringInformationEvent {
    private static final long serialVersionUID = -7627714111951381979L;
    
    private final AdditionalScoringInformationType informationType;
    
    public AdditionalScoringInformationEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, List<Competitor> competitors, int pPassId, AdditionalScoringInformationType informationType) {
        super(createdAt, author, logicalTimePoint, pId, competitors, pPassId);
        this.informationType = informationType;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public String getShortInfo() {
        return "UUID: " + getId() + " Type: " + getType().name();
    }
    
    @Override
    public String toString() {
        return super.toString() + " Type: " + getType().name();
    }

    @Override
    public AdditionalScoringInformationType getType() {
        return informationType;
    }
}
