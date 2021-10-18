package com.sap.sailing.hanaexport.jaxrs.api;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.ranking.RankingMetric.RankingInfo;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sailing.hanaexport.HanaConnectionFactory;
import com.sap.sailing.hanaexport.jaxrs.api.InsertRegattaStatement.RegattaAndEvent;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.shared.server.gateway.jaxrs.SharedAbstractSailingServerResource;
import com.sap.sse.ServerInfo;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

@Path("/v1/exporter")
public class HanaCloudSacExportResource extends SharedAbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(HanaCloudSacExportResource.class.getName());
    
    private class AlreadyInsertedContent {
        private final Set<Event> events;
        private final Set<Competitor> competitors;
        private final InsertCompetitorStatement insertCompetitors;
        private final InsertEventStatement insertEvents;
        public AlreadyInsertedContent(InsertEventStatement insertEvents, InsertCompetitorStatement insertCompetitors) {
            events = new HashSet<>();
            competitors = new HashSet<>();
            this.insertEvents = insertEvents;
            this.insertCompetitors = insertCompetitors;
        }
        
        public void ensureEventIsInserted(Event event) throws SQLException {
            if (!events.contains(event)) {
                events.add(event);
                insertEvents.insert(event);
            }
        }

        public void ensureCompetitorIsInserted(Competitor competitor) throws SQLException {
            if (!competitors.contains(competitor)) {
                competitors.add(competitor);
                insertCompetitors.insert(competitor);
            }
        }
}
    
    @POST
    @Produces("application/json;charset=UTF-8")
    @Path("clear")
    public Response clear(
            @FormParam("dbendpoint") String dbEndpoint,
            @FormParam("dbuser") String dbUser,
            @FormParam("dbpassword") String dbPassword) throws SQLException, IOException {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.SERVER.getStringPermissionForTypeRelativeIdentifier(
                SecuredSecurityTypes.ServerActions.CAN_EXPORT_MASTERDATA,
                new TypeRelativeObjectIdentifier(ServerInfo.getName())));
        logger.info("Clearing HANA Cloud SAILING DB on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        final Connection connection = HanaConnectionFactory.INSTANCE.getConnection(Optional.ofNullable(dbEndpoint),
                Optional.ofNullable(dbUser), Optional.ofNullable(dbPassword));
        tryExecutingQueriesFromSqlResource("/cleartables.sql", connection);
        logger.info("Done clearing HANA Cloud SAILING DB on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        return Response.ok().build();
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    @Path("export")
    public Response export(@QueryParam("leaderboardgroupid") List<String> leaderboardGroupIds,
            @QueryParam("eventid") List<String> eventIds,
            @FormParam("dbendpoint") String dbEndpoint,
            @FormParam("dbuser") String dbUser,
            @FormParam("dbpassword") String dbPassword) throws SQLException {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.SERVER.getStringPermissionForTypeRelativeIdentifier(
                SecuredSecurityTypes.ServerActions.CAN_EXPORT_MASTERDATA,
                new TypeRelativeObjectIdentifier(ServerInfo.getName())));
        logger.info("Exporting HANA Cloud SAILING DB content on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        final RacingEventService racingEventService = getService();
        try {
            final Connection connection = HanaConnectionFactory.INSTANCE.getConnection(Optional.ofNullable(dbEndpoint),
                    Optional.ofNullable(dbUser), Optional.ofNullable(dbPassword));
            exportBoatClasses(racingEventService, connection);
            exportIrms(racingEventService, connection);
            exportScoringSchemes(racingEventService, connection);
            final Iterable<Regatta> regattasToExport = getRegattasToExport(racingEventService, leaderboardGroupIds, eventIds);
            exportRaces(racingEventService, regattasToExport, connection);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Problem exporting data to HANA", e);
            throw e;
        }
        logger.info("Done exporting HANA Cloud SAILING DB content on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        return Response.ok().build();
    }

    private Iterable<Regatta> getRegattasToExport(RacingEventService racingEventService, List<String> leaderboardGroupIds, List<String> eventIds) {
        final Set<LeaderboardGroup> leaderboardGroups = new HashSet<>();
        final Iterable<Regatta> result;
        boolean atLeastOneIdProvided = false;
        if (leaderboardGroupIds != null) {
            for (final String leaderboardGroupIdAsString : leaderboardGroupIds) {
                atLeastOneIdProvided = true;
                try {
                    leaderboardGroups.add(racingEventService.getLeaderboardGroupByID(UUID.fromString(leaderboardGroupIdAsString)));
                } catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING, "Couldn't parse "+leaderboardGroupIdAsString+" as a leaderboard group's UUID. Ignoring.");
                }
            }
        }
        if (eventIds != null) {
            for (final String eventIdAsString : eventIds) {
                atLeastOneIdProvided = true;
                try {
                    final Event event = racingEventService.getEvent(UUID.fromString(eventIdAsString));
                    Util.addAll(event.getLeaderboardGroups(), leaderboardGroups);
                } catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING, "Couldn't parse "+eventIdAsString+" as an event's UUID. Ignoring.");
                }
            }
        }
        if (atLeastOneIdProvided) {
            final Set<Regatta> resultSet = new HashSet<>();
            for (final LeaderboardGroup leaderboardGroup : leaderboardGroups) {
                for (final Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                    if (leaderboard.getLeaderboardType().isRegattaLeaderboard()) {
                        resultSet.add(((RegattaLeaderboard) leaderboard).getRegatta());
                    }
                }
            }
            result = resultSet;
        } else {
            result = racingEventService.getAllRegattas();
        }
        return result;
    }

    private void exportIrms(RacingEventService racingEventService, Connection connection) throws SQLException {
        final InsertIrmStatement insertIrms = new InsertIrmStatement(connection);
        insertIrms.insertBatch(Arrays.asList(MaxPointsReason.values()));
        insertIrms.executeBatch();
    }

    private void exportScoringSchemes(RacingEventService racingEventService, Connection connection) throws SQLException {
        final InsertScoringSchemeStatement insertScoringScheme = new InsertScoringSchemeStatement(connection);
        for (final ScoringSchemeType scoringSchemeType : ScoringSchemeType.values()) {
            final ScoringScheme scoringScheme = racingEventService.getBaseDomainFactory().createScoringScheme(scoringSchemeType);
            insertScoringScheme.insertBatch(scoringScheme);
        }
        insertScoringScheme.executeBatch();
    }

    private void exportBoatClasses(final RacingEventService racingEventService, final Connection connection)
            throws SQLException {
        final InsertBoatClassStatement insertBoatClasses = new InsertBoatClassStatement(connection);
        insertBoatClasses.insertBatch(racingEventService.getBaseDomainFactory().getBoatClasses());
        insertBoatClasses.executeBatch();
    }

    private void exportRaces(RacingEventService racingEventService, Iterable<Regatta> regattasToExport, Connection connection) throws SQLException {
        final TimePoint now = TimePoint.now();
        final InsertEventStatement insertEvents = new InsertEventStatement(connection);
        final InsertCompetitorStatement insertCompetitors = new InsertCompetitorStatement(connection);
        final AlreadyInsertedContent alreadyInsertedContent = new AlreadyInsertedContent(insertEvents, insertCompetitors);
        final InsertRegattaStatement insertRegattas = new InsertRegattaStatement(connection);
        final InsertRaceStatement insertRaces = new InsertRaceStatement(connection);
        final InsertRaceResultStatement insertRaceResults = new InsertRaceResultStatement(connection);
        final InsertRaceStatsStatement insertRaceStats = new InsertRaceStatsStatement(connection);
        final InsertLegStatement insertLegs = new InsertLegStatement(connection);
        final InsertLegStatsStatement insertLegStats = new InsertLegStatsStatement(connection);
        final InsertManeuverStatement insertManeuvers = new InsertManeuverStatement(connection);
        for (final Regatta regatta : regattasToExport) {
            final Leaderboard leaderboard = racingEventService.getLeaderboardByName(regatta.getName());
            if (leaderboard != null) {
                final Event event = racingEventService.findEventContainingLeaderboardAndMatchingAtLeastOneCourseArea(leaderboard);
                if (event != null) {
                    alreadyInsertedContent.ensureEventIsInserted(event);
                    insertRegattas.insert(new RegattaAndEvent(regatta, event));
                    if (leaderboard != null) {
                        for (final RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                            for (final Competitor competitor : raceColumn.getAllCompetitors()) {
                                alreadyInsertedContent.ensureCompetitorIsInserted(competitor);
                                final Fleet fleet = raceColumn.getFleetOfCompetitor(competitor);
                                insertRaceResults.insertBatch(new InsertRaceResultStatement.RaceResult(regatta, leaderboard, competitor, raceColumn, fleet, now));
                            }
                            insertRaceResults.executeBatch();
                            for (final Fleet fleet : raceColumn.getFleets()) {
                                final TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                                if (trackedRace != null) {
                                    final RankingInfo rankingInfo = trackedRace.getRankingMetric().getRankingInfo(now);
                                    final WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache = new LeaderboardDTOCalculationReuseCache(now);
                                    insertRaces.insertBatch(new InsertRaceStatement.TrackedRaceWithRaceColumnAndFleet(trackedRace, raceColumn, fleet));
                                    for (final TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
                                        insertLegs.insertBatch(new InsertLegStatement.TrackedLegAndNow(now, trackedLeg));
                                        for (final Competitor competitor : trackedRace.getRace().getCompetitors()) {
                                            insertLegStats.insertBatch(new InsertLegStatsStatement.TrackedLegOfCompetitorRankingInfoCacheAndNow(now, rankingInfo, cache, trackedLeg.getTrackedLeg(competitor)));
                                        }
                                    }
                                }
                                final Waypoint startWaypoint = trackedRace == null ? null : trackedRace.getRace().getCourse().getFirstWaypoint();
                                if (trackedRace != null) {
                                    for (final Competitor competitor : trackedRace.getRace().getCompetitors()) {
                                        insertRaceStats.insertBatch(new InsertRaceStatsStatement.TrackedRaceWithCompetitorAndStartWaypoint(now, startWaypoint, competitor, trackedRace));
                                        final LinkedHashMap<TimePoint, Maneuver> timepointUniqueManeuvers = new LinkedHashMap<>();
                                        for (final Maneuver maneuver : trackedRace.getManeuvers(competitor, /* waitForLatest */ false)) {
                                            if (maneuver.getType() == ManeuverType.TACK || maneuver.getType() == ManeuverType.JIBE || maneuver.getType() == ManeuverType.PENALTY_CIRCLE) {
                                                timepointUniqueManeuvers.put(maneuver.getTimePoint(), maneuver);
                                            }
                                        }
                                        for (final Maneuver maneuver : timepointUniqueManeuvers.values()) {
                                            insertManeuvers.insertBatch(new InsertManeuverStatement.ManeuverTrackedRaceAndCompetitor(maneuver, trackedRace, competitor));
                                        }
                                    }
                                }
                            }
                            insertRaces.executeBatch();
                            insertLegs.executeBatch();
                            insertLegStats.executeBatch();
                            insertRaceStats.executeBatch();
                            insertManeuvers.executeBatch();
                        }
                    }
                }
            }
        }
    }

    @POST
    @Produces("application/json;charset=UTF-8")
    @Path("createtables")
    public Response createTables(@QueryParam("drop") boolean drop,
            @FormParam("dbendpoint") String dbEndpoint,
            @FormParam("dbuser") String dbUser,
            @FormParam("dbpassword") String dbPassword) throws SQLException, IOException {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.SERVER.getStringPermissionForTypeRelativeIdentifier(
                SecuredSecurityTypes.ServerActions.CAN_EXPORT_MASTERDATA,
                new TypeRelativeObjectIdentifier(ServerInfo.getName())));
        final Connection connection = HanaConnectionFactory.INSTANCE.getConnection(Optional.ofNullable(dbEndpoint),
                Optional.ofNullable(dbUser), Optional.ofNullable(dbPassword));
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
