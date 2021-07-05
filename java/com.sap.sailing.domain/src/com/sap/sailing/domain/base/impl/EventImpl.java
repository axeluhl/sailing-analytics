package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalablePosition;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.MarkPositionAtTimePointCache;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackingConnectorInfo;
import com.sap.sailing.domain.tracking.impl.MarkPositionAtTimePointCacheImpl;
import com.sap.sailing.geocoding.ReverseGeocoder;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class EventImpl extends EventBaseImpl implements Event {
    private static final long serialVersionUID = 855135446595485715L;

    private ConcurrentLinkedQueue<LeaderboardGroup> leaderboardGroups;
    
    private ConcurrentMap<String, Boolean> windFinderReviewedSpotsCollectionIds;
    
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
        if (windFinderReviewedSpotsCollectionIds == null) {
            windFinderReviewedSpotsCollectionIds = new ConcurrentHashMap<>();
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
    public Iterable<String> getAllFinderSpotIdsUsedByTrackedRacesInEvent() {
        final Set<String> result = new HashSet<>();
        for (final LeaderboardGroup leaderboardGroup : getLeaderboardGroups()) {
            for (final Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                for (final TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
                    for (final WindSource windTrackerWindSource : trackedRace.getWindSources(WindSourceType.WINDFINDER)) {
                        result.add(windTrackerWindSource.getId().toString());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void setWindFinderReviewedSpotsCollection(Iterable<String> reviewedSpotsCollectionIds) {
        windFinderReviewedSpotsCollectionIds.clear();
        for (final String reviewedSpotsCollectionId : reviewedSpotsCollectionIds) {
            windFinderReviewedSpotsCollectionIds.putIfAbsent(reviewedSpotsCollectionId, true);
        }
    }

    @Override
    public Set<TrackingConnectorInfo> getTrackingConnectorInfos() {
        final Set<TrackingConnectorInfo> result = new HashSet<>();
        for (LeaderboardGroup lbg : this.getLeaderboardGroups()) {
            for (Leaderboard lb : lbg.getLeaderboards()) {
                for (TrackedRace tr : lb.getTrackedRaces()) {
                    final TrackingConnectorInfo trackingConnectorInfo = tr.getTrackingConnectorInfo();
                    if (trackingConnectorInfo != null) {
                        result.add(trackingConnectorInfo);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Position getLocation() {
        final int MAX_NUMBER_OF_POSITION_SAMPLES = 5;
        final Set<Position> positionSamples = new HashSet<>();
        for (final LeaderboardGroup leaderboardGroup : getLeaderboardGroups()) {
            for (final Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                for (final TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
                    if (trackedRace.getStartOfRace() != null) {
                        final Position centerOfCourse = trackedRace.getCenterOfCourse(trackedRace.getStartOfRace());
                        if (centerOfCourse != null) {
                            positionSamples.add(centerOfCourse);
                            if (positionSamples.size() >= MAX_NUMBER_OF_POSITION_SAMPLES) {
                                return getAveragePosition(positionSamples);
                            }
                        }
                    }
                }
            }
        }
        // try to geo-code the venue:
        final ReverseGeocoder geocoder = ReverseGeocoder.INSTANCE;
        // TODO enhance ReverseGeocoder by the use of the forward search, such as http://api.geonames.org/search?name=Kiel&username=sailtracking3, then use Placemark.ByPopulation to sort
        return null;
    }

    private Position getAveragePosition(Set<Position> positionSamples) {
        ScalablePosition sum = null;
        int count = 0;
        for (Position waypointPosition : positionSamples) {
            ScalablePosition p = new ScalablePosition(waypointPosition);
            if (sum == null) {
                sum = p;
            } else {
                sum = sum.add(p);
            }
        }
        final Position result;
        if (sum == null) {
            result = null;
        } else {
            result = sum.divide(count);
        }
        return result;
    }
}
