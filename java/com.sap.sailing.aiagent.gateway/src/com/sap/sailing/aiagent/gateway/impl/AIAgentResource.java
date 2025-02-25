package com.sap.sailing.aiagent.gateway.impl;

import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.aiagent.interfaces.AIAgent;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.shared.server.gateway.jaxrs.SharedAbstractSailingServerResource;
import com.sap.sse.common.Util;

@Path(RestApiApplication.API + RestApiApplication.V1 + AIAgentResource.AI_AGENT)
public class AIAgentResource extends SharedAbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(AIAgentResource.class.getName());

    protected static final String AI_AGENT = "/aiagent";

    private AIAgent getAIAgent() {
        @SuppressWarnings("unchecked")
        ServiceTracker<AIAgent, AIAgent> tracker = (ServiceTracker<AIAgent, AIAgent>) getServletContext()
                .getAttribute(RestServletContainer.AI_AGENT_TRACKER_NAME);
        return tracker.getService();
    }

    @Path("/startcommenting/{eventUUID}")
    @POST
    @Produces("application/json;charset=UTF-8")
    public Response startCommentingEvent(@PathParam("eventUUID") String eventUUID) {
        final Response response;
        final AIAgent aiAgent = getAIAgent();
        final RacingEventService racingEventService = getService();
        final Event event = Util.first(racingEventService.getEventsSelectively(/* include */ true, Collections.singleton(UUID.fromString(eventUUID))));
        if (event != null) {
            getSecurityService().checkCurrentUserUpdatePermission(event);
            logger.info("User "+getSecurityService().getCurrentUser().getName()+" activated AI comments for event "+event.getName()+" with ID "+event.getId());
            aiAgent.startCommentingOnEvent(event);
            response = Response.ok().build();
        } else {
            response = Response
                    .status(Status.NOT_FOUND)
                    .entity("Event with the requested ID not found")
                    .build();
        }
        return response;
    }
    
    @Path("/stopcommenting/{eventUUID}")
    @POST
    @Produces("application/json;charset=UTF-8")
    public Response stopCommentingEvent(@PathParam("eventUUID") String eventUUID) {
        final Response response;
        final AIAgent aiAgent = getAIAgent();
        final RacingEventService racingEventService = getService();
        final Event event = Util.first(racingEventService.getEventsSelectively(/* include */ true, Collections.singleton(UUID.fromString(eventUUID))));
        if (event != null) {
            getSecurityService().checkCurrentUserUpdatePermission(event);
            logger.info("User "+getSecurityService().getCurrentUser().getName()+" de-activated AI comments for event "+event.getName()+" with ID "+event.getId());
            aiAgent.stopCommentingOnEvent(event);
            response = Response.ok().build();
        } else {
            response = Response
                    .status(Status.NOT_FOUND)
                    .entity("Event with the requested ID not found")
                    .build();
        }
        return response;
    }
    
}
