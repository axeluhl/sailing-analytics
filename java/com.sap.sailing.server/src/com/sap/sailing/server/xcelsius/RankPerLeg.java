package com.sap.sailing.server.xcelsius;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public class RankPerLeg extends Action {
    private final Set<String> competitorNameSet;

    public RankPerLeg(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
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
        final Event event = getEvent();
        final RaceDefinition race = getRace(event);
        final TrackedRace trackedRace = getTrackedRace(event, race);
        final TimePoint time = getTimePoint(trackedRace);
        // Prepare document
        final Document table = getTable("data");
        // Get Legs data
        int i=0;
        NumberFormat numberFormat = new DecimalFormat("00");
        for (final TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
            final Leg leg = trackedLeg.getLeg();
            final String legId = numberFormat.format(++i);
            final String markName = leg.getFrom().getName();
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
                    final int legRank = trackedLeg.getTrackedLeg(competitor).getRank(time);
                    final int posGL = 0; // not yet known
                    final Double gapToLeader = trackedLeg.getTrackedLeg(competitor).getGapToLeaderInSeconds(time);
                    final double legTime = 1./1000.*trackedLeg.getTrackedLeg(competitor).getTimeInMilliSeconds(time);
                    final Speed avgSpeed = trackedLeg.getTrackedLeg(competitor).getAverageSpeedOverGround(time);
                    final SpeedWithBearing speedOVG = trackedLeg.getTrackedLeg(competitor).getSpeedOverGround(time);

                    // Write data
                    addRow();
                    addColumn(legId);
                    addColumn(markName);
                    addColumn(leg.getFrom().getBuoys().iterator().next().getName());
                    addColumn(upOrDownwinLeg);
                    addColumn(competitorName);
                    addColumn(sailID==null?"null":sailID);
                    addColumn(nationality);
                    addColumn("" + overallRank);
                    addColumn("" + legRank);
                    addColumn("" + posGL);
                    addColumn("" + (gapToLeader==null ? "null" : gapToLeader));
                    addColumn("" + legTime);
                    addColumn("" + (avgSpeed == null ? "null" : avgSpeed.getKnots()));
                    addColumn("" + (speedOVG == null ? "null" : speedOVG.getKnots()));
                }
            }
        }
        say(table);
    }
}
