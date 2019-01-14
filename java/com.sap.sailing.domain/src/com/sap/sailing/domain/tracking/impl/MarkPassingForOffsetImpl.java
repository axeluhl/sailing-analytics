package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.MarkPassingForOffsetWaypoint;
import com.sap.sse.common.TimePoint;

public class MarkPassingForOffsetImpl extends MarkPassingImpl implements MarkPassingForOffsetWaypoint {

    private static final long serialVersionUID = -2168312586388285697L;
    private final MarkPassing markpassingforOffset;

    
    public MarkPassingForOffsetImpl(TimePoint timePoint, Waypoint waypoint, Competitor competitor, MarkPassing markPassingforOffset) {
        super(timePoint, waypoint, competitor, null);
        this.markpassingforOffset = markPassingforOffset;
    }

    @Override
    public MarkPassing getOffsetPassing() {
        return markpassingforOffset;
    }

}


   