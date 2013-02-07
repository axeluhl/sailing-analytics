package com.sap.sailing.server.gateway.ess40;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.AbstractCSVHttpServlet;

/**
 * Exports leaderboards of the ESS40 Series to a specific .csv format which is used by a native iOS app to show them.
 * Sample csv files can be found at http://www.extremesailingseries.com/app/results/csv_uploads/
 * @author Frank
 *
 */
public class ESS40ResultsAsCSVServlet extends AbstractCSVHttpServlet {
    private static final long serialVersionUID = -3975664653148197608L;
    private static final String PARAM_NAME_LEADERBOARDNAME = "leaderboardName";

    /*
    "Alinghi",9,3,9,3,6,6,10,2,1,11,1,11,1,11,7,5,DNF,0,1,11,8,4,1,11,1,11,6,6,4,8,1,11,2,10,1,11,6,6,2,10,1,11,4,8,2,10,5,7,5,7,5,7,4,8,3,9,6,6,10,2,4,8,2,20
    "Luna Rossa",5,7,5,7,4,8,8,4,5,7,7,5,7,5,1,11,DNF,0,2,10,7,5,4,8,4,8,9,3,6,6,2,10,7,5,4,8,3,9,3,9,3,9,6,6,4,8,1,11,2,10,2,10,5,7,6,6,5,7,9,3,5,7,5,14
    "Oman Air",1,11,4,8,1,11,3,9,9,3,2,10,8,4,6,6,2,10,9,3,11,1,DNF,0,8,4,DNF,0,7,5,4,8,4,8,2,10,1,11,10,2,4,8,1,11,3,9,7,5,3,9,6,6,1,11,4,8,4,8,5,7,8,4,1,22
    "Team GAC Pindar",4,8,3,9,10,2,1,11,7,5,9,3,4,8,10,2,5,7,5,7,4,8,2,10,3,9,3,9,5,7,10,2,6,6,8,4,11,1,8,4,6,6,10,2,8,4,2,10,1,11,1,11,2,10,7,5,3,9,2,10,10,2,4,16
    "Groupe Edmond de Rothschild",3,9,10,2,2,10,2,10,3,9,6,6,9,3,5,7,4,8,8,4,1,11,DNF,0,10,2,8,4,3,9,3,9,9,3,5,7,2,10,1,11,2,10,8,4,6,6,4,8,4,8,7,5,3,9,5,7,8,4,6,6,3,9,9,6
    "Emirates Team New Zealand",7,5,1,11,3,9,7,5,4,8,5,7,6,6,3,9,3,9,3,9,10,2,DNF,0,9,3,5,7,8,4,6,6,3,9,6,6,7,5,7,5,5,7,2,10,RDG,6.5,RDG,6.5,RDS,6.5,RDS,6.5,RDS,6.5,RDS,6.5,RDS,6.5,RDS,6.5,RDS,6.5,RDG,13
    "The Wave, Muscat",8,4,8,4,8,4,6,6,2,10,3,9,10,2,2,10,1,11,11,1,5,7,5,7,5,7,1,11,1,11,5,7,5,7,3,9,9,3,4,8,10,2,5,7,9,3,3,9,9,3,9,3,10,2,2,10,2,10,3,9,9,3,7,10
    "Red Bull Extreme Sailing",2,10,2,10,DNF,0,4,8,11,1,8,4,5,7,4,8,DNF,0,4,8,3,9,3,9,2,10,DNF,0,2,10,7,5,1,11,11,1,5,7,6,6,7,5,3,9,1,11,6,6,6,6,8,4,7,5,8,4,1,11,4,8,7,5,10,4
    "Team Tilt",11,1,7,5,7,5,9,3,10,2,11,1,2,10,11,1,8,4,6,6,9,3,7,5,6,6,2,10,9,3,9,3,10,2,10,2,10,2,9,3,8,4,7,5,7,5,10,2,8,4,4,8,9,3,1,11,9,3,1,11,2,10,6,12
    "Team Extreme",10,2,11,1,9,3,5,7,6,6,10,2,3,9,8,4,6,6,7,5,6,6,6,6,7,5,4,8,10,2,11,1,8,4,7,5,8,4,11,1,11,1,11,1,5,7,8,4,10,2,3,9,6,6,9,3,10,2,7,5,6,6,3,18
    "Niceforyou",6,6,6,6,5,7,11,1,8,4,4,8,11,1,9,3,7,5,10,2,2,10,DNF,0,11,1,7,5,11,1,8,4,11,1,9,3,4,8,5,7,9,3,9,3,10,2,9,3,7,5,10,2,8,4,10,2,7,5,8,4,1,11,8,8
    */
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RacingEventService racingEventService = getService();
        String pathInfo = request.getPathInfo();
        String leaderboardNameParam = request.getParameter(PARAM_NAME_LEADERBOARDNAME);
        
