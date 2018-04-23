package com.sap.sailing.server.operationaltransformation;

import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;
import com.sap.sse.shared.media.ImageDescriptor;
import com.sap.sse.shared.media.VideoDescriptor;

public class UpdateEvent extends AbstractEventOperation<Void> {
    private static final long serialVersionUID = -8271559266421161532L;
    private final String venueName;
    private final TimePoint startDate;
    private final TimePoint endDate;
    private final boolean isPublic;
    private final Iterable<UUID> leaderboardGroupIds;
    private final String eventName;
    private final String eventDescription;
    private final URL officialWebsiteURL;
    private final URL baseURL;
    private final Map<Locale, URL> sailorsInfoWebsiteURLs;
    private final Iterable<ImageDescriptor> images;
    private final Iterable<VideoDescriptor> videos;
    private final Iterable<String> windFinderReviewedSpotCollectionIds;

    public UpdateEvent(UUID id, String eventName, String eventDescription, TimePoint startDate, TimePoint endDate,
            String venueName, boolean isPublic, Iterable<UUID> leaderboardGroupIds, URL officialWebsiteURL, URL baseURL, 
            Map<Locale, URL> sailorsInfoWebsiteURLs, Iterable<ImageDescriptor> images, Iterable<VideoDescriptor> videos, Iterable<String> windFinderReviewedSpotCollectionIds) {
        super(id);
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.venueName = venueName;
        this.isPublic = isPublic;
        this.leaderboardGroupIds = leaderboardGroupIds;
        this.officialWebsiteURL = officialWebsiteURL;
        this.baseURL = baseURL;
        this.sailorsInfoWebsiteURLs = sailorsInfoWebsiteURLs;
        this.images = images;
        this.videos = videos;
        this.windFinderReviewedSpotCollectionIds = windFinderReviewedSpotCollectionIds;
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

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.updateEvent(getId(), eventName, eventDescription, startDate, endDate, venueName, isPublic,
                leaderboardGroupIds, officialWebsiteURL, baseURL, sailorsInfoWebsiteURLs, images, videos,
                windFinderReviewedSpotCollectionIds);
        return null;
    }
}
