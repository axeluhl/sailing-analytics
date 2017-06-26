package com.sap.sailing.domain.abstractlog.shared.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterBoatEvent;
import com.sap.sailing.domain.base.Boat;
import com.sap.sse.common.TimePoint;

public abstract class BaseRegisterBoatEventImpl<VisitorT> extends AbstractLogEventImpl<VisitorT> implements
        RegisterBoatEvent<VisitorT> {
    private static final long serialVersionUID = -224096196692372694L;
    private final Boat boat;

    /**
     * @throws IllegalArgumentException
     *             if {@code boat} is null
     */
    public BaseRegisterBoatEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, Boat boat) throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, pId);
        checkBoat(boat);
        this.boat = boat;
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code boat} is null
     */
    public BaseRegisterBoatEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Boat boat) throws IllegalArgumentException {
        this(now(), logicalTimePoint, author, randId(), boat);
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
