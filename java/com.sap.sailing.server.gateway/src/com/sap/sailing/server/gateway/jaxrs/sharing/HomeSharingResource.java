package com.sap.sailing.server.gateway.jaxrs.sharing;

import java.util.Map;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.security.SecurityService;

/**
 * Root resource (exposed at "helloworld" path)
 */
@Path("/home")
public class HomeSharingResource extends AbstractSailingServerResource{
    @Context
    private UriInfo context;

    /** Creates a new instance of HelloWorld */
    public HomeSharingResource() {
    }

    /**
     * Retrieves representation of an instance of helloWorld.HelloWorld
     * @return an instance of java.lang.String
     */
    @GET
    @Path("/events/{eventId}")
    @Produces("text/html")
    public String getSharedEvent(@PathParam("eventId") String eventId) {
        RacingEventService eventService = getService();
        UUID uuid = UUID.fromString(eventId);
        final Event event = eventService.getEvent(uuid);
        if (event != null) {
            SecurityService securityService = getSecurityService();
            securityService.checkCurrentUserReadPermission(event);
            final String title = event.getName();
            final String description = HomeSharingUtils.findDescription(event);
            final String imageUrl = HomeSharingUtils.findSpecificTeaserImageUrl(event);
            String placeUrl = new TokenizedHomePlaceUrl(context).asEventUrl(eventId);
            final Map<String, String> replacementMap = HomeSharingUtils.createReplacementMap(context, title,
                    description, imageUrl, placeUrl);
            String content = HomeSharingUtils.loadSharingHTML(this.getClass().getClassLoader(), context);
            return HomeSharingUtils.replaceMetatags(content, replacementMap);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @GET
    @Path("/events/{eventId}/regattas/{regattaId}")
    @Produces("text/html")
    public String getSharedRegatta(@PathParam("eventId") String eventId, @PathParam("regattaId") String regattaId) {
        return null;
    }
    
    @GET
    @Path("/series/{seriesId}")
    @Produces("text/html")
    public String getSharedSeries(@PathParam("seriesId") String seriesId) {
        return null;
    }
    
}