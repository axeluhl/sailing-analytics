package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterEntryEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RegattaLogRegisterEntryEventImpl extends AbstractLogEventImpl<RegattaLogEventVisitor>
        implements RegattaLogRegisterEntryEvent {

    private static final long serialVersionUID = 2759139058776278902L;
    private Competitor competitor;

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public RegattaLogRegisterEntryEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Competitor competitor) throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, id);
        checkCompetitor(competitor);
        this.competitor = competitor;
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public RegattaLogRegisterEntryEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Competitor competitor) throws IllegalArgumentException {
        this(now(), logicalTimePoint, author, randId(), competitor);
    }

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }
    
    private static void checkCompetitor(Competitor competitor) throws IllegalArgumentException {
        if (competitor == null) {
            throw new IllegalArgumentException("Competitor may not be null");
        }
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public String getShortInfo() {
        return "competitor: " + getCompetitor().toString();
    }

}
