package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.hanaexport.HanaConnectionFactory;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.shared.server.gateway.jaxrs.SharedAbstractSailingServerResource;
import com.sap.sse.ServerInfo;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

@Path("/v1/exporter")
public class HanaCloudSacExportResource extends SharedAbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(HanaCloudSacExportResource.class.getName());
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("clear")
    public Response clear() throws SQLException {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.SERVER.getStringPermissionForTypeRelativeIdentifier(
                SecuredSecurityTypes.ServerActions.CAN_EXPORT_MASTERDATA,
                new TypeRelativeObjectIdentifier(ServerInfo.getName())));
        logger.info("Clearing HANA Cloud SAILING DB on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        final Connection connection = HanaConnectionFactory.INSTANCE.getConnection();
        connection.createStatement().execute("DELETE FROM SAILING.BOAT_CLASS");
        logger.info("Done learing HANA Cloud SAILING DB on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        return Response.ok().build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("export")
    public Response export() throws SQLException {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.SERVER.getStringPermissionForTypeRelativeIdentifier(
                SecuredSecurityTypes.ServerActions.CAN_EXPORT_MASTERDATA,
                new TypeRelativeObjectIdentifier(ServerInfo.getName())));
        logger.info("Exporting HANA Cloud SAILING DB content on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        final RacingEventService racingEventService = getService();
        final Connection connection = HanaConnectionFactory.INSTANCE.getConnection();
        final PreparedStatement insertBoatClasses = connection.prepareStatement("INSERT INTO SAILING.BOAT_CLASS (\"id\", \"description\") VALUES (?, ?);");
        for (final BoatClass boatClass : racingEventService.getBaseDomainFactory().getBoatClasses()) {
            insertBoatClasses.setString(1, boatClass.getName());
            insertBoatClasses.setString(2, "Type "+boatClass.getHullType().name()+", length "+
                    boatClass.getHullLength().getMeters()+"m, beam "+boatClass.getHullBeam().getMeters()+"m");
            insertBoatClasses.execute();
        }
        logger.info("Done exporting HANA Cloud SAILING DB content on behalf of user "+SecurityUtils.getSubject().getPrincipal());
        return Response.ok().build();
    }
}
