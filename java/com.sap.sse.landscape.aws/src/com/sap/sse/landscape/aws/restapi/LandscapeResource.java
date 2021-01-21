package com.sap.sse.landscape.aws.restapi;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;

import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.rest.StreamingOutputUtil;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.shared.json.JsonSerializer;

@Path("/landscape")
public class LandscapeResource extends StreamingOutputUtil {
    private static final JsonSerializer<SSHKeyPair> sshKeyPairJsonSerializer = new SSHKeyPairJsonSerializer();
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/get_ssh_keys_owned_by_user")
    public Response getSshKeysOwnedByUser(@QueryParam("username[]") final Set<String> usernames) throws IOException {
        final JSONArray sshKeysAsJsonArray = new JSONArray();
        final AwsLandscape<?, ?, ?> landscape = AwsLandscape.obtain();
        for (final SSHKeyPair sshKeyPair : landscape.getSSHKeyPairs()) {
            if (usernames.contains(sshKeyPair.getCreatorName()) &&
                    SecurityUtils.getSubject().isPermitted(sshKeyPair.getIdentifier().getStringPermission(DefaultActions.READ))) {
                sshKeysAsJsonArray.add(sshKeyPairJsonSerializer.serialize(sshKeyPair));
            }
        }
        return Response.ok(streamingOutput(sshKeysAsJsonArray)).build();
    }
}
