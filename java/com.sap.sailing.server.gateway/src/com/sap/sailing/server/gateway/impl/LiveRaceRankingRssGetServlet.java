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

public class LiveRaceRankingRssGetServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = -508373964426868319L;
    private static final String PARAM_NAME_LEADERBOARDNAME = "leaderboardName";
    private static final String PARAM_MAX_COMPETITORS = "max";
    private String charEncoding =  "UTF-8";
    private int maxCompetitorsToShow = 1000;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // to allow access to the rss feed directly from a client side javascript
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/rss+xml");
        resp.setCharacterEncoding(charEncoding);

        TimePoint timePoint = MillisecondsTimePoint.now();
        String leaderboardName = req.getParameter(PARAM_NAME_LEADERBOARDNAME);
        String maxCompetitorsParam  = req.getParameter(PARAM_MAX_COMPETITORS);
        if(maxCompetitorsParam != null) {
			try {
				maxCompetitorsToShow = Integer.parseInt(maxCompetitorsParam);
			} catch (NumberFormatException e) {
			}
        }
        
        if (leaderboardName == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Need to specify a leaderboard name using the "+
                    PARAM_NAME_LEADERBOARDNAME+" parameter");
        } else {
            Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            if (leaderboard == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Leaderboard "+leaderboardName+" not found");
            } else {
                try {
                    RaceColumn raceColumnToShow = null;
                    TrackedRace liveTrackedRace = null;

                    // find a live race in the leaderboard
                    Iterable<RaceColumn> races = leaderboard.getRaceColumns();
                    for (RaceColumn raceInLeaderboard : races) {
                        for (Fleet fleet : raceInLeaderboard.getFleets()) {
                            TrackedRace trackedRaceOfFleet = raceInLeaderboard.getTrackedRace(fleet);
                            if(trackedRaceOfFleet != null && isLive(trackedRaceOfFleet)) {
                                raceColumnToShow = raceInLeaderboard;
                                liveTrackedRace = trackedRaceOfFleet;
                                break;
                            }
                        }
                    }

                    SyndFeed feed = new SyndFeedImpl();
                    String title = leaderboard.getName();
                    feed.setTitle(title);
                    feed.setFeedType("rss_2.0");
                    feed.setLink("http://www.sapsaling.com");
                    feed.setDescription("SAP Sailing Analytics");
                    feed.setPublishedDate(new Date());
                    feed.setEncoding(charEncoding);
                    
                    List<SyndEntry> entries = new ArrayList<SyndEntry>();
                    SyndContent description = new SyndContentImpl();
                    SyndEntry entry = new SyndEntryImpl();
                    entry.setLink("http://www.sapsailing.com");
                    description.setType("text/plain");

                    if(raceColumnToShow != null && liveTrackedRace != null) {
                        String entryTitle = "Live ranking of race '" + raceColumnToShow.getName() + "'";
                        entry.setTitle(entryTitle);

                        
                        
                    	List<Competitor> competitorsFromBestToWorst = leaderboard.getCompetitorsFromBestToWorst(raceColumnToShow, timePoint);
                        int currentCompetitorCounter = 0; 
                        String feedText = "";
                        
                        for (Competitor competitor : competitorsFromBestToWorst) {
                        	if(currentCompetitorCounter == maxCompetitorsToShow) {
                        		break;
                        	}                        	
                        	currentCompetitorCounter++;
                            String sailID = competitor.getBoat().getSailID();
                            feedText += currentCompetitorCounter + ". " + sailID + "\n";
                        	
//                            String competitorName = competitor.getName();
//                            final String displayName = leaderboard.getDisplayName(competitor);
//                            String id = competitor.getId().toString();
//                            Nationality nationality = competitor.getTeam().getNationality();
//                            String nat = nationality != null ? nationality.getThreeLetterIOCAcronym(): null;
//                            String countryCode = nationality != null ? nationality.getCountryCode().getTwoLetterISOCode(): null;
//                            String totalRank = "" +competitorsFromBestToWorst.indexOf(competitor) + 1;
//                            String totalPoints = "" + leaderboard.getTotalPoints(competitor, timePoint);
//                            String raceColumnName = raceColumnToShow.getName();
//                            final Fleet fleetOfCompetitor = raceColumnToShow.getFleetOfCompetitor(competitor);
//                            String fleet = fleetOfCompetitor==null?"":fleetOfCompetitor.getName();
//                            String netpoints = String.valueOf(leaderboard.getNetPoints(competitor, raceColumnToShow, timePoint));
//                            String totalPointsRaceColumn = String.valueOf(leaderboard.getTotalPoints(competitor, raceColumnToShow, timePoint));
//                            MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, raceColumnToShow, timePoint);
//                            String maxPoints = maxPointsReason != null ? maxPointsReason.toString(): null;
//                            String isDiscarded = String.valueOf(leaderboard.isDiscarded(competitor, raceColumnToShow, timePoint));
//                            String isCorrected = String.valueOf(leaderboard.getScoreCorrection().isScoreCorrected(competitor, raceColumnToShow));

                        }
                        description.setValue(feedText);
                    } else {
                        String entryTitle = "No live race right now";
                        entry.setTitle(entryTitle);
                        description.setValue("");
                    }

                    entry.setDescription(description);
                    entries.add(entry);
                    feed.setEntries(entries);

                    PrintWriter writer = resp.getWriter();
                    SyndFeedOutput feedOutput = new SyndFeedOutput();
                    feedOutput.output(feed, writer);
                } catch (Exception e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            }
        }
    }

    /**
    * @return <code>true</code> if the startOfTracking is after the current date and there's no end of the race
     */
    private boolean isLive(TrackedRace trackedRace) {
        return trackedRace.getEndOfRace() == null && (trackedRace.getStartOfTracking() != null ? new Date().after(trackedRace.getStartOfTracking().asDate()) : false);
    }
    
}
