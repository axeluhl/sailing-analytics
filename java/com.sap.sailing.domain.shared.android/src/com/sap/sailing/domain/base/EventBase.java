package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.Renamable;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WithID;

/**
 * Base interface for an Event consisting of all static information, which might be shared
 * by the server and an Android application.
 */
public interface EventBase extends Named, Renamable, WithID {

    /**
     * @return a non-<code>null</code> venue for this event
     */
    Venue getVenue();

    /**
     *  @return the start date of the event 
     */
    TimePoint getStartDate();

    void setStartDate(TimePoint startDate);

    /**
     *  @return the end date of the event 
     */
    TimePoint getEndDate();

    void setEndDate(TimePoint startDate);

    boolean isPublic();

    void setPublic(boolean isPublic);
}
