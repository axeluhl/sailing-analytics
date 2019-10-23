package com.sap.sailing.domain.abstractlog.orc.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCUseImpliedWindFromOtherRaceEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventDataImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogORCUseImpliedWindFromOtherRaceEventImpl extends AbstractLogEventImpl<RaceLogEventVisitor> implements RaceLogORCUseImpliedWindFromOtherRaceEvent {
    private static final long serialVersionUID = 3506411337361892600L;
    private final RaceLogEventData raceLogEventData;
    private final SimpleRaceLogIdentifier otherRace;

    public RaceLogORCUseImpliedWindFromOtherRaceEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, int passId, SimpleRaceLogIdentifier otherRace) {
        super(createdAt, logicalTimePoint, author, pId);
        this.raceLogEventData = new RaceLogEventDataImpl(/* involvedBoats */ null, passId);
        this.otherRace = otherRace;
        
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

    @Override
    public SimpleRaceLogIdentifier getOtherRace() {
        return otherRace;
    }

    @Override
    public String getShortInfo() {
        return "Using implied wind from race "+getOtherRace();
    }
}
