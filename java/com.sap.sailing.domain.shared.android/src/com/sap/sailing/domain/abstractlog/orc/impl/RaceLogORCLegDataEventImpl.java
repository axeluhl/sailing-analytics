package com.sap.sailing.domain.abstractlog.orc.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventImpl;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;

public class RaceLogORCLegDataEventImpl extends RaceLogEventImpl implements RaceLogORCLegDataEvent {

    private static final long serialVersionUID = -5063350268001993185L;
    
    private final int oneBasedLegNumber;
    private final Bearing twa;
    private final Distance length;
    private final ORCPerformanceCurveLegTypes type;

    public RaceLogORCLegDataEventImpl(TimePoint createdAt, TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, int oneBasedLegNumber, Bearing twa, Distance length, ORCPerformanceCurveLegTypes type) {
        super(createdAt, logicalTimePoint, author, pId, pPassId);
        this.oneBasedLegNumber = oneBasedLegNumber;
        this.twa = twa;
        this.length = length;
        this.type = type;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public String getShortInfo() {
        return ""+getType()+" for leg #"+oneBasedLegNumber + (twa==null?"":(", twa: " + twa.getDegrees())) +
                (length==null?"":(", length: " + length.getNauticalMiles()+"NM"));
    }
    
    @Override
    public int getOneBasedLegNumber() {
        return oneBasedLegNumber;
    }
    
    @Override
    public Distance getLength() {
        return length;
    }
        
    @Override
    public Bearing getTwa() {
        return twa;
    }

    @Override
    public ORCPerformanceCurveLegTypes getType() {
        return type;
    }
}
