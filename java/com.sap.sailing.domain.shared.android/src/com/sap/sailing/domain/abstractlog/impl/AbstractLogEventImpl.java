package com.sap.sailing.domain.abstractlog.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.common.TimePoint;

public abstract class AbstractLogEventImpl<VisitorT> implements AbstractLogEvent<VisitorT> {

    private static final long serialVersionUID = -5810258278984777732L;

    private final TimePoint createdAt;
    private final TimePoint logicalTimePoint;
    private final Serializable id;
    private final AbstractLogEventAuthor author;

    public AbstractLogEventImpl(TimePoint createdAt, AbstractLogEventAuthor author, TimePoint logicalTimePoint,
            Serializable pId) {
        this.createdAt = createdAt;
        this.author = author;
        this.logicalTimePoint = logicalTimePoint;
        this.id = pId;
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
