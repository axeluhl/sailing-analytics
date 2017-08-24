package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterBoatEvent;
import com.sap.sailing.domain.base.Boat;
import com.sap.sse.common.TimePoint;

public class RegattaLogRegisterBoatEventImpl extends AbstractLogEventImpl<RegattaLogEventVisitor>
        implements RegattaLogRegisterBoatEvent {
    private static final long serialVersionUID = -4531928509653259811L;
    private final Boat boat;

    /**
     * @throws IllegalArgumentException
     *             if {@code boat} is null
     */
    public RegattaLogRegisterBoatEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Boat boat) throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, id);
        checkBoat(boat);
        this.boat = boat;
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code boat} is null
     */
    public RegattaLogRegisterBoatEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Boat boat) throws IllegalArgumentException {
        this(now(), logicalTimePoint, author, randId(), boat);
    }

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }

    private static void checkBoat(Boat boat) throws IllegalArgumentException {
        if (boat == null) {
            throw new IllegalArgumentException("Boat may not be null");
        }
    }

    @Override
    public Boat getBoat() {
        return boat;
    }

    @Override
    public String getShortInfo() {
        return "boat: " + getBoat().toString();
    }
}
