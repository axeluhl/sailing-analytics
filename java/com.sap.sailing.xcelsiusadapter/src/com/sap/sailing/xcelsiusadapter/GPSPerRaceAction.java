package com.sap.sailing.xcelsiusadapter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public class GPSPerRaceAction extends HttpAction {
    private final Set<String> competitorNameSet;

    public GPSPerRaceAction(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
        super(req, res, service, maxRows);
        String[] competitors = req.getParameterValues("competitor");
        if (competitors == null) {
            competitorNameSet = null;
        } else {
            competitorNameSet = new HashSet<String>();
            for (String competitorName : competitors) {
                competitorNameSet.add(competitorName);
            }
        }
    }

    public void perform() throws Exception {
        // Get data from request
        final Regatta regatta = getRegatta();
        final RaceDefinition race = getRace(regatta);
        final TrackedRace trackedRace = getTrackedRace(regatta, race);
        if (trackedRace != null) {
            final TimePoint time = getTimePoint(trackedRace);
            // Prepare document
            final Document table = getTable("data");
            // Get Legs data
            int i = 0;
            NumberFormat numberFormat = new DecimalFormat("00");
            TrackedLeg previousLeg = null;
            for (final TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
                final Leg leg = trackedLeg.getLeg();
                final String legId = numberFormat.format(++i);
                final String markName = leg.getTo().getName();
                final String upOrDownwinLeg = trackedLeg.isUpOrDownwindLeg(time) ? "U" : "D";
                LinkedHashMap<Competitor, Integer> ranks = trackedLeg.getRanks(time);
                // Get competitor data
                for (final Competitor competitor : ranks.keySet()) {
                    if (competitorNameSet == null || competitorNameSet.contains(competitor.getName())) {
                        // Get data
                        final String competitorName = competitor.getName();
                        final String nationality = competitor.getTeam().getNationality().getThreeLetterIOCAcronym();
                        final String sailID = competitor.getBoat().getSailID();
                        final int overallRank = trackedRace.getRank(competitor);
                        TrackedLegOfCompetitor trackedLegOfCompetitor = trackedLeg.getTrackedLeg(competitor);
                        final int legRank = trackedLegOfCompetitor.getRank(time);
                        int posGL = 0;
                        if (previousLeg != null) {
                            posGL = legRank - previousLeg.getTrackedLeg(competitor).getRank(time);
                        }
                        final Double gapToLeader = trackedLegOfCompetitor.getGapToLeaderInSeconds(time);
                        final Long timeInMilliSeconds = trackedLegOfCompetitor.getTimeInMilliSeconds(time);
                        final double legTime = 1. / 1000. * (timeInMilliSeconds==null?0:timeInMilliSeconds);
                        final Speed avgSpeed = trackedLegOfCompetitor.getAverageSpeedOverGround(time);
                        final Distance distanceSailed = trackedLegOfCompetitor.getDistanceTraveled(time);

                        // Write data
                        addRow();
                        addColumn(legId);
                        addColumn(markName);
                        addColumn(leg.getTo().getMarks().iterator().next().getName());
                        addColumn(upOrDownwinLeg);
                        addColumn(competitorName);
                        addColumn(sailID == null ? "null" : sailID);
                        addColumn(nationality);
                        addColumn("" + overallRank);
                        addColumn("" + legRank);
                        addColumn("" + posGL);
                        addColumn("" + (gapToLeader == null ? "null" : gapToLeader));
                        addColumn("" + legTime);
                        addColumn("" + (avgSpeed == null ? "null" : avgSpeed.getKnots()));
                        addColumn("" + (distanceSailed == null ? "null" : distanceSailed.getMeters()));

                        // position and tracking-related columns (see Kersten's mail of 2011-10-07T11:32:00CEST)
                        GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                        GPSFixMoving fix = track.getFirstFixAtOrAfter(time);
                        addColumn("" + (fix == null ? "null" : fix.getPosition().getLngDeg()));
                        addColumn("" + (fix == null ? "null" : fix.getPosition().getLatDeg()));
                        SpeedWithBearing speedOverGround = trackedLegOfCompetitor.getSpeedOverGround(time);
                        addColumn("" + (speedOverGround == null ? "null" : speedOverGround.getKnots()));
                        Double estimatedTimeToNextMarkInSeconds = trackedLegOfCompetitor
                                .getEstimatedTimeToNextMarkInSeconds(time);
                        addColumn(""
                                + (estimatedTimeToNextMarkInSeconds == null ? "null" : estimatedTimeToNextMarkInSeconds));
                        Speed velocityMadeGood = trackedLegOfCompetitor.getVelocityMadeGood(time);
                        addColumn("" + (velocityMadeGood == null ? "null" : velocityMadeGood.getKnots()));
                    }
                }
                previousLeg = trackedLeg;
            }
            say(table);
        }
    }
}
