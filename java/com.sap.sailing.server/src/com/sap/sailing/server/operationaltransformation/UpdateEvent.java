package com.sap.sailing.server.operationaltransformation;

import java.net.URL;
import java.util.UUID;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sse.common.TimePoint;

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
    private final URL logoImageURL;
    private final Iterable<URL> imageURLs;
    private final Iterable<URL> videoURLs;
    private final Iterable<URL> sponsorImageURLs;

    public UpdateEvent(UUID id, String eventName, String eventDescription, TimePoint startDate, TimePoint endDate,
            String venueName, boolean isPublic, Iterable<UUID> leaderboardGroupIds, URL logoImageURL,
            URL officialWebsiteURL, Iterable<URL> imageURLs, Iterable<URL> videoURLs, Iterable<URL> sponsorImageURLs) {
        super(id);
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.venueName = venueName;
        this.isPublic = isPublic;
        this.leaderboardGroupIds = leaderboardGroupIds;
        this.officialWebsiteURL = officialWebsiteURL;
        this.logoImageURL = logoImageURL;
        this.imageURLs = imageURLs;
        this.videoURLs = videoURLs;
        this.sponsorImageURLs = sponsorImageURLs;
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
                leaderboardGroupIds, officialWebsiteURL, logoImageURL, imageURLs, videoURLs, sponsorImageURLs);
        return null;
    }
}
