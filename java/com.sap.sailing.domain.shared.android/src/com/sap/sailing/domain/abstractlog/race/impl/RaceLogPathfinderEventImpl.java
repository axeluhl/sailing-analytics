package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RaceLogPathfinderEventImpl extends RaceLogEventImpl implements RaceLogPathfinderEvent {

    private static final long serialVersionUID = -1654474931330970804L;
    private final String pathfinderId;

    public RaceLogPathfinderEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, List<Competitor> pCompetitors, int pPassId, String pathfinderId) {
        super(createdAt, pTimePoint, author, pId, pCompetitors, pPassId);
        this.pathfinderId = pathfinderId;
    }

    public RaceLogPathfinderEventImpl(TimePoint pTimePoint, AbstractLogEventAuthor author, int pPassId,
            String pathfinderId) {
        super(pTimePoint, author, pPassId);
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
