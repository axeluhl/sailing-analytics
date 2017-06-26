package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterBoatEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.BaseRegisterBoatEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RegattaLogRegisterBoatEventImpl extends BaseRegisterBoatEventImpl<RegattaLogEventVisitor>
        implements RegattaLogRegisterBoatEvent {
    private static final long serialVersionUID = -4531928509653259811L;

    /**
     * @throws IllegalArgumentException
     *             if {@code boat} is null
     */
    public RegattaLogRegisterBoatEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Boat boat) throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, id, boat);
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code boat} is null
     */
    public RegattaLogRegisterBoatEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Boat boat) throws IllegalArgumentException {
        super(logicalTimePoint, author, boat);
    }

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
