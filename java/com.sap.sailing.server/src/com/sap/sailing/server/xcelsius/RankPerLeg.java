package com.sap.sailing.server.xcelsius;

import java.net.URLEncoder;

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

import org.jdom.Document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RankPerLeg extends Action {
  public RankPerLeg(HttpServletRequest req, HttpServletResponse res, RacingEventService service) {
    super(req, res, service);
  }

  public void perform() throws Exception {
    /*
     * Get data from request
     */
    final Event          event       = getEvent();

    final RaceDefinition race        = getRace(event);

    final TrackedRace    trackedRace = getTrackedRace(event, race);

    final TimePoint      time        = getTimePoint(trackedRace);

    /*
     * Prepare document
     */
    final Document       table       = getTable("data");

    /*
     * Get Legs data
     */
    for (final TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
      final Leg    leg            = trackedLeg.getLeg();

      final String legId          = "" + leg.getFrom().getId();

      final String markName       = leg.getFrom().getName();

      final String upOrDownwinLeg = trackedLeg.isUpOrDownwindLeg(time) ? "U" : "D";

      /*
       * Get competitor data
       */
      for (final Competitor competitor : race.getCompetitors()) {
        /*
         * Get data
         */
        final String competitorName = URLEncoder.encode(competitor.getName(), "UTF-8");

        final String nationality    = competitor.getTeam().getNationality().getThreeLetterIOCAcronym();

        final int    overallRank    = trackedRace.getRank(competitor);

        final int    legRank        = trackedLeg.getTrackedLeg(competitor).getRank(time);

        final int    posGL          = 0; // not yet known

        final Double gapToLeader    = trackedLeg.getTrackedLeg(competitor).getGapToLeaderInSeconds(time);

        final double legTime        = 0; // not yet known

        final Speed avgSpeed       = trackedLeg.getTrackedLeg(competitor).getAverageSpeedOverGround(time);

        final SpeedWithBearing speedOVG  = trackedLeg.getTrackedLeg(competitor).getSpeedOverGround(time);

        /*
         * Write data
         */
        addRow();
        addColumn(legId);
        addColumn(markName);
        addColumn(upOrDownwinLeg);
        addColumn(competitorName);
        addColumn(nationality);
        addColumn("" + overallRank);
        addColumn("" + legRank);
        addColumn("" + posGL);
        addColumn("" + gapToLeader);
        addColumn("" + legTime);
        addColumn("" + (avgSpeed==null?"null":avgSpeed.getKnots()));
        addColumn("" + (speedOVG==null?"null":speedOVG.getKnots()));
      }

      say(table);
    }
  }
}
