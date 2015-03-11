package com.sap.sailing.gwt.ui.shared.fakeseries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.shared.general.LabelType;

public class EventSeriesViewDTO implements IsSerializable {
    private UUID id;
    private String displayName;
    private ArrayList<EventSeriesEventDTO> events = new ArrayList<>();
    private String baseUrl;
    private boolean onRemoteServer;
    private String logoImageURL;
    private boolean hasMedia;

    private String leaderboardId;

    public enum EventSeriesState {
        UPCOMING(LabelType.UPCOMMING), RUNNING(LabelType.LIVE), FINISHED(LabelType.NONE), IN_PROGRESS(LabelType.NONE);
        
        private final LabelType stateMarker;

        private EventSeriesState(LabelType stateMarker) {
            this.stateMarker = stateMarker;
        }
        
        public LabelType getStateMarker() {
            return stateMarker;
        }
    }

    // TODO: frank, please implement
    public EventSeriesState state;

    public EventSeriesViewDTO() {
    }
    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<EventSeriesEventDTO> getEvents() {
        return events;
    }

    public void addEvent(EventSeriesEventDTO event) {
        this.events.add(event);
    }

    public String getBaseURL() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isOnRemoteServer() {
        return onRemoteServer;
    }
    
    public void setOnRemoteServer(boolean onRemoteServer) {
        this.onRemoteServer = onRemoteServer;
    }

    public String getLogoImageURL() {
        return logoImageURL;
    }
    
    public void setLogoImageURL(String logoImageURL) {
        this.logoImageURL = logoImageURL;
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
}
