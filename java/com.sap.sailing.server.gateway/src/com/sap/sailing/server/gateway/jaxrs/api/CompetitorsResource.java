package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;

@Path("/v1/competitors")
public class CompetitorsResource extends AbstractSailingServerResource {
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{competitorId}")
    public Response getCompetitor(@PathParam("competitorId") String competitorIdAsString) {
        Response response;
        Competitor competitor = getService().getCompetitorStore().getExistingCompetitorByIdAsString(
                competitorIdAsString);
        if (competitor == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a competitor with id '" + competitorIdAsString + "'.").type(MediaType.TEXT_PLAIN)
                    .build();
        } else {
            //see http://wiki.sapsailing.com/wiki/tracking-app-api-v1-draft#Competitor-Information
            JSONObject json = new JSONObject();
            json.put("id", competitorIdAsString);
            json.put("name", competitor.getName());
            json.put("sailID", competitor.getBoat().getSailID());
            json.put("nationality", competitor.getTeam().getNationality().getThreeLetterIOCAcronym());
            json.put("countryCode", competitor.getTeam().getNationality().getCountryCode().getTwoLetterISOCode());
            json.put("boatClassName", competitor.getBoat().getBoatClass().getName());

            String jsonString = json.toJSONString();
            response = Response.ok(jsonString, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }
}
