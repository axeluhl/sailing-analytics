package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.BaseRegisterCompetitorEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RegattaLogRegisterCompetitorEventImpl extends BaseRegisterCompetitorEventImpl<RegattaLogEventVisitor>
implements RegattaLogRegisterCompetitorEvent {
    private static final long serialVersionUID = -5114645637316367845L;
    
    public RegattaLogRegisterCompetitorEventImpl(TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint logicalTimePoint, Serializable id, Competitor competitor) {
        super(createdAt, author, logicalTimePoint, id, competitor);
    }

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
