<%@ page import="java.util.*" %>
<%@ page import="com.sap.sailing.domain.leaderboard.Leaderboard" %>
<%@ page import="com.sap.sailing.domain.base.*" %>
<%@ page import="com.sap.sailing.domain.base.impl.*" %>
<%@ page import="com.sap.sailing.domain.common.*" %>
<%@ page import="com.sap.sailing.domain.tracking.*" %>
<%@ page pageEncoding="UTF-8" %>
<html>
<head>
</head>
<body>
<%
	TimePoint timePoint = MillisecondsTimePoint.now();
	Leaderboard leaderboard = (Leaderboard) request.getAttribute("leaderboard");

	// competitors are ordered according to total rank
	List<Competitor> competitorsFromBestToWorst = leaderboard.getCompetitorsFromBestToWorst(timePoint);
	Map<RaceColumn, List<Competitor>> rankedCompetitorsPerColumn = new HashMap<RaceColumn, List<Competitor>>();
	Iterable<RaceColumn> raceColumns = leaderboard.getRaceColumns();
%>
<h3>Leaderboard: <%= leaderboard.getName() %></h3>
<table cellpadding="2" cellspacing="2" border="1">
	<tr>
		<td>Rank</td>
		<td>Name</td>
		<td>Country</td>
		<% 	for (RaceColumn raceColumn : raceColumns) { %>
			<td><%= raceColumn.getName() %></td>
		<% } %>
	</tr>
	<% for (Competitor competitor : competitorsFromBestToWorst) {
	    int totalRank = competitorsFromBestToWorst.indexOf(competitor) + 1;
	    Nationality nationality = competitor.getTeam().getNationality();
	%>
    <tr>
		<td><%= totalRank %></td>
		<td><%= competitor.getName() %></td>
		<td><%= nationality.getCountryCode().getTwoLetterISOCode() %></td>
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
                //    jsonEntry.put("maxPointsReason", maxPointsReason != null ? maxPointsReason.toString(): null);
                int rank = rankedCompetitorsForColumn.indexOf(competitor)+1;
                TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
                if (trackedRace != null) {
                    int raceRank = trackedRace.getRank(competitor, timePoint);
                }
                boolean isDiscarded = leaderboard.isDiscarded(competitor, raceColumn, timePoint);
                boolean isCorrected = leaderboard.getScoreCorrection().isScoreCorrected(competitor, raceColumn);
                
                String points = totalPoints != null ? totalPoints.toString() : "&nbsp;"; 
		%>
			<td><%= points  %></td>
		<% } %>
    </tr>
	<% } %>
</table>
</body>
</html>
