package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogFlagEventImpl extends RaceLogEventImpl implements RaceLogFlagEvent {

    private static final long serialVersionUID = 6333303528852541914L;
    private final Flags upperFlag;
    private final Flags lowerFlag;
    private final boolean isDisplayed;

    public RaceLogFlagEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, Flags pUpperFlag, Flags pLowerFlag, boolean pIsDisplayed) {
        super(createdAt, author, pTimePoint, pId, pInvolvedBoats, pPassId);
        this.upperFlag = pUpperFlag;
        this.lowerFlag = pLowerFlag;
        this.isDisplayed = pIsDisplayed;
    }

    @Override
    public Flags getUpperFlag() {
        return upperFlag;
    }

    @Override
    public Flags getLowerFlag() {
        return lowerFlag;
    }

    @Override
    public boolean isDisplayed() {
        return isDisplayed;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getShortInfo() {
        return "upperFlag=" + upperFlag + ", lowerFlag=" + lowerFlag + ", isDisplayed=" + isDisplayed;
    }

 }
