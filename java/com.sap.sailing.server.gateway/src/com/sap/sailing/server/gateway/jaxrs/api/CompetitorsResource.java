package com.sap.sailing.server.gateway.jaxrs.api;

import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.impl.NationalityJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PersonJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.TeamJsonSerializer;

@Path("/v1/competitors")
public class CompetitorsResource extends AbstractSailingServerResource {
	
	private static final Logger logger = Logger.getLogger(CompetitorsResource.class.getName());
	
    public static JSONObject getCompetitorJSON(Competitor competitor) {
        //see http://wiki.sapsailing.com/wiki/tracking-app-api-v1-draft#Competitor-Information
        JSONObject json = new JSONObject();
        json.put("id", competitor.getId().toString());
        json.put("name", competitor.getName());
        json.put("sailID", competitor.getBoat().getSailID());
        json.put("nationality", competitor.getTeam().getNationality().getThreeLetterIOCAcronym());
        json.put("countryCode", competitor.getTeam().getNationality().getCountryCode().getTwoLetterISOCode());
        json.put("boatClassName", competitor.getBoat().getBoatClass().getName());

        return json;
    }
    
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
            String jsonString = getCompetitorJSON(competitor).toJSONString();
            response = Response.ok(jsonString, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{competitor-id}/team")
    public Response getTeam(@PathParam("competitor-id") String competitorId) {
        Competitor competitor = getService().getCompetitorStore().getExistingCompetitorByIdAsString(competitorId);
        
        if (competitor == null){ 
        	return Response.status(Status.NOT_FOUND)
                    .entity("Could not find a competitor with id '" + competitorId + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        
        Team team = competitor.getTeam();
    
        if (team == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Could not find a team associated with competitor '" + competitorId + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } 
            
        TeamJsonSerializer teamJsonSerializer = new TeamJsonSerializer(new PersonJsonSerializer(new NationalityJsonSerializer()));
        JSONObject teamJson = teamJsonSerializer.serialize(team);
        String json = teamJson.toJSONString();
        
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{competitor-id}/team/image")
    public Response setTeamImage(String json, @PathParam("competitor-id") String competitorId) {
    	Competitor competitor = getService().getCompetitorStore().getExistingCompetitorByIdAsString(competitorId);
    	TeamImpl team = (TeamImpl) competitor.getTeam();
    	
        Object requestBody;
        JSONObject requestObject;
        try {
            requestBody = JSONValue.parseWithException(json);
            requestObject = Helpers.toJSONObjectSafe(requestBody);
        } catch (ParseException | JsonDeserializationException e) {
            logger.warning(String.format("Exception while parsing post request:\n%s", e.toString()));
            return Response.status(Status.BAD_REQUEST).entity("Invalid JSON body in request")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        
        String teamImageURIAsString = (String) requestObject.get(TeamJsonSerializer.FIELD_IMAGE_URI);
        
        if (teamImageURIAsString == null){
        	return Response.status(Status.BAD_REQUEST).entity("Invalid JSON body in request")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        
        URI teamImageURI;
        try {
        	teamImageURI = URI.create(teamImageURIAsString);
        } catch (IllegalArgumentException e){
        	return Response.status(Status.BAD_REQUEST).entity("Invalid imageURI in JSON request body")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        
        team.setImage(teamImageURI);
    	
        return Response.status(Status.OK).build();
    }
}
