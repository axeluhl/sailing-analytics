package com.sap.sse.landscape.aws.restapi;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;

import com.sap.sse.rest.StreamingOutputUtil;

@Path("/landscape")
public class LandscapeResource extends StreamingOutputUtil {
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/get_ssh_keys_owned_by_user")
    public Response getSshKeysOwnedByUser(@QueryParam("username[]") final List<String> usernames) throws IOException {
        final JSONArray sshKeysAsJsonArray = null;
        // TODO
        return Response.ok(streamingOutput(sshKeysAsJsonArray)).build();
    }
}
