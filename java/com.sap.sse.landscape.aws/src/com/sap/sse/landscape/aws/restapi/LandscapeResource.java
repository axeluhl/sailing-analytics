package com.sap.sse.landscape.aws.restapi;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.impl.Activator;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.rest.StreamingOutputUtil;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.shared.json.JsonSerializer;

@Path("/landscape")
public class LandscapeResource extends StreamingOutputUtil {
    private static final JsonSerializer<SSHKeyPair> sshKeyPairJsonSerializer = new SSHKeyPairJsonSerializer();
    
    /**
     * Obtains the SSH key pairs whose {@link SSHKeyPair#getCreatorName() creator name} equals that of any of the
     * {@code usernames} passed in any of the {@code username[]} query parameters. The requesting user must have the
     * {@link DefaultActions#READ} permission for the respective SSH keys, otherwise the key won't be added to the
     * response.
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/get_ssh_keys_owned_by_user")
    public Response getSshKeysOwnedByUser(@QueryParam("username[]") final Set<String> usernames) throws IOException {
        final JSONArray sshKeysAsJsonArray = new JSONArray();
        final AwsLandscape<?, ?, ?> landscape = Activator.getInstance().getDefaultLandscape();
        for (final SSHKeyPair sshKeyPair : landscape.getSSHKeyPairs()) {
            if (usernames.contains(sshKeyPair.getCreatorName()) &&
                    SecurityUtils.getSubject().isPermitted(sshKeyPair.getIdentifier().getStringPermission(DefaultActions.READ))) {
                sshKeysAsJsonArray.add(sshKeyPairJsonSerializer.serialize(sshKeyPair));
            }
        }
        return Response.ok(streamingOutput(sshKeysAsJsonArray)).build();
    }

    /**
     * Obtains the time point when the last change in the set of users having the permission {@code LANDSCAPE:MANAGE:AWS} was
     * observed. The earliest time point that may be reported is the time this bundle has been activated.
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/get_time_point_of_last_change_in_ssh_keys_of_aws_landscape_managers")
    public Response getTimePointOfLastChangeInSshKeysOfAwsLandscapeManagers() throws IOException {
        final JSONObject result = new JSONObject();
        final TimePoint timePointOfLastChangeOfSetOfLandscapeManagers = Activator.getInstance().getTimePointOfLastChangeOfSetOfLandscapeManagers();
        result.put("timePointOfLastChangeOfSetOfLandscapeManagers-millis", timePointOfLastChangeOfSetOfLandscapeManagers.asMillis());
        final DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmmX");
        result.put("timePointOfLastChangeOfSetOfLandscapeManagers-iso", isoDateFormat.format(timePointOfLastChangeOfSetOfLandscapeManagers.asDate()));
        return Response.ok(streamingOutput(result)).build();
    }
}
