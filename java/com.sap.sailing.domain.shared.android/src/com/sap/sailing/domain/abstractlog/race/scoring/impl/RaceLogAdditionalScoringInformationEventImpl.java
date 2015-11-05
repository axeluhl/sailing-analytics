package com.sap.sailing.domain.abstractlog.race.scoring.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventImpl;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogAdditionalScoringInformationEventImpl extends RaceLogEventImpl implements RaceLogAdditionalScoringInformationEvent {
    private static final long serialVersionUID = -7627714111951381979L;
    
    private final AdditionalScoringInformationType informationType;
    
    public RaceLogAdditionalScoringInformationEventImpl(TimePoint createdAt, TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, AdditionalScoringInformationType informationType) {
        super(createdAt, logicalTimePoint, author, pId, pPassId);
        this.informationType = informationType;
    }
    
    public RaceLogAdditionalScoringInformationEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int pPassId, AdditionalScoringInformationType informationType) {
        this(now(), logicalTimePoint, author, randId(), pPassId, informationType);
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
