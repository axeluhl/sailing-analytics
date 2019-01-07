package com.sap.sailing.server.operationaltransformation;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;
import com.sap.sse.shared.media.ImageDescriptor;
import com.sap.sse.shared.media.VideoDescriptor;

/**
 * Creates an {@link Event} in the server, with a new venue and an empty course area list.
 * See the {@link AddCourseAreas} operation for adding course areas to the event's venue.
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
    private final Iterable<ImageDescriptor> images;
    private final Iterable<VideoDescriptor> videos;
    private final URL officialWebsiteURL;
    private final URL baseURL;
    private final Map<Locale, URL> sailorsInfoWebsiteURLs;
    private final Iterable<UUID> leaderboardGroupIds;
    
    public CreateEvent(String eventName, String eventDescription, TimePoint startDate, TimePoint endDate, String venue,
            boolean isPublic, UUID id, URL officialWebsiteURL, URL baseURL, Map<Locale, URL> sailorsInfoWebsiteURLs,
            Iterable<ImageDescriptor> images, Iterable<VideoDescriptor> videos, Iterable<UUID> leaderboardGroupIds) {
        super(id);
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.venue = venue;
        this.isPublic = isPublic;
        this.officialWebsiteURL = officialWebsiteURL;
        this.baseURL = baseURL;
        this.sailorsInfoWebsiteURLs = sailorsInfoWebsiteURLs;
        this.images = images;
        this.videos = videos;
        this.leaderboardGroupIds = leaderboardGroupIds;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    protected String getEventName() {
        return eventName;
    }

    @Override
    public Event internalApplyTo(RacingEventService toState) {
        final Event result = toState.createEventWithoutReplication(getEventName(), eventDescription, startDate, endDate, venue, isPublic,
                getId(), officialWebsiteURL, baseURL, sailorsInfoWebsiteURLs, images, videos);
        List<LeaderboardGroup> lgl = new ArrayList<>();
        for (final UUID lgid : leaderboardGroupIds) {
            final LeaderboardGroup lg = toState.getLeaderboardGroupByID(lgid);
            if (lg != null) {
                lgl.add(lg);
            }
        }
        result.setLeaderboardGroups(lgl);
        return result;
    }

}
