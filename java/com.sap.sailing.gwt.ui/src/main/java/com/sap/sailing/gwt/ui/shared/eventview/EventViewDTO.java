package com.sap.sailing.gwt.ui.shared.eventview;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;

public class EventViewDTO extends EventDTO {
    private static final long serialVersionUID = -7100030301376959817L;

    public enum EventType {
        SINGLE_REGATTA, MULTI_REGATTA, SERIES_EVENT
    }

    public enum EventState {
        UPCOMING, RUNNING, FINISHED
    }

    private List<RegattaMetadataDTO> regattas = new ArrayList<>();
    private List<EventReferenceDTO> eventsOfSeries = new ArrayList<>();

    private Date currentServerTime;

    private List<LeaderboardGroupDTO> leaderboardGroups; // keeps the more specific type accessible in a type-safe way

    // TODO: frank, please implement
    private EventType type;
    private EventState state;

    public EventViewDTO() {
        this(new ArrayList<LeaderboardGroupDTO>());
    }

    private EventViewDTO(List<LeaderboardGroupDTO> leaderboardGroups) {
        super(leaderboardGroups);
        this.leaderboardGroups = leaderboardGroups;
        initCurrentServerTime();
    }

    public EventViewDTO(String name) {
        this(name, new ArrayList<LeaderboardGroupDTO>());
    }

    private EventViewDTO(String name, List<LeaderboardGroupDTO> leaderboardGroups) {
        super(name, leaderboardGroups);
        this.leaderboardGroups = leaderboardGroups;
        initCurrentServerTime();

    }

    public boolean isRunning() {
        return getCurrentServerTime().after(startDate) && getCurrentServerTime().before(endDate);
    }

    public boolean isFinished() {
        return getCurrentServerTime().after(endDate);
    }

    private void initCurrentServerTime() {
        currentServerTime = new Date();
    }

    public Date getCurrentServerTime() {
        return currentServerTime;
    }

    public void addLeaderboardGroup(LeaderboardGroupDTO leaderboardGroup) {
        leaderboardGroups.add(leaderboardGroup);
    }

    public List<LeaderboardGroupDTO> getLeaderboardGroups() {
        return leaderboardGroups;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public EventState getState() {
        return state;
    }

    public void setState(EventState state) {
        this.state = state;
    }

    public List<RegattaMetadataDTO> getRegattas() {
        return regattas;
    }

    public List<EventReferenceDTO> getEventsOfSeries() {
        return eventsOfSeries;
    }

    public VenueDTO getVenue() {
        return venue;
    }

    public String getVenueCountry() {
        // FIXME: We need a country?
        return "";
    }

    public String getSeriesName() {
        if(!isFakeSeries()) {
            return null;
        }
        LeaderboardGroupDTO leaderboardGroupDTO = leaderboardGroups.get(0);
        return leaderboardGroupDTO.getDisplayName() != null ? leaderboardGroupDTO.getDisplayName() : leaderboardGroupDTO.getName();
    }
    public String getSeriesIdAsString() {
        return id.toString();
    }
}
