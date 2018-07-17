package com.sap.sailing.gwt.home.communication.fakeseries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.gwt.home.communication.event.EventAndLeaderboardReferenceWithStateDTO;
import com.sap.sailing.gwt.home.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.home.communication.event.HasLogo;
import com.sap.sailing.gwt.home.communication.event.LabelType;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

public class EventSeriesViewDTO implements Result, HasLogo {
    private UUID leaderboardGroupUUID;
    private String displayName;
    private ArrayList<EventMetadataDTO> eventsAscending = new ArrayList<>();
    private ArrayList<EventAndLeaderboardReferenceWithStateDTO> eventsAndRegattasOfSeriesAscending = new ArrayList<>();
    private ImageDTO logoImage;
    private boolean hasMedia;
    private boolean hasAnalytics;
    private String leaderboardId;

    public enum EventSeriesState {
        UPCOMING(LabelType.UPCOMING), RUNNING(LabelType.LIVE), FINISHED(LabelType.FINISHED), IN_PROGRESS(LabelType.PROGRESS);
        
        private final LabelType stateMarker;

        private EventSeriesState(LabelType stateMarker) {
            this.stateMarker = stateMarker;
        }
        
        public LabelType getStateMarker() {
            return stateMarker;
        }
    }

    public EventSeriesState state;

    public UUID getLeaderboardGroupUUID() {
        return leaderboardGroupUUID;
    }
    
    public void setLeaderboardGroupUUID(UUID leaderboardGroupUUID) {
        this.leaderboardGroupUUID = leaderboardGroupUUID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<EventMetadataDTO> getEventsAscending() {
        return eventsAscending;
    }
    
    public List<EventMetadataDTO> getEventsDescending() {
        return reverseList(eventsAscending);
    }

    public void addEvent(EventMetadataDTO event) {
        this.eventsAscending.add(event);
    }
    
    public List<EventAndLeaderboardReferenceWithStateDTO> getEventsAndRegattasOfSeriesAscending() {
        return eventsAndRegattasOfSeriesAscending;
    }
    
    public List<EventAndLeaderboardReferenceWithStateDTO> getEventsAndRegattasOfSeriesDescending() {
        return reverseList(eventsAndRegattasOfSeriesAscending);
    }

    public void addEventAndLeaderboard(EventAndLeaderboardReferenceWithStateDTO event) {
        this.eventsAndRegattasOfSeriesAscending.add(event);
    }

    private <T> List<T> reverseList(final ArrayList<T> ordered) {
        final ArrayList<T> reversed = new ArrayList<>(ordered);
        Collections.reverse(reversed);
        return reversed;
    }

    public ImageDTO getLogoImage() {
        return logoImage;
    }
    
    public void setLogoImage(ImageDTO logoImage) {
        this.logoImage = logoImage;
    }

    public boolean isHasMedia() {
        return hasMedia;
    }

    public void setHasMedia(boolean hasMedia) {
        this.hasMedia = hasMedia;
    }

    public String getLeaderboardId() {
        return leaderboardId;
    }

    public void setLeaderboardId(String leaderboardGroup) {
        this.leaderboardId = leaderboardGroup;
    }

    public EventSeriesState getState() {
        return state;
    }

    public void setState(EventSeriesState state) {
        this.state = state;
    }

    public boolean isHasAnalytics() {
        return hasAnalytics;
    }

    public void setHasAnalytics(boolean hasAnalytics) {
        this.hasAnalytics = hasAnalytics;
    }
}
