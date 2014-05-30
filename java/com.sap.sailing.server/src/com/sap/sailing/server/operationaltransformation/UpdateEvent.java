package com.sap.sailing.server.operationaltransformation;

import java.net.URL;
import java.util.UUID;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateEvent extends AbstractEventOperation<Void> {
    private static final long serialVersionUID = -8271559266421161532L;
    private final String venueName;
    private final TimePoint startDate;
    private final TimePoint endDate;
    private final boolean isPublic;
    private final Iterable<UUID> leaderboardGroupIds;
    private final String eventName;
    private final Iterable<URL> imageURLs;
    private final Iterable<URL> videoURLs;

    public UpdateEvent(UUID id, String eventName, TimePoint startDate, TimePoint endDate, String venueName,
            boolean isPublic, Iterable<UUID> leaderboardGroupIds, Iterable<URL> imageURLs, Iterable<URL> videoURLs) {
        super(id);
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.venueName = venueName;
        this.isPublic = isPublic;
        this.leaderboardGroupIds = leaderboardGroupIds;
        this.imageURLs = imageURLs;
        this.videoURLs = videoURLs;
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
        toState.updateEvent(getId(), eventName, startDate, endDate, venueName, isPublic, leaderboardGroupIds, imageURLs, videoURLs);
        return null;
    }
}
