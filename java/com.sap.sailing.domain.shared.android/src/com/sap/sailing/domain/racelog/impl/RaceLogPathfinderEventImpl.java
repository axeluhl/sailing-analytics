package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;

public class RaceLogPathfinderEventImpl extends RaceLogEventImpl implements RaceLogPathfinderEvent {
   
    private static final long serialVersionUID = -1654474931330970804L;
    private final String pathfinderId;

    public RaceLogPathfinderEventImpl(TimePoint createdAt,
            RaceLogEventAuthor author, TimePoint pTimePoint, Serializable pId, List<Competitor> pCompetitors, int pPassId, String pathfinderId) {
        super(createdAt, author, pTimePoint, pId, pCompetitors, pPassId);
        this.pathfinderId = pathfinderId;
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getPathfinderId() {
        return this.pathfinderId;
    }

    @Override
    public String getShortInfo() {
        return "pathfinderId=" + pathfinderId;
    }

}
