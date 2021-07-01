package com.sap.sailing.hanaexport.jaxrs.api;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.hanaexport.HanaConnectionFactory;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.shared.server.gateway.jaxrs.SharedAbstractSailingServerResource;
import com.sap.sse.ServerInfo;
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
        logger.info("Done exporting HANA Cloud SAILING DB content on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        return Response.ok().build();
    }

    private void exportIrms(RacingEventService racingEventService, Connection connection) throws SQLException {
        final PreparedStatement insertBoatClasses = connection.prepareStatement(
                "INSERT INTO SAILING.IRM (\"NAME\", \"DISCARDABLE\", \"ADVANCECOMPETITORSTRACKEDWORSE\", \"APPLIESATSTARTOFRACE\") VALUES (?, ?, ?, ?);");
        for (final MaxPointsReason irm : MaxPointsReason.values()) {
            insertBoatClasses.setString(1, irm.name());
            insertBoatClasses.setBoolean(2, irm.isDiscardable());
            insertBoatClasses.setBoolean(3, irm.isAdvanceCompetitorsTrackedWorse());
            insertBoatClasses.setBoolean(4, irm.isAppliesAtStartOfRace());
            insertBoatClasses.execute();
        }
    }

    private void exportBoatClasses(final RacingEventService racingEventService, final Connection connection)
            throws SQLException {
        final PreparedStatement insertBoatClasses = connection.prepareStatement(
                "INSERT INTO SAILING.BOAT_CLASS (\"ID\", \"DESCRIPTION\", \"HULLLENGTHINMETERS\", \"HULLBEAMINMETERS\", \"HULLTYPE\") VALUES (?, ?, ?, ?, ?);");
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
