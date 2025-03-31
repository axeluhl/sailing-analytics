package com.sap.sailing.domain.abstractlog.impl;

import java.io.Serializable;
import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class AbstractLogEventImpl<VisitorT> implements AbstractLogEvent<VisitorT> {

    private static final long serialVersionUID = -5810258278984777732L;

    private final TimePoint createdAt;
    private final TimePoint logicalTimePoint;
    private final Serializable id;
    private final AbstractLogEventAuthor author;

    public AbstractLogEventImpl(TimePoint createdAt, TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Serializable pId) {
        this.createdAt = createdAt;
        this.author = author;
        this.logicalTimePoint = logicalTimePoint;
        this.id = pId;
    }
    
    /**
     * To be used by constructors creating new events as the value for {@link #createdAt}.
     */
    protected static final TimePoint now() {
        return MillisecondsTimePoint.now();
    }
    
    /**
     * To be used by constructors creating new events as the value for {@link #id}.
     */
    protected static final Serializable randId() {
        return UUID.randomUUID();
    }

    @Override
    public TimePoint getCreatedAt() {
        return createdAt;
    }

    /*
     * Redirects to getCreatedAt()
     */
    @Override
    public TimePoint getTimePoint() {
        return getCreatedAt();
    }
    
    @Override
    public TimePoint getLogicalTimePoint() {
        return logicalTimePoint;
    }

    @Override
    public Serializable getId() {
        return id;
    }
    
    @Override
    public AbstractLogEventAuthor getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": createdAt: " + getCreatedAt() + ", logicalTimePoint: " + getLogicalTimePoint()
                + ", id: " + getId() + ", author: "+ getAuthor();
    }
    
    @Override
    public String getShortInfo() {
        return "";
    }
}
