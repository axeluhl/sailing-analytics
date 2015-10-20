package com.sap.sailing.datamining.impl.data;

import java.util.concurrent.TimeUnit;

import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;

public class RaceOfCompetitorWithContext implements HasRaceOfCompetitorContext {

    private final HasTrackedRaceContext trackedRaceContext;
    private final Competitor competitor;

    public RaceOfCompetitorWithContext(HasTrackedRaceContext trackedRaceContext, Competitor competitor) {
        this.trackedRaceContext = trackedRaceContext;
        this.competitor = competitor;
    }

    @Override
    public HasTrackedRaceContext getTrackedRaceContext() {
        return trackedRaceContext;
    }

    private TrackedRace getTrackedRace() {
        return getTrackedRaceContext().getTrackedRace();
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }
    
    @Override
    public Distance getDistanceToStartLineAtStart() {
        return getTrackedRace().getDistanceToStartLine(getCompetitor(), 0);
    }
    
    @Override
    public Speed getSpeedWhenStarting() {
        return getTrackedRace().getSpeedWhenCrossingStartLine(getCompetitor());
    }
    
    @Override
    public Speed getSpeedTenSecondsBeforeStart() {
        return getTrackedRace().getSpeed(getCompetitor(), TimeUnit.SECONDS.toMillis(10));
    }
    
    @Override
    public Double getRankAtFirstMark() {
        Course course = getTrackedRace().getRace().getCourse();
        Waypoint firstMark = course.getFirstLeg().getTo();
        Competitor competitor = getCompetitor();
        return Double.valueOf(getTrackedRace().getRank(competitor, getTrackedRace().getMarkPassing(competitor, firstMark).getTimePoint()));
    }

    @Override
    public Double getNumberOfTacks() {
        return getNumberOf(ManeuverType.TACK);
    }

    @Override
    public Double getNumberOfJibes() {
        return getNumberOf(ManeuverType.JIBE);
    }

    @Override
    public Double getNumberOfPenaltyCircles() {
        return getNumberOf(ManeuverType.PENALTY_CIRCLE);
    }

    private Double getNumberOf(ManeuverType maneuverType) {
        TrackedRace trackedRace = getTrackedRace();
        double number = 0;
        for (Maneuver maneuver : trackedRace.getManeuvers(getCompetitor(), trackedRace.getStartOfRace(), trackedRace.getEndOfTracking(), false)) {
            if (maneuver.getType() == maneuverType) {
                number++;
            }
        }
        return number;
    }
    
    @Override
    public Double getRankGainsOrLossesBetweenFirstMarkAndFinish() {
        Double rankAtFirstMark = getRankAtFirstMark();
        Double rankAtFinish = getRankAtFinish();
        return rankAtFirstMark - rankAtFinish;
    }

    private Double getRankAtFinish() {
        return Double.valueOf(getTrackedRace().getRank(getCompetitor(), getTrackedRace().getEndOfTracking()));
    }
    
    @Override
    public Double getNormalizedDistanceToStarboardSideAtStart() {
        TrackedRace trackedRace = getTrackedRace();
        TrackedLegOfCompetitor firstTrackedLegOfCompetitor = trackedRace.getTrackedLeg(competitor, trackedRace.getRace().getCourse().getFirstLeg());
        TimePoint competitorStartTime = firstTrackedLegOfCompetitor.getStartTime();
        Double distance = trackedRace.getDistanceFromStarboardSideOfStartLine(getCompetitor(), competitorStartTime).getMeters();
        Double length = trackedRace.getStartLine(competitorStartTime).getLength().getMeters();
        return distance / length;
    }

}