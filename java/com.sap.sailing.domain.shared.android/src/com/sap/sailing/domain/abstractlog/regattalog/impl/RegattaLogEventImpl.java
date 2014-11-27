package com.sap.sailing.domain.abstractlog.regattalog.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.regattalog.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regattalog.RegattaLogEventVisitor;
import com.sap.sse.common.TimePoint;

public abstract class RegattaLogEventImpl extends AbstractLogEventImpl<RegattaLogEventVisitor> implements RegattaLogEvent {

    private static final long serialVersionUID = -2557594972618769182L;

    public RegattaLogEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId) {
        super(createdAt, author, logicalTimePoint, pId);
    }
}
