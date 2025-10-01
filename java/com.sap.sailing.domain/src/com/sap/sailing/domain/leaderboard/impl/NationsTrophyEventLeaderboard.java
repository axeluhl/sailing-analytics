package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.metering.CPUMeter;

public class NationsTrophyEventLeaderboard extends AbstractSimpleLeaderboardImpl implements MetaLeaderboard {
    private static final long serialVersionUID = 5744928408390959642L;
    private static final DefaultFleetImpl defaultFleet = new DefaultFleetImpl();
    
    private final Event event;

    public NationsTrophyEventLeaderboard(Event event, ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        super(resultDiscardingRule);
        this.event = event;
    }

    @Override
    public Iterable<Boat> getAllBoats() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Pair<Iterable<RaceDefinition>, Iterable<Competitor>> getAllCompetitorsWithRaceDefinitionsConsidered() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Competitor> getAllCompetitors(RaceColumn raceColumn, Fleet fleet) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boat getBoatOfCompetitor(Competitor competitor, RaceColumn raceColumn, Fleet fleet) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Fleet getFleet(String fleetName) {
        return defaultFleet;
    }

    @Override
    public int getTrackedRank(Competitor competitor, RaceColumn race, TimePoint timePoint,
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Iterable<RaceColumn> getRaceColumns() {
        // TODO find out the days of the event and create a column for each day
        return null;
    }

    @Override
    public Competitor getCompetitorByIdAsString(String idAsString) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getDelayToLiveInMillis() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<TrackedRace> getTrackedRaces() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScoringScheme getScoringScheme() {
        return new LowPointA82Only();
    }

    @Override
    public Iterable<CourseArea> getCourseAreas() {
        return event.getVenue().getCourseAreas();
    }

    @Override
    public LeaderboardType getLeaderboardType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isResultsAreOfficial(RaceColumn raceColumn, Fleet fleet) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName() {
        return "Nations Trophy "+event.getName();
    }

    @Override
    public CPUMeter getCPUMeter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Leaderboard> getLeaderboards() {
        return event.getLeaderboards();
    }
}
