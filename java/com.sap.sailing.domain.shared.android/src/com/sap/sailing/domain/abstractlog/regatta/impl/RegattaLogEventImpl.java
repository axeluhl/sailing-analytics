package com.sap.sailing.domain.abstractlog.regatta.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;

public abstract class RegattaLogEventImpl extends AbstractLogEventImpl implements RegattaLogEvent {

    private static final long serialVersionUID = -2557594972618769182L;

    public RegattaLogEventImpl(TimePoint createdAt, RaceLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, List<Competitor> pInvolvedBoats, int pPassId) {
        super(createdAt, author, logicalTimePoint, pId, pInvolvedBoats, pPassId);
    }
}
