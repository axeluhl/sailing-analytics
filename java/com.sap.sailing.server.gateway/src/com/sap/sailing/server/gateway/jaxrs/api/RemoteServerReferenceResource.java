package com.sap.sailing.server.gateway.jaxrs.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.authz.UnauthorizedException;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.operationaltransformation.AddRemoteSailingServerReference;
import com.sap.sailing.server.operationaltransformation.RemoveRemoteSailingServerReference;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;
import com.sap.sse.security.util.RemoteServerUtil;

@Path ("/v1/remoteserverreference")
public class RemoteServerReferenceResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(RemoteServerReferenceResource.class.getName());
    
    public RemoteServerReferenceResource() {
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path ("/add")
    public Response addRemoteServerReference(
            @FormParam("remoteServerUrl") String remoteServerUrlAsString,
            @FormParam("remoteServerName") String remoteServerName) {
        Response response = null;
        if (!Util.hasLength(remoteServerUrlAsString) || !Util.hasLength(remoteServerName)) {
            response = badRequest();
        } else {
            try {
                final URL remoteServerUrl = RemoteServerUtil.createBaseUrl(remoteServerUrlAsString);
                getSecurityService().checkCurrentUserServerPermission(ServerActions.CONFIGURE_REMOTE_INSTANCES);
                final RemoteSailingServerReference serverRef = getService()
                        .apply(new AddRemoteSailingServerReference(remoteServerName, remoteServerUrl));
                final JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("remoteServerNameAdded", serverRef.getName());
                jsonResponse.put("remoteServerUrlAdded", serverRef.getURL().toString());
                jsonResponse.put("remoteServerEventsAdded", getRemoteEventsList(serverRef));
                response = Response.ok(streamingOutput(jsonResponse))
                        .header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
            } catch (UnauthorizedException e) {
                response = returnUnauthorized(e);
            } catch (MalformedURLException e) {
                response = badRequest();
                logger.warning(e.getMessage() + " for URL: " + remoteServerUrlAsString);
            } catch (Throwable e) {
                response = returnInternalServerError(e);
            }
        }
        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path ("/remove")
    public Response removeRemoteServerReference(
            @FormParam("remoteServerName") String remoteServerName) {
        Response response = null;
        if (!Util.hasLength(remoteServerName)) {
            response = badRequest();
        } else {
            try {
                getSecurityService().checkCurrentUserServerPermission(ServerActions.CONFIGURE_REMOTE_INSTANCES);
                final RemoteSailingServerReference serverRef = getService()
                        .getRemoteServerReferenceByName(remoteServerName);
                if (serverRef == null) {
                    response = Response.status(Status.NOT_FOUND)
                            .entity("remoteServerName: \"" + remoteServerName + "\" doesn't exist on this server.")
                            .build();
                } else {
                    final JSONObject jsonResponse = new JSONObject();
                    final List<String> remoteEventList = getRemoteEventsList(serverRef);
                    getService().apply(new RemoveRemoteSailingServerReference(remoteServerName));
                    jsonResponse.put("remoteServerNameRemoved", serverRef.getName());
                    jsonResponse.put("remoteServerUrlRemoved", serverRef.getURL().toString());
                    jsonResponse.put("remoteServerEventsRemoved", remoteEventList);
                    response = Response.ok(streamingOutput(jsonResponse))
                            .header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
                }
            } catch (UnauthorizedException e) {
                response = returnUnauthorized(e);
            } catch (Throwable e) {
                response = returnInternalServerError(e);
            }
        }
        return response;
    }

    private List<String> getRemoteEventsList(RemoteSailingServerReference serverRef) {
        final Pair<Iterable<EventBase>, Exception> remoteServerEventsAndExceptions = getService()
                .updateRemoteServerEventCacheSynchronously(serverRef, false);
        final List<String> remoteEventsList = new LinkedList<>();
        for (EventBase event : remoteServerEventsAndExceptions.getA()) {
            remoteEventsList.add(event.getName());
        }
        return remoteEventsList;
    }

    private Response returnUnauthorized(UnauthorizedException e) {
        final Response response = Response.status(Status.UNAUTHORIZED).build();
        logger.warning(e.getMessage() + " for user: " + getSecurityService().getCurrentUser());
        return response;
    }

    private Response returnInternalServerError(Throwable e) {
        final Response response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        logger.severe(e.toString());
        return response;
    }

    private Response badRequest() {
        final Response response = Response.status(Status.BAD_REQUEST).build();
        return response;
    }
}
