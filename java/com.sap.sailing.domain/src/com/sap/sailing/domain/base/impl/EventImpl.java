package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class EventImpl extends EventBaseImpl implements Event {
    private static final long serialVersionUID = 855135446595485715L;

    private ConcurrentLinkedQueue<LeaderboardGroup> leaderboardGroups;
    
    private final ConcurrentMap<String, Boolean> windFinderReviewedSpotsCollectionIds;
    
    public EventImpl(String name, TimePoint startDate, TimePoint endDate, String venueName, boolean isPublic, UUID id) {
        this(name, startDate, endDate, new VenueImpl(venueName), isPublic, id);
    }

    /**
     * @param venue must not be <code>null</code>
     */
    public EventImpl(String name, TimePoint startDate, TimePoint endDate, Venue venue, boolean isPublic, UUID id) {
        super(name, startDate, endDate, venue, isPublic, id);
        this.leaderboardGroups = new ConcurrentLinkedQueue<>();
        this.windFinderReviewedSpotsCollectionIds = new ConcurrentHashMap<>();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        if (leaderboardGroups == null) {
            leaderboardGroups = new ConcurrentLinkedQueue<>();
        }
    }
    
    public String toString() {
        return getId() + " " + getName() + " " + getVenue().getName() + " " + isPublic();
    }
    
    @Override
    public Iterable<LeaderboardGroup> getLeaderboardGroups() {
        return Collections.unmodifiableCollection(leaderboardGroups);
    }
    
    @Override
    public void setLeaderboardGroups(Iterable<LeaderboardGroup> leaderboardGroups) {
        this.leaderboardGroups.clear();
        Util.addAll(leaderboardGroups, this.leaderboardGroups);
    }

    @Override
    public void addLeaderboardGroup(LeaderboardGroup leaderboardGroup) {
        leaderboardGroups.add(leaderboardGroup);
    }

    @Override
    public boolean removeLeaderboardGroup(LeaderboardGroup leaderboardGroup) {
        return leaderboardGroups.remove(leaderboardGroup);
    }

    @Override
    public Iterable<String> getWindFinderReviewedSpotsCollectionIds() {
        return Collections.unmodifiableSet(windFinderReviewedSpotsCollectionIds.keySet());
    }

    @Override
    public void setWindFinderReviewedSpotsCollection(Iterable<String> reviewedSpotsCollectionIds) {
        windFinderReviewedSpotsCollectionIds.clear();
        for (final String reviewedSpotsCollectionId : reviewedSpotsCollectionIds) {
            windFinderReviewedSpotsCollectionIds.putIfAbsent(reviewedSpotsCollectionId, true);
        }
    }
}