        if (leaderboardNameParam == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "You need to specify a leaderboard name using the "+
                    PARAM_NAME_LEADERBOARDNAME+" parameter");
            return;
        }

        Leaderboard leaderboard = racingEventService.getLeaderboardByName(leaderboardNameParam);

        if(pathInfo.equals("/leaderboard") && leaderboard != null) {
            try {
                String fileName = leaderboardNameParam + ".csv";
                List<List<Object>> csv = new ArrayList<List<Object>>();
                
                setCSVResponseHeader(response, fileName);
                
                TimePoint timePoint = MillisecondsTimePoint.now();

                // competitors are ordered according to total rank
                List<Competitor> suppressedCompetitors = new ArrayList<Competitor>();
                for(Competitor c: leaderboard.getSuppressedCompetitors()) {
                    suppressedCompetitors.add(c);
                }
                List<Competitor> competitorsFromBestToWorst = leaderboard.getCompetitorsFromBestToWorst(timePoint);
                Map<RaceColumn, List<Competitor>> rankedCompetitorsPerColumn = new HashMap<RaceColumn, List<Competitor>>();
                Iterable<RaceColumn> raceColumns = leaderboard.getRaceColumns();

                for (Competitor competitor : competitorsFromBestToWorst) {
                    if(!suppressedCompetitors.contains(competitor)) {
                        int totalRank = competitorsFromBestToWorst.indexOf(competitor) + 1;
                        Double totalPoints = leaderboard.getTotalPoints(competitor, timePoint);
                        if(leaderboard.hasCarriedPoints(competitor)) {
                            Double carriedPoints = leaderboard.getCarriedPoints(competitor);
                            if(carriedPoints != null) {
                                totalPoints = totalPoints != null ? totalPoints += carriedPoints : carriedPoints;  
                            }
                        }
                        
                        List<Object> csvLine = new ArrayList<Object>();
                        csvLine.add(competitor.getName());
                        csv.add(csvLine);
                        
                        for (RaceColumn raceColumn : raceColumns) {
                            List<Competitor> rankedCompetitorsForColumn = rankedCompetitorsPerColumn.get(raceColumn);
                            if (rankedCompetitorsForColumn == null) {
                                rankedCompetitorsForColumn = leaderboard.getCompetitorsFromBestToWorst(raceColumn, timePoint);
                                rankedCompetitorsPerColumn.put(raceColumn, rankedCompetitorsForColumn);
                            }
                            Double netRacePoints = leaderboard.getNetPoints(competitor, raceColumn, timePoint);
                            Double totalRacePoints = leaderboard.getTotalPoints(competitor, raceColumn, timePoint);
                                            MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, raceColumn, timePoint);
                            int rank = rankedCompetitorsForColumn.indexOf(competitor)+1;
                            TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
                            if (trackedRace != null) {
                                int raceRank = trackedRace.getRank(competitor, timePoint);
                            }
                            boolean isDiscarded = leaderboard.isDiscarded(competitor, raceColumn, timePoint);
                            boolean isCorrected = leaderboard.getScoreCorrection().isScoreCorrected(competitor, raceColumn);
    
                            if (maxPointsReason == null || maxPointsReason == MaxPointsReason.NONE) {
                                if (!isDiscarded) {
                                    if(totalRacePoints != null) {
                                        csvLine.add(totalRacePoints);
                                    }
                                } else {
                                    if(netRacePoints != null) {
                                        csvLine.add(netRacePoints);
                                    }
                                }
                            } else {
                                if (!isDiscarded) {
                                    csvLine.add(maxPointsReason.name());
                                } else {
                                    csvLine.add(maxPointsReason.name());
                                }
                            }
                        }
                    }
                }
                writeCsv(csv, ',', true, response.getOutputStream());
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during leaderboard export");
            }
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during leaderboard export");
        }            
    }

}
