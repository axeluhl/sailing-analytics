package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.AbstractDefineMarkEventImpl;
import com.sap.sailing.domain.base.Mark;
import com.sap.sse.common.TimePoint;

public class RegattaLogDefineMarkEventImpl extends AbstractDefineMarkEventImpl<RegattaLogEventVisitor> implements RegattaLogDefineMarkEvent {
    private static final long serialVersionUID = -5114645637316367845L;
    
    public RegattaLogDefineMarkEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, Mark mark) {
        super(createdAt, author, logicalTimePoint, pId, mark);
    }

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }

}
