package com.sap.sailing.domain.base.configuration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

/**
 * Interface holding information about tablet's (RCApp) configuration.
 */
public interface DeviceConfiguration extends WithID, WithQualifiedObjectIdentifier, Named {
    @Override
    UUID getId();
    
    /**
     * Course area names this tablet is allowed to log on.
     */
    List<String> getAllowedCourseAreaNames();
    
    /**
     * She is getting the result mails.
     */
    String getResultsMailRecipient();
    
    /**
     * Course names allowed in the By-Name-Course-Designer
     */
    List<String> getByNameCourseDesignerCourseNames();
    
    /**
     * Default configuration for all races without a custom {@link RegattaConfiguration}
     */
    RegattaConfiguration getRegattaConfiguration();
    
    /**
     * Copy me.
     */
    DeviceConfiguration copy();
    
    /**
     * The optional ID of an {@link EventBase} object that can be resolved by the server and which has a
     * {@link EventBase#getVenue() venue} with {@link Venue#getCourseAreas() course areas}. If a valid event ID is set
     * here, any {@link #getCourseAreaId() course area} specified by ID should be part of the event's venue, and UIs as
     * well as service methods taking in a {@link DeviceConfiguration} shall make sure to validate this constraint.
     * Likewise, if an event ID of an existing event is specified, the {@link #getAllowedCourseAreaNames()} results are
     * expected to match {@link CourseArea#getName() course area names} of those course areas in the event's venue.
     */
    Optional<UUID> getEventId();
    
    /**
     * @param eventId if {@code null}, an {@link Optional#empty() empty} optional will result from {@link #getEventId()}.
     */
    void setEventId(UUID eventId);
    
    /**
     * If a valid {@link #getEventId() event ID} is specified, a course area ID can be specified here to default the
     * log-in to that course area. This may come in handy if devices are to be pre-configured for hand-out to a specific
     * course area only. Make sure that this ID references a course area whose name is part of the result of calling
     * {@link #getAllowedCourseAreaNames()}.
     */
    Optional<UUID> getCourseAreaId();
    
    /**
     * @param courseAreaId if {@code null}, an {@link Optional#empty() empty} optional will result from {@link #getCourseAreaId()}.
     */
    void setCourseAreaId(UUID courseAreaId);
    
    /**
     * During the Race Manager App's login process the user is asked to choose a "role" such as "Race Officer on the
     * Water" or "Shore Control." These map to numeric priorities, with lesser numerical values taking higher
     * precedence. "Race Officer on the Water" would map to the number 1, "Shore Control" to 2. An exceptional priority
     * of 3 may be translated by the app into "Demo Mode" if that is permissible. The field may not be set, then
     * returning an empty {@link Optional}, meaning that this device configuration leaves it up for the user to choose
     * the priority during the login process.
     */
    Optional<Integer> getPriority();

    /**
     * Sets the {@link #getPriority() priority}.
     */
    void setPriority(Integer priority);
}
