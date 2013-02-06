<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.sap.sailing.domain.leaderboard.*" %>
<%@ page import="com.sap.sailing.domain.leaderboard.meta.*" %>
<%@ page import="com.sap.sailing.domain.base.*" %>
<%@ page import="com.sap.sailing.domain.base.impl.*" %>
<%@ page import="com.sap.sailing.domain.common.*" %>
<%@ page import="com.sap.sailing.domain.tracking.*" %>
<%@ page pageEncoding="UTF-8" %>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
    <title>SAP Sailing Analytics Leaderboard</title>
	<link rel="stylesheet" href="<%= request.getContextPath() %>/css/leaderboard.css" type="text/css"></link>
	<link rel="stylesheet" href="<%= request.getContextPath() %>/css/tablesorter.css" type="text/css"></link>
	<script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery-latest.js"></script> 
	<script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery.tablesorter.js"></script>
	<script type="text/javascript">
	$(document).ready(function() {
	    $("#leaderboardTable").tablesorter();
	});
	</script>
</head>
<body>

<%
	TimePoint timePoint = MillisecondsTimePoint.now();
	LeaderboardGroupMetaLeaderboard leaderboard = (LeaderboardGroupMetaLeaderboard) request.getAttribute("leaderboard");
	Boolean showDetails = (Boolean) request.getAttribute("showDetails");

	// competitors are ordered according to total rank
	List<Competitor> competitorsFromBestToWorst = leaderboard.getCompetitorsFromBestToWorst(timePoint);
	Map<RaceColumn, List<Competitor>> rankedCompetitorsPerColumn = new HashMap<RaceColumn, List<Competitor>>();
	Iterable<RaceColumn> raceColumns = leaderboard.getRaceColumns();
	List<Competitor> suppressedCompetitors = new ArrayList<Competitor>();
	for(Competitor c: leaderboard.getSuppressedCompetitors()) {
	    suppressedCompetitors.add(c);
	}
	
	NumberFormat scoreFormat = NumberFormat.getNumberInstance();
	int digitPresicion = 1;
	scoreFormat.setMinimumFractionDigits(digitPresicion);
	scoreFormat.setMaximumFractionDigits(digitPresicion);

	String imgFlagPath = "/gwt/images/flags/";
%>
<h3>Overall Leaderboard: <%= leaderboard.getName() %></h3>
<table id="leaderboardTable" class="tablesorter">
	<thead>
	<tr>
		<th>Rank</th>
		<th>Team</th>
		<% 	for (RaceColumn raceColumn : raceColumns) { %>
			<th><% 
					String raceColumnName = raceColumn.getName();
					String prefixToCut = "Extreme 40 ";
					if(raceColumnName.startsWith(prefixToCut)) {
					    raceColumnName = raceColumnName.substring(prefixToCut.length(), raceColumnName.length());
					}
				%><%= raceColumnName %></th>
		<% } %>
		<th>Points</th>
	</tr>
	</thead>
	<tbody>
	<% 
		for (Competitor competitor : competitorsFromBestToWorst) {
		    int totalRank = competitorsFromBestToWorst.indexOf(competitor) + 1;
		    Nationality nationality = competitor.getTeam().getNationality();
			Double totalPoints = leaderboard.getTotalPoints(competitor, timePoint);
			if(leaderboard.hasCarriedPoints(competitor)) {
			    Double carriedPoints = leaderboard.getCarriedPoints(competitor);
			    if(carriedPoints != null) {
			        totalPoints = totalPoints != null ? totalPoints += carriedPoints : carriedPoints;  
			    }
			}
			if(!suppressedCompetitors.contains(competitor)) {
	%>
    <tr>
		<td><%= totalRank %></td>
		<td style="white-space:nowrap;"><img src="<%= imgFlagPath + nationality.getCountryCode().getTwoLetterISOCode().toLowerCase() + ".png"%>" />&nbsp;&nbsp;<%= competitor.getName() %></td>
		<% 	for (RaceColumn raceColumn : raceColumns) { 
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

                String racePointsStyleClasses = "";

                String racePoints = "&nbsp;";
                if (maxPointsReason == null || maxPointsReason == MaxPointsReason.NONE) {
                    if (!isDiscarded) {
                        racePointsStyleClasses = "leaderboard-points-TotalPoints-NotDiscarded";
						if(totalRacePoints != null) {
						    racePoints = scoreFormat.format(totalRacePoints);
						}
                    } else {
                        racePointsStyleClasses = "leaderboard-points-TotalPoints-Discarded";
						if(netRacePoints != null) {
						    racePoints = scoreFormat.format(netRacePoints);
						}
                    }
                } else {
                    if (!isDiscarded) {
                    	racePointsStyleClasses = "leaderboard-points-MaxPoints-NotDiscarded";
                    } else {
                    	racePointsStyleClasses = "leaderboard-points-MaxPoints-Discarded";
                    }
					racePoints = maxPointsReason.name();
                }
		%>
			<td class="<%= racePointsStyleClasses %>"><%= racePoints  %></td>
		<% } %>
		<td><%= totalPoints %></td>
    </tr>
	<% 	} } %>
	</tbody>
</table>
</body>
</html>
