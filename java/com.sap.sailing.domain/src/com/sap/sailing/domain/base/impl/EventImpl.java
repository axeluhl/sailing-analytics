package com.sap.sailing.domain.base.impl;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

public class EventImpl extends EventBaseImpl implements Event {
    private static final long serialVersionUID = 855135446595485715L;
    
    private final Set<Regatta> regattas;
    
    private final ConcurrentLinkedQueue<LeaderboardGroup> leaderboardGroups;
    
    private final ConcurrentLinkedQueue<URL> imageURLs;
    
    private final ConcurrentLinkedQueue<URL> videoURLs;

    public EventImpl(String name, TimePoint startDate, TimePoint endDate, String venueName, boolean isPublic, UUID id) {
        this(name, startDate, endDate, new VenueImpl(venueName), isPublic, id);
    }
    
    /**
     * @param venue must not be <code>null</code>
     */
    public EventImpl(String name, TimePoint startDate, TimePoint endDate, Venue venue, boolean isPublic, UUID id) {
        super(name, startDate, endDate, venue, isPublic, id);
        this.regattas = new HashSet<Regatta>();
        this.leaderboardGroups = new ConcurrentLinkedQueue<>();
        this.imageURLs = new ConcurrentLinkedQueue<>();
        this.videoURLs = new ConcurrentLinkedQueue<>();
    }
    
    @Override
    public Iterable<Regatta> getRegattas() {
        return Collections.unmodifiableSet(regattas);
    }

    @Override
    public void addRegatta(Regatta regatta) {
        regattas.add(regatta);
    }

    @Override
    public void removeRegatta(Regatta regatta) {
        regattas.remove(regatta);
    }
    
    public String toString() {
        return getId() + " " + getName() + " " + getVenue().getName() + " " + isPublic();
    }
    
    @Override
    public Iterable<URL> getImageURLs() {
        return Collections.unmodifiableCollection(imageURLs);
    }
    
    @Override
    public void addImageURL(URL imageURL) {
        if (!imageURLs.contains(imageURL)) {
            imageURLs.add(imageURL);
        }
    }

    @Override
    public void removeImageURL(URL imageURL) {
        imageURLs.remove(imageURL);
    }

    @Override
    public Iterable<URL> getVideoURLs() {
        return Collections.unmodifiableCollection(videoURLs);
    }

    @Override
    public void addVideoURL(URL videoURL) {
        if (!videoURLs.contains(videoURL)) {
            videoURLs.add(videoURL);
        }
    }

    @Override
    public void removeVideoURL(URL videoURL) {
        videoURLs.remove(videoURL);
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
}
