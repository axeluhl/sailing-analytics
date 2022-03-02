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

    /**
     * Looks for a group with the name provided in parameter {@code userGroupName}. If found, the group's {@link UUID} is
     * returned, otherwise {@code null}.
     */
    UUID getUserGroupIdByName(String userGroupName) throws MalformedURLException, ClientProtocolException, IOException, ParseException, IllegalAccessException;

    Pair<UUID, String> getGroupAndUserOwner(HasPermissions type, TypeRelativeObjectIdentifier typeRelativeObjectId)
            throws ClientProtocolException, IOException, ParseException;

    Iterable<Pair<WildcardPermission, Boolean>> hasPermissions(Iterable<WildcardPermission> permissions) throws UnsupportedEncodingException, MalformedURLException, ClientProtocolException, IOException, ParseException;
    /**
     * The name of the user authenticated by the credentials used by this facade object.
     */
    String getUsername() throws MalformedURLException, ClientProtocolException, IOException, ParseException;

    /**
     * If the user authenticated for this server is permitted to update the user group identified by the {@code userGroupId}, the
     * current user is added to the group. If the current user is already part of the group, this method does nothing.
     */
    default void addCurrentUserToGroup(UUID userGroupId) throws ClientProtocolException, IOException, ParseException {
        addUserToGroup(userGroupId, getUsername());
    }

    /**
     * If the user authenticated for this server is permitted to update the user group identified by the
     * {@code userGroupId}, the user specified by {@code username} is added to the group. If the current user is already
     * part of the group, this method does nothing.
     */
    void addUserToGroup(UUID userGroupId, String username) throws ClientProtocolException, IOException, ParseException;
    
    /**
     * If the user authenticated for this server is permitted to update the user group identified by the {@code userGroupId}, the
     * current user is removed from the group. If the current user is not part of the group, this method does nothing.
     */
    default void removeCurrentUserFromGroup(UUID userGroupId) throws ClientProtocolException, MalformedURLException, IOException, ParseException {
        removeUserFromGroup(userGroupId, getUsername());
    }

    /**
     * If the user authenticated for this server is permitted to update the user group identified by the {@code userGroupId}, the
     * user specified by {@code username} is removed from the group. If the current user is not part of the group, this method does nothing.
     */
    void removeUserFromGroup(UUID userGroupId, String username) throws ClientProtocolException, IOException, ParseException;

    /**
     * Create a user group named {@code serverGroupName} if no group by that name exists yet. The group will be owned by the
     * user authenticated for this server, and the user will be added to the group.
     */
    UUID createUserGroupAndAddCurrentUser(String serverGroupName) throws ClientProtocolException, IOException, ParseException, IllegalAccessException;

    Iterable<String> getNamesOfUsersInGroup(UUID userGroupId) throws ClientProtocolException, IOException, ParseException;
}
