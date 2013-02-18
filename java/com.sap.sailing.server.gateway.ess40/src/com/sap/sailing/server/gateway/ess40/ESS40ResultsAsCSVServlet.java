package com.sap.sailing.server.gateway.ess40;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.AbstractCSVHttpServlet;

/**
 * Exports leaderboards of the ESS40 Series to a specific .csv format which is used by a native iOS app to show them.
 * Sample csv files can be found at http://www.extremesailingseries.com/app/results/csv_uploads/
 * 
 * @author Frank
 * 
 */
public class ESS40ResultsAsCSVServlet extends AbstractCSVHttpServlet {
    private static final long serialVersionUID = -3975664653148197608L;
    private static final String PARAM_NAME_LEADERBOARDNAME = "leaderboardName";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RacingEventService racingEventService = getService();
        String pathInfo = request.getPathInfo();
        String leaderboardNameParam = request.getParameter(PARAM_NAME_LEADERBOARDNAME);

        if (leaderboardNameParam == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "You need to specify a leaderboard name using the " + PARAM_NAME_LEADERBOARDNAME + " parameter");
            return;
        }

        Leaderboard leaderboard = racingEventService.getLeaderboardByName(leaderboardNameParam);

        if (pathInfo.equals("/leaderboard") && leaderboard != null) {
            try {
                String fileName = leaderboardNameParam + ".csv";
                List<List<Object>> csv = new ArrayList<List<Object>>();

                setCSVResponseHeader(response, fileName);

                TimePoint timePoint = MillisecondsTimePoint.now();

                // competitors are ordered according to total rank
                List<Competitor> suppressedCompetitors = new ArrayList<Competitor>();
                for (Competitor c : leaderboard.getSuppressedCompetitors()) {
                    suppressedCompetitors.add(c);
                }
                List<Competitor> competitorsFromBestToWorst = leaderboard.getCompetitorsFromBestToWorst(timePoint);
                Map<RaceColumn, List<Competitor>> rankedCompetitorsPerColumn = new HashMap<RaceColumn, List<Competitor>>();
                Iterable<RaceColumn> raceColumns = leaderboard.getRaceColumns();

                for (Competitor competitor : competitorsFromBestToWorst) {
                    if (!suppressedCompetitors.contains(competitor)) {
                        // int totalRank = competitorsFromBestToWorst.indexOf(competitor) + 1;
                        Double totalPoints = leaderboard.getTotalPoints(competitor, timePoint);
                        if (leaderboard.hasCarriedPoints(competitor)) {
                            Double carriedPoints = leaderboard.getCarriedPoints(competitor);
                            if (carriedPoints != null) {
                                totalPoints = totalPoints != null ? totalPoints += carriedPoints : carriedPoints;
                            }
                        }

                        List<Object> csvLine = new ArrayList<Object>();
                        csvLine.add(competitor.getName());
                        csv.add(csvLine);

                        for (RaceColumn raceColumn : raceColumns) {
                            List<Competitor> rankedCompetitorsForColumn = rankedCompetitorsPerColumn.get(raceColumn);
                            if (rankedCompetitorsForColumn == null) {
                                rankedCompetitorsForColumn = leaderboard.getCompetitorsFromBestToWorst(raceColumn,
                                        timePoint);
                                rankedCompetitorsPerColumn.put(raceColumn, rankedCompetitorsForColumn);
                            }
                            Double netRacePoints = leaderboard.getNetPoints(competitor, raceColumn, timePoint);
                            Double totalRacePoints = leaderboard.getTotalPoints(competitor, raceColumn, timePoint);
                            MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, raceColumn,
                                    timePoint);
                            // int rank = rankedCompetitorsForColumn.indexOf(competitor)+1;
                            // TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
                            // if (trackedRace != null) {
                            // int raceRank = trackedRace.getRank(competitor, timePoint);
                            // }
                            // boolean isCorrected = leaderboard.getScoreCorrection().isScoreCorrected(competitor,
                            // raceColumn);
                            boolean isDiscarded = leaderboard.isDiscarded(competitor, raceColumn, timePoint);

                            if (maxPointsReason == null || maxPointsReason == MaxPointsReason.NONE) {
                                if (!isDiscarded) {
                                    if (totalRacePoints != null) {
                                        csvLine.add(totalRacePoints);
                                    }
                                } else {
                                    if (netRacePoints != null) {
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
    
    @Override
    protected <T> void writeCsv (List<List<T>> csv, char separator, boolean quoteStrings, OutputStream output) throws IOException {
        DecimalFormat df = new DecimalFormat("0.##");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
        for (List<T> row : csv) {
            StringBuilder line = new StringBuilder();
            int column = 1;
            for (Iterator<T> iter = row.iterator(); iter.hasNext();) {
                T fieldObject = iter.next();
                String field = String.valueOf(fieldObject).replace("\"", "\"\"");
                if(fieldObject instanceof String && column == 1) {
                    field = '"' + field + '"';
                } else if(fieldObject instanceof Double) {
                    field = df.format(fieldObject);
                }
                line.append(field);
                if (iter.hasNext()) {
                    line.append(separator);
                }
                column++;
            }
            writer.write(line.toString());
            writer.newLine();
        }
        writer.flush();
    }

}
