package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sse.common.Util;

public class EventDTO extends EventBaseDTO {
    private static final long serialVersionUID = -7100030301376959817L;

    private Date currentServerTime;

    private List<LeaderboardGroupDTO> leaderboardGroups; // keeps the more specific type accessible in a type-safe way
    
    private List<String> windFinderReviewedSpotsCollectionIds;

    public EventDTO() {
        this(new ArrayList<LeaderboardGroupDTO>());
    }

    protected EventDTO(List<LeaderboardGroupDTO> leaderboardGroups) {
        super(leaderboardGroups);
        this.leaderboardGroups = leaderboardGroups;
        initCurrentServerTime();
    }

    public EventDTO(String name) {
        this(name, new ArrayList<LeaderboardGroupDTO>());
    }

    protected EventDTO(String name, List<LeaderboardGroupDTO> leaderboardGroups) {
        super(name, leaderboardGroups);
        this.leaderboardGroups = leaderboardGroups;
        initCurrentServerTime();
    }

    public boolean isFakeSeries() {
        return leaderboardGroups.size() == 1 && leaderboardGroups.get(0).hasOverallLeaderboard();
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

    public Iterable<UUID> getLeaderboardGroupIds() {
        final List<UUID> updatedEventLeaderboardGroupIds = new ArrayList<>();
        for (LeaderboardGroupDTO leaderboardGroup : this.getLeaderboardGroups()) {
            updatedEventLeaderboardGroupIds.add(leaderboardGroup.getId());
        }
        return updatedEventLeaderboardGroupIds;
    }
    
    public AbstractLeaderboardDTO getLeaderboardByName(String leaderboardName) {
        for (LeaderboardGroupDTO leaderboardGroup : this.getLeaderboardGroups()) {
            for (StrippedLeaderboardDTO leaderboardDTO : leaderboardGroup.getLeaderboards()) {
                if(leaderboardName.equals(leaderboardDTO.name)) {
                    return leaderboardDTO;
                }
            }
        }
        return null;
    }
    
    /**
     * An event may happen in the vicinity of one or more WindFinder (https://www.windfinder.com) weather
     * stations. Which ones those are can be defined using {@link #setWindFinderReviewedSpotsCollection(Iterable)},
     * and this getter returns the IDs last set.
     * 
     * @return always a valid {@link Iterable} which may, though, be empty
     */
    public Iterable<String> getWindFinderReviewedSpotsCollection() {
        final Iterable<String> result;
        if (windFinderReviewedSpotsCollectionIds == null) {
            result = Collections.emptySet();
        } else {
            result = windFinderReviewedSpotsCollectionIds;
        }
        return result;
    }

    /**
     * Set the IDs of the reviewed WindFinder spot collections to consider during this event.
     * Setting this to a non-empty value shall lead to a corresponding display of a WindFinder
     * logo / link on the event's UI representation.
     */
    public void setWindFinderReviewedSpotsCollection(Iterable<String> reviewedSpotsCollectionId) {
        windFinderReviewedSpotsCollectionIds = new ArrayList<>();
        Util.addAll(reviewedSpotsCollectionId, windFinderReviewedSpotsCollectionIds);
    }

}
