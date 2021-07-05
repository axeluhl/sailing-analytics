package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogExcludeWindSourcesEvent;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class RaceLogExcludeWindSourcesEventImpl extends RaceLogEventImpl implements RaceLogExcludeWindSourcesEvent {
    private static final long serialVersionUID = 7879094280634905183L;
    
    private final Iterable<WindSource> windSourcesToExclude;
    
    public RaceLogExcludeWindSourcesEventImpl(TimePoint createdAt, TimePoint pTimePoint, AbstractLogEventAuthor author,
            Serializable pId, int pPassId, Iterable<WindSource> windSourcesToExclude) {
        super(createdAt, pTimePoint, author, pId, pPassId);
        final Set<WindSource> editableWindSourcesToExclude = new HashSet<>();
        Util.addAll(windSourcesToExclude, editableWindSourcesToExclude);
        this.windSourcesToExclude = editableWindSourcesToExclude;
    }
    
    public RaceLogExcludeWindSourcesEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            int pPassId, Iterable<WindSource> windSourcesToExclude) {
        this(now(), logicalTimePoint, author, randId(), pPassId, windSourcesToExclude);
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Iterable<WindSource> getWindSourcesToExclude() {
        return windSourcesToExclude;
    }

    @Override
    public String getShortInfo() {
        return "windSourcesToExclude=" + getWindSourcesToExclude();
    }
}
