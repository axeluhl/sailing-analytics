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
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.AbstractCSVHttpServlet;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

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
    private static final String PARAM_NAME_RESULTSTATE = "resultState";
    private enum ResultStates { Live, Preliminary, Final };
    private final ResultStates DEFAULT_RESULT_STATE = ResultStates.Final;  

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

                ResultStates resultState = resolveRequestedResultState(request.getParameter(PARAM_NAME_RESULTSTATE));
                TimePoint requestTimePoint = MillisecondsTimePoint.now();
                TimePoint resultTimePoint = calculateTimePointForResultState(leaderboard, resultState);

                // competitors are ordered according to total rank
                List<Competitor> suppressedCompetitors = new ArrayList<Competitor>();
                for (Competitor c : leaderboard.getSuppressedCompetitors()) {
                    suppressedCompetitors.add(c);
                }
                List<Competitor> competitorsFromBestToWorst = leaderboard.getCompetitorsFromBestToWorst(resultTimePoint != null ? resultTimePoint : requestTimePoint);
                Map<RaceColumn, List<Competitor>> rankedCompetitorsPerColumn = new HashMap<RaceColumn, List<Competitor>>();
                Iterable<RaceColumn> raceColumns = leaderboard.getRaceColumns();

                if(resultTimePoint != null) {
                    for (Competitor competitor : competitorsFromBestToWorst) {
                        if (!suppressedCompetitors.contains(competitor)) {
                            // int totalRank = competitorsFromBestToWorst.indexOf(competitor) + 1;
                            Double totalPoints = leaderboard.getTotalPoints(competitor, resultTimePoint);
                            if (leaderboard.hasCarriedPoints(competitor)) {
                                Double carriedPoints = leaderboard.getCarriedPoints(competitor);
                                if (carriedPoints != null) {
                                    totalPoints = totalPoints != null ? totalPoints += carriedPoints : carriedPoints;
                                }
                            }
    
                            List<Object> csvLine = new ArrayList<Object>();
                            String competitorDisplayName = leaderboard.getDisplayName(competitor);
                            csvLine.add(competitorDisplayName != null ? competitorDisplayName : competitor.getName());
                            csv.add(csvLine);
    
                            // TODO: we should only export complete races where all competitors have valid totalPoints for a race
                            for (RaceColumn raceColumn : raceColumns) {
                                List<Competitor> rankedCompetitorsForColumn = rankedCompetitorsPerColumn.get(raceColumn);
                                if (rankedCompetitorsForColumn == null) {
                                    rankedCompetitorsForColumn = leaderboard.getCompetitorsFromBestToWorst(raceColumn,
                                            resultTimePoint);
                                    rankedCompetitorsPerColumn.put(raceColumn, rankedCompetitorsForColumn);
                                }
                                // Double netRacePoints = leaderboard.getNetPoints(competitor, raceColumn, timePoint);
                                Double totalRacePoints = leaderboard.getTotalPoints(competitor, raceColumn, resultTimePoint);
                                MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, raceColumn, resultTimePoint);
                                int rank = rankedCompetitorsForColumn.indexOf(competitor)+1;
                                if(totalRacePoints != null) {
                                    if (maxPointsReason == null || maxPointsReason == MaxPointsReason.NONE) {
                                        csvLine.add(rank);
                                        csvLine.add(totalRacePoints);
    
                                    } else {
                                        csvLine.add(maxPointsReason.name());
                                        csvLine.add(totalRacePoints);
                                    }
                                } else {
                                    /* Make sure to also include null columns */
                                    csvLine.add(0);
                                    csvLine.add(0);
                                }
                            }
                        }
                    }
                } else {
                    for (Competitor competitor : competitorsFromBestToWorst) {
                        if (!suppressedCompetitors.contains(competitor)) {
                            List<Object> csvLine = new ArrayList<Object>();
                            String competitorDisplayName = leaderboard.getDisplayName(competitor);
                            csvLine.add(competitorDisplayName != null ? competitorDisplayName : competitor.getName());
                            csv.add(csvLine);
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

    private ResultStates resolveRequestedResultState(String resultStateParam) {
        ResultStates result = DEFAULT_RESULT_STATE;
        if(resultStateParam != null) {
            for(ResultStates state: ResultStates.values()) {
                if(state.name().equalsIgnoreCase(resultStateParam)) {
                    result = state;
                    break;
                }
            }
        }
        return result;
    }

    private TimePoint calculateTimePointForResultState(Leaderboard leaderboard, ResultStates resultState) {
        TimePoint result = null;
        switch (resultState) {
        case Live:
            result = MillisecondsTimePoint.now();
            break;
        case Preliminary:
        case Final:
            if(leaderboard.getScoreCorrection() != null) {
                result = leaderboard.getScoreCorrection().getTimePointOfLastCorrectionsValidity();
            }
            break;
        }
        
        return result;
    }
}
