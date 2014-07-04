package com.sap.sse.security.jaxrs.api;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sap.sse.security.jaxrs.AbstractSecurityResource;
import com.sap.sse.security.userstore.shared.UserManagementException;

@Path("/restsecurity")
public class SecurityResource extends AbstractSecurityResource {

    private Response getSecurityErrorResponse(String msg) {
        return  Response.status(Status.BAD_REQUEST).entity(msg).type(MediaType.TEXT_PLAIN).build();
    }

//    @GET
//    @Produces("application/json;charset=UTF-8")
//    public Response getEvents() {
//        JsonSerializer<EventBase> eventSerializer = new EventBaseJsonSerializer(new VenueJsonSerializer(new CourseAreaJsonSerializer()), new LeaderboardGroupBaseJsonSerializer());
//        JSONArray result = new JSONArray();
//        for (EventBase event : getService().getAllEvents()) {
//            if (event.isPublic()) {
//                result.add(eventSerializer.serialize(event));
//            }
//        }
//        String json = result.toJSONString();
//        return Response.ok(json, MediaType.APPLICATION_JSON).build();
//    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response sayHello(){
        return Response.ok("Hello!", MediaType.TEXT_PLAIN).build();
    }
    
    @POST
    @Path("/login")
    @Produces("application/json;charset=UTF-8")
    public Response login(@FormParam("username") String username, @FormParam("password") String password){
        System.out.println("Logging in " + username + " with pw: " + password);
        try {
            getService().login(username, password);
        } catch (UserManagementException e) {
            return getSecurityErrorResponse(e.getMessage());
        }
        return Response.ok("Logged in!", MediaType.TEXT_PLAIN).build();
    }
}
 