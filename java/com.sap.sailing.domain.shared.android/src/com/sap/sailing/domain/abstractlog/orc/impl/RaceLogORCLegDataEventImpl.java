package com.sap.sailing.domain.abstractlog.orc.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;

public class RaceLogORCLegDataEventImpl extends RaceLogEventImpl implements RaceLogORCLegDataEvent {

    private static final long serialVersionUID = -5063350268001993185L;
    
    private final int legNr;
    private final Bearing twa;
    private final Distance length;

    public RaceLogORCLegDataEventImpl(TimePoint createdAt, TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, int legNr, Bearing twa, Distance length) {
        super(createdAt, logicalTimePoint, author, pId, pPassId);
        this.legNr = legNr;
        this.twa = twa;
        this.length = length;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public String getShortInfo() {
        return "ORCLegData, legNr= " + legNr + ", twa: " + twa.getDegrees() + ", length: " + length.getNauticalMiles();
    }
    
    @Override
    public int getLegNr() {
        return legNr;
    }
    
    @Override
    public Distance getLength() {
        return length;
    }
        
    @Override
    public Bearing getTwa() {
        return twa;
    }
}
