package com.sap.sailing.xcelsiusadapter;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public class RankPerRace extends Action {
    private final Set<String> competitorNameSet;

    public RankPerRace(HttpServletRequest req, HttpServletResponse res, RacingEventService service, int maxRows) {
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
            List<Leg> legs = race.getCourse().getLegs();
            TrackedLeg trackedLeg = trackedRace.getTrackedLeg(legs.get(legs.size() - 1));
            LinkedHashMap<Competitor, Integer> ranks = trackedLeg.getRanks(time);
            // Get competitor data
            for (final Map.Entry<Competitor, Integer> competitorAndRank : ranks.entrySet()) {
                if (competitorNameSet == null || competitorNameSet.contains(competitorAndRank.getKey().getName())) {
                    // Get data
                    final String competitorName = competitorAndRank.getKey().getName();
                    final String nationality = competitorAndRank.getKey().getTeam().getNationality()
                            .getThreeLetterIOCAcronym();
                    final String sailID = competitorAndRank.getKey().getBoat().getSailID();
                    final int overallRank = competitorAndRank.getValue();

                    // Write data
                    addRow();
                    addColumn("" + overallRank);
                    addColumn(nationality);
                    addColumn(sailID == null ? "null" : sailID);
                    addColumn(competitorName);
                    addColumn(URLEncoder.encode(competitorName, "UTF-8"));
                }
            }
            say(table);
        }
    }
}
