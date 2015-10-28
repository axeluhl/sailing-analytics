package com.sap.sailing.domain.abstractlog.shared.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.shared.events.DefineMarkEvent;
import com.sap.sailing.domain.base.Mark;
import com.sap.sse.common.TimePoint;

public abstract class AbstractDefineMarkEventImpl<VisitorT> extends AbstractLogEventImpl<VisitorT> implements
DefineMarkEvent<VisitorT> {

    private static final long serialVersionUID = 5865183507521850320L;

    private final Mark mark;
    
    public AbstractDefineMarkEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId, Mark mark) {
        super(createdAt, author, logicalTimePoint, pId);
        this.mark = mark;
    }

    @Override
    public Mark getMark() {
        return mark;
    }

    @Override
    public String getShortInfo() {
        return "mark: "+mark.toString();
    }
}
