<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="com.sap.sailing.domain.leaderboard.Leaderboard" %>
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
	Leaderboard leaderboard = (Leaderboard) request.getAttribute("leaderboard");

	// competitors are ordered according to total rank
	List<Competitor> competitorsFromBestToWorst = leaderboard.getCompetitorsFromBestToWorst(timePoint);
	Map<RaceColumn, List<Competitor>> rankedCompetitorsPerColumn = new HashMap<RaceColumn, List<Competitor>>();
	Iterable<RaceColumn> raceColumns = leaderboard.getRaceColumns();
	
	String imgFlagPath = "/gwt/images/flags/";
%>
<h3>Leaderboard: <%= leaderboard.getName() %></h3>
<table id="leaderboardTable" class="tablesorter">
	<thead>
	<tr>
		<th width="50">Rank</th>
		<th>Name</th>
		<% 	for (RaceColumn raceColumn : raceColumns) { %>
			<th><%= raceColumn.getName() %></th>
		<% } %>
	</tr>
	</thead>
	<tbody>
	<% for (Competitor competitor : competitorsFromBestToWorst) {
	    int totalRank = competitorsFromBestToWorst.indexOf(competitor) + 1;
	    Nationality nationality = competitor.getTeam().getNationality();
	%>
    <tr>
		<td><%= totalRank %></td>
		<td><img src="<%= imgFlagPath + nationality.getCountryCode().getTwoLetterISOCode().toLowerCase() + ".png"%>" />&nbsp;<%= competitor.getName() %></td>
		<% 	for (RaceColumn raceColumn : raceColumns) { 
                List<Competitor> rankedCompetitorsForColumn = rankedCompetitorsPerColumn.get(raceColumn);
                if (rankedCompetitorsForColumn == null) {
                    rankedCompetitorsForColumn = leaderboard.getCompetitorsFromBestToWorst(raceColumn, timePoint);
                    rankedCompetitorsPerColumn.put(raceColumn, rankedCompetitorsForColumn);
                }
				Fleet fleetOfCompetitor = raceColumn.getFleetOfCompetitor(competitor);
                Double netPoints = leaderboard.getNetPoints(competitor, raceColumn, timePoint);
                Double totalPoints = leaderboard.getTotalPoints(competitor, raceColumn, timePoint);
				MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, raceColumn, timePoint);
                int rank = rankedCompetitorsForColumn.indexOf(competitor)+1;
                TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
                if (trackedRace != null) {
                    int raceRank = trackedRace.getRank(competitor, timePoint);
                }
                boolean isDiscarded = leaderboard.isDiscarded(competitor, raceColumn, timePoint);
                boolean isCorrected = leaderboard.getScoreCorrection().isScoreCorrected(competitor, raceColumn);

                NumberFormat scoreFormat = NumberFormat.getNumberInstance();
                int digitPresicion = 1;
                scoreFormat.setMinimumFractionDigits(digitPresicion);
                scoreFormat.setMaximumFractionDigits(digitPresicion);

                String IS_LIVE_TEXT_COLOR = "#1876B3";
                String DEFAULT_TEXT_COLOR = "#000000";

                String racePointsStyleClasses = "";
                
            	boolean isLive = false; //isLive(entry.fleet);
            	
                String textColor = isLive ? IS_LIVE_TEXT_COLOR : DEFAULT_TEXT_COLOR;

                // if (fleet != null && fleet.getColor() != null) {
                //	 html.append(raceColumnTemplate.cellFrameWithTextColorAndFleetBorder(textColor, fleet.getColor().getAsHtml()));
                // } else {
                // 	html.append(raceColumnTemplate.cellFrameWithTextColor(textColor));
                //}

                // don't show points if max points / penalty
                String racePoints = "&nbsp;";
                if (maxPointsReason == null || maxPointsReason == MaxPointsReason.NONE) {
                    if (!isDiscarded) {
                        racePointsStyleClasses = "leaderboard-points-TotalPoints-NotDiscarded";
						if(totalPoints != null) {
						    racePoints = scoreFormat.format(totalPoints);
						}
                    } else {
                        racePointsStyleClasses = "leaderboard-points-TotalPoints-Discarded";
						if(netPoints != null) {
						    racePoints = scoreFormat.format(netPoints);
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
    </tr>
	<% } %>
	</tbody>
</table>
</body>
</html>
