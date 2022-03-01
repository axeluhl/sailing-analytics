package com.sap.sse.security.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * Represents a remote instance of a server process or an entire application replica set with a master and zero or more
 * replicas, running at least this web bundle, reachable under the {@code /security/api} URL path, exposing the REST
 * API of this bundle. In short, this is a Java facade for a REST API.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 */
public interface SecuredServer {
    String SECURITY_API_PREFIX = "security/api";

    /**
     * The server's base URL, ending with a slash "/" character
     */
    URL getBaseUrl();
    
    String getBearerToken();

    UUID getUserGroupIdByName(String userGroupName) throws MalformedURLException, ClientProtocolException, IOException, ParseException;

    Pair<UUID, String> getGroupAndUserOwner(HasPermissions type, TypeRelativeObjectIdentifier typeRelativeObjectId)
            throws ClientProtocolException, IOException, ParseException;

    Iterable<Pair<WildcardPermission, Boolean>> hasPermissions(Iterable<WildcardPermission> permissions) throws UnsupportedEncodingException, MalformedURLException, ClientProtocolException, IOException, ParseException;
    /**
     * The name of the user authenticated by the credentials used by this facade object.
     */
    String getUsername() throws MalformedURLException, ClientProtocolException, IOException, ParseException;
}
