package com.sap.sailing.hanaexport.jaxrs.api;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NavigableSet;
import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.hanaexport.HanaConnectionFactory;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.shared.server.gateway.jaxrs.SharedAbstractSailingServerResource;
import com.sap.sse.ServerInfo;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

@Path("/v1/exporter")
public class HanaCloudSacExportResource extends SharedAbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(HanaCloudSacExportResource.class.getName());
    
    @POST
    @Produces("application/json;charset=UTF-8")
    @Path("clear")
    public Response clear() throws SQLException, IOException {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.SERVER.getStringPermissionForTypeRelativeIdentifier(
                SecuredSecurityTypes.ServerActions.CAN_EXPORT_MASTERDATA,
                new TypeRelativeObjectIdentifier(ServerInfo.getName())));
        logger.info("Clearing HANA Cloud SAILING DB on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        final Connection connection = HanaConnectionFactory.INSTANCE.getConnection();
        tryExecutingQueriesFromSqlResource("/cleartables.sql", connection);
        logger.info("Done learing HANA Cloud SAILING DB on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        return Response.ok().build();
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    @Path("export")
    public Response export() throws SQLException {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.SERVER.getStringPermissionForTypeRelativeIdentifier(
                SecuredSecurityTypes.ServerActions.CAN_EXPORT_MASTERDATA,
                new TypeRelativeObjectIdentifier(ServerInfo.getName())));
        logger.info("Exporting HANA Cloud SAILING DB content on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        final RacingEventService racingEventService = getService();
        final Connection connection = HanaConnectionFactory.INSTANCE.getConnection();
        exportBoatClasses(racingEventService, connection);
        exportIrms(racingEventService, connection);
        exportScoringSchemes(racingEventService, connection);
        exportCompetitors(racingEventService, connection);
        exportEvents(racingEventService, connection);
        exportRaces(racingEventService, connection);
        logger.info("Done exporting HANA Cloud SAILING DB content on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        return Response.ok().build();
    }

    private void exportCompetitors(RacingEventService racingEventService, Connection connection) throws SQLException {
        final PreparedStatement insertCompetitors = connection.prepareStatement(
                "INSERT INTO \"Competitor\" (\"id\", \"name\", \"shortName\", \"nationality\", \"sailNumber\") VALUES (?, ?, ?, ?, ?);");
        for (final Competitor competitor : racingEventService.getCompetitorAndBoatStore().getAllCompetitors()) {
            insertCompetitors.setString(1, competitor.getId().toString());
            insertCompetitors.setString(2, competitor.getName());
            insertCompetitors.setString(3, competitor.getShortName());
            insertCompetitors.setString(4, competitor.getNationality() == null ? "   " : competitor.getNationality().getThreeLetterIOCAcronym());
            insertCompetitors.setString(5, competitor.hasBoat() ? ((CompetitorWithBoat) competitor).getBoat().getSailID() : null);
            insertCompetitors.execute();
        }
    }

    private void exportIrms(RacingEventService racingEventService, Connection connection) throws SQLException {
        final PreparedStatement insertBoatClasses = connection.prepareStatement(
                "INSERT INTO \"IRM\" (\"name\", \"discardable\", \"advanceCompetitorsTrackedWorse\", \"appliesAtStartOfRace\") VALUES (?, ?, ?, ?);");
        for (final MaxPointsReason irm : MaxPointsReason.values()) {
            insertBoatClasses.setString(1, irm.name());
            insertBoatClasses.setBoolean(2, irm.isDiscardable());
            insertBoatClasses.setBoolean(3, irm.isAdvanceCompetitorsTrackedWorse());
            insertBoatClasses.setBoolean(4, irm.isAppliesAtStartOfRace());
            insertBoatClasses.execute();
        }
    }

    private void exportScoringSchemes(RacingEventService racingEventService, Connection connection) throws SQLException {
        final PreparedStatement insertBoatClasses = connection.prepareStatement(
                "INSERT INTO \"ScoringScheme\" (\"id\", \"higherIsBetter\") VALUES (?, ?);");
        for (final ScoringSchemeType scoringSchemeType : ScoringSchemeType.values()) {
            final ScoringScheme scoringScheme = racingEventService.getBaseDomainFactory().createScoringScheme(scoringSchemeType);
            insertBoatClasses.setString(1, scoringScheme.getType().name());
            insertBoatClasses.setBoolean(2, scoringScheme.isHigherBetter());
            insertBoatClasses.execute();
        }
    }

    private void exportBoatClasses(final RacingEventService racingEventService, final Connection connection)
            throws SQLException {
        final PreparedStatement insertBoatClasses = connection.prepareStatement(
                "INSERT INTO \"BoatClass\" (\"id\", \"description\", \"hullLengthInMeters\", \"hullBeamInMeters\", \"hullType\") VALUES (?, ?, ?, ?, ?);");
        for (final BoatClass boatClass : racingEventService.getBaseDomainFactory().getBoatClasses()) {
            insertBoatClasses.setString(1, boatClass.getName().substring(0, Math.min(boatClass.getName().length(), 20)));
            insertBoatClasses.setString(2, "Type "+boatClass.getHullType().name()+", length "+
                    boatClass.getHullLength().getMeters()+"m, beam "+boatClass.getHullBeam().getMeters()+"m");
            insertBoatClasses.setDouble(3, boatClass.getHullLength().getMeters());
            insertBoatClasses.setDouble(4, boatClass.getHullBeam().getMeters());
            insertBoatClasses.setString(5, boatClass.getHullType().name());
            insertBoatClasses.execute();
        }
    }

    private void exportEvents(RacingEventService racingEventService, Connection connection) throws SQLException {
        final PreparedStatement insertEvents = connection.prepareStatement(
                "INSERT INTO \"Event\" (\"id\", \"name\", \"startDate\", \"endDate\", \"venue\", \"isListed\", \"description\") VALUES (?, ?, ?, ?, ?, ?, ?);");
        for (final Event event : racingEventService.getAllEvents()) {
            insertEvents.setString(1, event.getId().toString());
            insertEvents.setString(2, event.getName());
            insertEvents.setDate(3, new Date(event.getStartDate().asMillis()));
            insertEvents.setDate(4, new Date(event.getEndDate().asMillis()));
            insertEvents.setString(5, event.getVenue().getName());
            insertEvents.setBoolean(6, event.isPublic());
            insertEvents.setString(7, event.getDescription());
            insertEvents.execute();
        }
    }

    private void exportRaces(RacingEventService racingEventService, Connection connection) throws SQLException {
        final TimePoint now = TimePoint.now();
        final PreparedStatement insertRegattas = connection.prepareStatement(
                "INSERT INTO \"Regatta\" (\"name\", \"boatClass\", \"scoringScheme\", \"rankingMetric\") "+
                                       "VALUES (?, ?, ?, ?);");
        final PreparedStatement insertRaces = connection.prepareStatement(
                "INSERT INTO \"Race\" (\"name\", \"regatta\", \"raceColumn\", \"fleet\", \"startOfTracking\", "+
                                       "\"startOfRace\", \"endOfTracking\", \"endOfRace\", \"avgWindSpeedInKnots\") "+
                                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
        final PreparedStatement insertRaceResults = connection.prepareStatement(
                "INSERT INTO \"RaceResult\" (\"regatta\", \"raceColumn\", \"competitorId\", \"points\", "+
                                       "\"discarded\", \"irm\") "+
                                       "VALUES (?, ?, ?, ?, ?, ?);");
        final PreparedStatement insertRaceStats = connection.prepareStatement(
                "INSERT INTO \"RaceStats\" (\"race\", \"regatta\", \"competitorId\", \"rankOneBased\", \"distanceSailedInMeters\", \"elapsedTimeInSeconds\", "+
                                       "\"avgCrossTrackErrorInMeters\", \"absoluteAvGCrossTrackErrorInMeters\", \"numberOfTacks\", "+
                                       "\"numberOfGybes\", \"numberOfPenaltyCircles\", \"startDelayInSeconds\", \"distanceFromStartLineInMetersAtStart\") "+
                                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
        for (final Regatta regatta : racingEventService.getAllRegattas()) {
            insertRegattas.setString(1, regatta.getName());
            insertRegattas.setString(2, regatta.getBoatClass().getName());
            insertRegattas.setString(3, regatta.getScoringScheme().getType().name());
            insertRegattas.setString(4, regatta.getRankingMetricType().name());
            insertRegattas.execute();
            final Leaderboard leaderboard = racingEventService.getLeaderboardByName(regatta.getName());
            if (leaderboard != null) {
                for (final RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    for (final Competitor competitor : raceColumn.getAllCompetitors()) {
                        parameterizeInsertRaceResult(insertRaceResults, now, competitor, leaderboard, raceColumn, regatta);
                        insertRaceResults.execute();
                    }
                    for (final Fleet fleet : raceColumn.getFleets()) {
                        final TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                        if (trackedRace != null) {
                            parameterizeInsertRacesStatement(insertRaces, regatta, raceColumn, fleet, trackedRace);
                            insertRaces.execute();
                        }
                        final Waypoint startWaypoint = trackedRace == null ? null : trackedRace.getRace().getCourse().getFirstWaypoint();
                        if (trackedRace != null) {
                            for (final Competitor competitor : trackedRace.getRace().getCompetitors()) {
                                parameterizeInsertRaceStats(insertRaceStats, now, startWaypoint, competitor, trackedRace);
                                insertRaceStats.execute();
                            }
                        }
                    }
                }
            }
        }
    }

    private void parameterizeInsertRaceStats(PreparedStatement insertRaceStats, TimePoint now, Waypoint startWaypoint,
            Competitor competitor, TrackedRace trackedRace) throws SQLException {
        insertRaceStats.setString(1, trackedRace.getRace().getName());
        insertRaceStats.setString(2, trackedRace.getTrackedRegatta().getRegatta().getName());
        insertRaceStats.setString(3, competitor.getId().toString());
        insertRaceStats.setInt(4, trackedRace.getRank(competitor, now));
        insertRaceStats.setDouble(5, metersOr0ForNull(trackedRace.getDistanceTraveled(competitor, now)));
        insertRaceStats.setDouble(6, secondsOr0ForNull(trackedRace.getTimeSailedSinceRaceStart(competitor, now)));
        insertRaceStats.setDouble(7, metersOr0ForNull(trackedRace.getAverageSignedCrossTrackError(competitor, now, /* waitForLatest */ false)));
        insertRaceStats.setDouble(8, metersOr0ForNull(trackedRace.getAverageAbsoluteCrossTrackError(competitor, now, /* waitForLatest */ false)));
        final Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, /* waitForLatest */ false);
        insertRaceStats.setInt(9, Util.size(Util.filter(maneuvers, m->m.getType() == ManeuverType.TACK)));
        insertRaceStats.setInt(10, Util.size(Util.filter(maneuvers, m->m.getType() == ManeuverType.JIBE)));
        insertRaceStats.setInt(11, Util.size(Util.filter(maneuvers, m->m.getType() == ManeuverType.PENALTY_CIRCLE)));
        final TimePoint startOfRace = trackedRace.getStartOfRace();
        final double startDelay;
        if (startWaypoint != null && startOfRace != null) {
            NavigableSet<MarkPassing> competitorMarkPassings = trackedRace.getMarkPassings(competitor);
            trackedRace.lockForRead(competitorMarkPassings);
            try {
                if (!Util.isEmpty(competitorMarkPassings)) {
                    final MarkPassing competitorStartMarkPassing = competitorMarkPassings.iterator().next();
                    startDelay = secondsOr0ForNull(startOfRace.until(competitorStartMarkPassing.getTimePoint()));
                } else {
                    startDelay = 0;
                }
            } finally {
                trackedRace.unlockAfterRead(competitorMarkPassings);
            }
        } else {
            startDelay = 0;
        }
        insertRaceStats.setDouble(12, startDelay);
        if (startOfRace != null) {
            insertRaceStats.setDouble(13, metersOr0ForNull(trackedRace.getDistanceToStartLine(competitor, startOfRace)));
        } else {
            insertRaceStats.setDouble(13, 0);
        }
    }

    private double metersOr0ForNull(final Distance distance) {
        return distance == null ? 0 : distance.getMeters();
    }

    private double secondsOr0ForNull(final Duration duration) {
        return duration == null ? 0 : duration.asSeconds();
    }

    private void parameterizeInsertRaceResult(PreparedStatement insertRaceResults, TimePoint now,
            Competitor competitor, Leaderboard leaderboard, RaceColumn raceColumn, Regatta regatta) throws SQLException {
        insertRaceResults.setString(1, regatta.getName());
        insertRaceResults.setString(2, raceColumn.getName());
        insertRaceResults.setString(3, competitor.getId().toString());
        final Double totalPoints = leaderboard.getTotalPoints(competitor, raceColumn, now);
        insertRaceResults.setDouble(4, totalPoints == null ? 0 : totalPoints);
        insertRaceResults.setBoolean(5, leaderboard.isDiscarded(competitor, raceColumn, now));
        final MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, raceColumn, now);
        insertRaceResults.setString(6, (maxPointsReason == null ? MaxPointsReason.NONE : maxPointsReason).name());
    }

    private void parameterizeInsertRacesStatement(final PreparedStatement insertRaces, final Regatta regatta,
            final RaceColumn raceColumn, final Fleet fleet, final TrackedRace trackedRace) throws SQLException {
        insertRaces.setString(1, trackedRace.getRace().getName());
        assert trackedRace.getTrackedRegatta().getRegatta() == regatta;
        insertRaces.setString(2, trackedRace.getTrackedRegatta().getRegatta().getName());
        insertRaces.setString(3, raceColumn.getName());
        insertRaces.setString(4, fleet.getName());
        if (trackedRace.getStartOfTracking() != null) {
            insertRaces.setDate(5, new Date(trackedRace.getStartOfTracking().asMillis()));
        } else {
            insertRaces.setDate(5, null);
        }
        if (trackedRace.getStartOfRace() != null) {
            insertRaces.setDate(6, new Date(trackedRace.getStartOfRace().asMillis()));
        } else {
            insertRaces.setDate(6, null);
        }
        if (trackedRace.getEndOfTracking() != null) {
            insertRaces.setDate(7, new Date(trackedRace.getEndOfTracking().asMillis()));
        } else {
            insertRaces.setDate(7, null);
        }
        if (trackedRace.getEndOfRace() != null) {
            insertRaces.setDate(8, new Date(trackedRace.getEndOfRace().asMillis()));
        } else {
            insertRaces.setDate(8, null);
        }
        final SpeedWithConfidence<TimePoint> averageWind = trackedRace.getAverageWindSpeedWithConfidenceWithNumberOfSamples(/* number of samples */ 5);
        if (averageWind != null) {
            insertRaces.setDouble(9, averageWind.getObject().getKnots());
        } else {
            insertRaces.setDouble(9, 0.0);
        }
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    @Path("createtables")
    public Response createTables(@QueryParam("drop") boolean drop) throws SQLException, IOException {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.SERVER.getStringPermissionForTypeRelativeIdentifier(
                SecuredSecurityTypes.ServerActions.CAN_EXPORT_MASTERDATA,
                new TypeRelativeObjectIdentifier(ServerInfo.getName())));
        final Connection connection = HanaConnectionFactory.INSTANCE.getConnection();
        if (drop) {
            logger.info("Dropping HANA Cloud SAILING DB tables on behalf of user "+SecurityUtils.getSubject().getPrincipal());
            tryExecutingQueriesFromSqlResource("/droptables.sql", connection);
            logger.info("Done dropping HANA Cloud SAILING DB tables on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        }
        logger.info("Creating HANA Cloud SAILING DB tables on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        executeQueriesFromSqlResource("/createtables.sql", connection);
        logger.info("Done creating HANA Cloud SAILING DB tables on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        return Response.ok().build();
    }

    private void tryExecutingQueriesFromSqlResource(String resourceWithSemicolonSeparatedStatements, Connection connection) throws IOException {
        for (final String statementAsString : getStatementsFromResource(resourceWithSemicolonSeparatedStatements)) {
            try {
                logger.fine("...executing "+statementAsString);
                connection.createStatement().execute(statementAsString);
            } catch (Exception e) {
                logger.info("Problem trying to execute "+statementAsString+"; continuing...");
            }
        }
    }

    private String[] getStatementsFromResource(String resourceWithSemicolonSeparatedStatements) throws IOException {
        final StringWriter sw = new StringWriter();
        IOUtils.copy(getClass().getResourceAsStream(resourceWithSemicolonSeparatedStatements), sw);
        return sw.toString().split(";");
    }
        
    /**
     * Executes statements one by one; if an exception occurs it is immediately forwarded to the caller and neither
     * caught nor logged here.
     */
    private void executeQueriesFromSqlResource(String resourceWithSemicolonSeparatedStatements, final Connection connection) throws IOException, SQLException {
        for (final String statementAsString : getStatementsFromResource(resourceWithSemicolonSeparatedStatements)) {
            connection.createStatement().execute(statementAsString);
        }
    }
}
