package com.sap.sailing.server.operationaltransformation;

import java.net.URL;
import java.util.UUID;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;

/**
 * Creates an {@link Event} in the server, with a new venue and an empty course area list.
 * See the {@link AddCourseArea} operation for adding course areas to the event's venue.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CreateEvent extends AbstractEventOperation<Event> {
    private static final long serialVersionUID = 308389324918359960L;
    private final String venue;
    private final TimePoint startDate;
    private final TimePoint endDate;
    private final boolean isPublic;
    private final String eventName;
    private final String eventDescription;
    private final Iterable<URL> videoURLs;
    private final Iterable<URL> imageURLs;
    private final Iterable<URL> sponsorImageURLs;
    private final URL logoImageURL;
    private final URL officialWebsiteURL;
    
    public CreateEvent(String eventName, String eventDescription, TimePoint startDate, TimePoint endDate, String venue,
            boolean isPublic, UUID id, Iterable<URL> imageURLs, Iterable<URL> videoURLs, Iterable<URL> sponsorImageURLs, URL logoImageURL, URL officialWebsiteURL) {
        super(id);
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.venue = venue;
        this.isPublic = isPublic;
        this.imageURLs = imageURLs;
        this.videoURLs = videoURLs;
        this.sponsorImageURLs = sponsorImageURLs;
        this.logoImageURL = logoImageURL;
        this.officialWebsiteURL = officialWebsiteURL;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<Event> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<Event> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    protected String getEventName() {
        return eventName;
    }

    @Override
    public Event internalApplyTo(RacingEventService toState) {
        return toState.createEventWithoutReplication(getEventName(), eventDescription, startDate, endDate, venue, isPublic,
                getId(), imageURLs, videoURLs, sponsorImageURLs, logoImageURL, officialWebsiteURL);
    }

}
