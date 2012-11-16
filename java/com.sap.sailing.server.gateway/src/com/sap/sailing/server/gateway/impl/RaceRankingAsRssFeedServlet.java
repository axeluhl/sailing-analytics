package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

public class RaceRankingAsRssFeedServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = -508373964426868319L;
    private static final String PARAM_NAME_LEADERBOARDNAME = "leaderboardName";
    private static final String PARAM_NAME_RACENAME = "raceName";
    private String charEncoding =  "UTF-8";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // to allow access to the json document directly from a client side javascript
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/rss+xml");
        resp.setCharacterEncoding(charEncoding);

        TimePoint timePoint = MillisecondsTimePoint.now();
        String leaderboardName = req.getParameter(PARAM_NAME_LEADERBOARDNAME);
        String raceName = req.getParameter(PARAM_NAME_RACENAME);
        if (leaderboardName == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Need to specify a leaderboard name using the "+
                    PARAM_NAME_LEADERBOARDNAME+" parameter");
        } else if (raceName == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Need to specify a race column name using the "+
                        PARAM_NAME_RACENAME+" parameter");
        } else {
            Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            if (leaderboard == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Leaderboard "+leaderboardName+" not found");
            } else {
                try {
                    RaceColumn raceColumnToShow = null; 
                    
                    Iterable<RaceColumn> races = leaderboard.getRaceColumns();
                    for (RaceColumn raceInLeaderboard : races) {
                        for (Fleet fleet : raceInLeaderboard.getFleets()) {
                            TrackedRace trackedRace = raceInLeaderboard.getTrackedRace(fleet);
                            if (trackedRace != null && raceInLeaderboard.getName().equals(raceName)) {
                                raceColumnToShow = raceInLeaderboard; 
                                    break;
                            }
                        }
                    }

                    if(raceColumnToShow != null) {
                        List<Competitor> competitorsFromBestToWorst = leaderboard.getCompetitorsFromBestToWorst(raceColumnToShow, MillisecondsTimePoint.now());
                        String eventName = "STG meets 'Berlin Match Race'";
                        String title = eventName + ": " + leaderboardName;
                        
                        SyndFeed feed = new SyndFeedImpl();
                        feed.setTitle(title);
                        feed.setFeedType("rss_2.0");
                        feed.setLink("http://www.sapsaling.com");
                        feed.setDescription("SAP Sailing Analytics");
                        feed.setPublishedDate(new Date());
                        feed.setEncoding(charEncoding);
                        
                        String feedText = "";
                        
                        for (Competitor competitor : competitorsFromBestToWorst) {
                            String sailID = competitor.getBoat().getSailID();
//                            String competitorName = competitor.getName();
//                            final String displayName = leaderboard.getDisplayName(competitor);
//                            String id = competitor.getId().toString();
//                            Nationality nationality = competitor.getTeam().getNationality();
//                            String nat = nationality != null ? nationality.getThreeLetterIOCAcronym(): null;
//                            String countryCode = nationality != null ? nationality.getCountryCode().getTwoLetterISOCode(): null;
//                            String totalRank = "" +competitorsFromBestToWorst.indexOf(competitor) + 1;
//                            String totalPoints = "" + leaderboard.getTotalPoints(competitor, timePoint);
//
//                            String raceColumnName = raceColumnToShow.getName();
//                            final Fleet fleetOfCompetitor = raceColumnToShow.getFleetOfCompetitor(competitor);
//                            String fleet = fleetOfCompetitor==null?"":fleetOfCompetitor.getName();
//                            String netpoints = String.valueOf(leaderboard.getNetPoints(competitor, raceColumnToShow, timePoint));
//                            String totalPointsRaceColumn = String.valueOf(leaderboard.getTotalPoints(competitor, raceColumnToShow, timePoint));
//                            MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, raceColumnToShow, timePoint);
//                            String maxPoints = maxPointsReason != null ? maxPointsReason.toString(): null;
//                            String isDiscarded = String.valueOf(leaderboard.isDiscarded(competitor, raceColumnToShow, timePoint));
//                            String isCorrected = String.valueOf(leaderboard.getScoreCorrection().isScoreCorrected(competitor, raceColumnToShow));

                            final TrackedRace trackedRace = raceColumnToShow.getTrackedRace(competitor);
                            
                            boolean isLive = trackedRace != null && trackedRace.getEndOfRace() == null && (trackedRace.getStartOfTracking() != null ? new Date().after(trackedRace.getStartOfTracking().asDate()) : false);
                            if(isLive) {
                                String raceRank = "" + trackedRace.getRank(competitor, timePoint);
                                feedText += raceRank + ". " + sailID + "\n";
                            }
                        }

                        List<SyndEntry> entries = new ArrayList<SyndEntry>();
                        SyndContent description = new SyndContentImpl();
                        
                        SyndEntry entry = new SyndEntryImpl();
                        entry.setTitle("Live Race ranking of race " + raceName);
                        entry.setLink("http://obmr2012.sapsailing.com");
                        description.setType("text/plain");
                        description.setValue(feedText);
                        entry.setDescription(description);
                        entries.add(entry);
                        feed.setEntries(entries);
                        
                        PrintWriter writer = resp.getWriter();
                        SyndFeedOutput feedOutput = new SyndFeedOutput();
                        feedOutput.output(feed, writer);
                    } else {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No live race at the moment");
                    }
                } catch (Exception e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            }
        }
    }
}
