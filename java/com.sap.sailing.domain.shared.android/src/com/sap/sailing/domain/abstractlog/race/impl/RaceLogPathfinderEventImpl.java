package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sse.common.TimePoint;

public class RaceLogPathfinderEventImpl extends RaceLogEventImpl implements RaceLogPathfinderEvent {

    private static final long serialVersionUID = -1654474931330970804L;
    private final String pathfinderId;

    public RaceLogPathfinderEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, String pathfinderId) {
        super(createdAt, pTimePoint, author, pId, pPassId);
        this.pathfinderId = pathfinderId;
    }

    public RaceLogPathfinderEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author, int pPassId,
            String pathfinderId) {
        this(now(), logicalTimePoint, author, randId(), pPassId, pathfinderId);
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
