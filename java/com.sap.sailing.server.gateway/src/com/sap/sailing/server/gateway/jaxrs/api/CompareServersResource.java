package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.LeaderboardGroupConstants;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.util.RemoteServerUtil;
import com.sap.sse.util.HttpUrlConnectionHelper;

@Path("/v1/compareservers")
public class CompareServersResource extends AbstractSailingServerResource {
    public static final Logger logger = Logger.getLogger(CompareServersResource.class.getName());

    private static final String LEADERBOARDGROUPSPATH = "/sailingserver/api/v1/leaderboardgroups";
    private static final String LEADERBOARDGROUPSIDENTIFIABLEPATH = LEADERBOARDGROUPSPATH+"/identifiable";
    protected static final String SERVER1_FORM_PARAM = "server1";
    protected static final String SERVER2_FORM_PARAM = "server2";
    protected static final String USER1_FORM_PARAM = "user1";
    protected static final String USER2_FORM_PARAM = "user2";
    protected static final String PASSWORD1_FORM_PARAM = "password1";
    protected static final String PASSWORD2_FORM_PARAM = "password2";
    protected static final String BEARER1_FORM_PARAM = "bearer1";
    protected static final String BEARER2_FORM_PARAM = "bearer2";
    protected static final String LEADERBOARDGROUP_UUID_FORM_PARAM = "leaderboardgroupUUID[]";
    /**
     * The list of keys that are compared during a compare run.
     */
    private static final String[] KEYLISTTOCOMPARE = new String[] { LeaderboardGroupConstants.ID,
            LeaderboardGroupConstants.DESCRIPTION, LeaderboardGroupConstants.EVENTS,
            LeaderboardGroupConstants.LEADERBOARDS, LeaderboardGroupConstants.DISPLAYNAME,
            LeaderboardNameConstants.ISMETALEADERBOARD, LeaderboardNameConstants.ISREGATTALEADERBOARD,
            LeaderboardNameConstants.SCORINGCOMMENT, LeaderboardNameConstants.LASTSCORINGUPDATE,
            LeaderboardNameConstants.SCORINGSCHEME, LeaderboardNameConstants.REGATTANAME,
            LeaderboardNameConstants.SERIES, LeaderboardNameConstants.ISMEDALSERIES, LeaderboardNameConstants.FLEETS,
            LeaderboardNameConstants.COLOR, LeaderboardNameConstants.ORDERING, LeaderboardNameConstants.RACES,
            LeaderboardNameConstants.ISMEDALRACE, LeaderboardNameConstants.ISTRACKED,
            LeaderboardNameConstants.REGATTANAME, LeaderboardNameConstants.TRACKEDRACENAME,
            LeaderboardNameConstants.HASGPSDATA, LeaderboardNameConstants.HASWINDDATA };
    private static final Set<String> KEYSETTOCOMPARE = new HashSet<>(Arrays.asList(KEYLISTTOCOMPARE));
    /**
     * The list of keys that are not compared.
     */
    private static final String[] KEYSTOIGNORE = new String[] { LeaderboardGroupConstants.TIMEPOINT,
            LeaderboardNameConstants.RACEVIEWERURLS };
    private static final Set<String> KEYSETTOIGNORE = new HashSet<>(Arrays.asList(KEYSTOIGNORE));
    /**
     * The list of keys that get always printed. "name" needs a special treatment, as it should be printed, but also
     * during a compare run there is no need to compare entries with different values for "name".
     */
    private static final String[] KEYSTOPRINT = new String[] { LeaderboardGroupConstants.ID };
    private static final Set<String> KEYSETTOPRINT = new HashSet<>(Arrays.asList(KEYSTOPRINT));

    private static final String SERVERTOOLD = "At least one server you are trying to compare has not yet enabled the "
            + LEADERBOARDGROUPSIDENTIFIABLEPATH
            + " endpoint and therefore you need to fallback to running the compareServers shell script.";
    
    public CompareServersResource() {
    }
    
    @Context
    UriInfo uriInfo;
    
    /**
     * @param server1
     *            optional; if not provided, the server receiving this request will act as the default for "server1"
     * @param user1
     *            the username for authenticating the request for {@code server1}, together with {@code password1};
     *            alternatively to {@code user1} and {@code password1} clients may specify {@code bearer1}. If none of
     *            these are provided, this request's authentication will be used to obtain a bearer token for
     *            {@code server1}.
     * @param password1
     *            use together with {@code user1} to provide authentication for the requests for {@code server1}
     *            (defaulting to this server).
     * @param bearer1
     *            alternative for {@code user1}/{@code password1}, specifying a bearer token that is used to
     *            authenticate a user on {@code server1} (which defaults to the server handling this request). If
     *            neither of {@code user1}, {@code password1} and {@core bearer1} are provided, this request's
     *            authentication is used to obtain a bearer token which is then used to authenticate the requests for
     *            {@code server1}.
     * @param server2
     *            mandatory; specifies the host name or IP address of the host against which to compare the
     *            {@code server1} content
     * @param user2
     *            the username for authenticating the request for {@code server2}, together with {@code password2};
     *            alternatively to {@code user2} and {@code password2} clients may specify {@code bearer2}. If none of
     *            these are provided, this request's authentication will be used to obtain a bearer token for
     *            {@code server2}, assuming that the server responding to this request and {@code server2} share a
     *            common {@code SecurityService} through replication.
     * @param password2
     *            use together with {@code user2} to provide authentication for the requests for {@code server2}.
     * @param bearer2
     *            alternative for {@code user2}/{@code password2}, specifying a bearer token that is used to
     *            authenticate a user on {@code server2}. If neither of {@code user2}, {@code password2} and
     *            {@core bearer2} are provided, this request's authentication is used to obtain a bearer token which is
     *            then used to authenticate the requests for {@code server1}, assuming that the server responding to
     *            this request and {@code server2} share a common {@code SecurityService} through replication.
     * @param uuidset
     *            can optionally be used to specify a set of UUIDs identifying leaderboard groups to compare. If not
     *            specified (represented as an {@link Set#isEmpty() empty} set), all leaderboard groups found on both,
     *            {@code server1} and {@code server2} will be compared.
     */
    @POST
    @Produces("application/json;charset=UTF-8")
    public Response compareServers(
            @FormParam(SERVER1_FORM_PARAM) String server1, 
            @FormParam(SERVER2_FORM_PARAM) String server2,
            @FormParam(LEADERBOARDGROUP_UUID_FORM_PARAM) Set<String> uuidset,
            @FormParam(USER1_FORM_PARAM) String user1,
            @FormParam(USER2_FORM_PARAM) String user2,
            @FormParam(PASSWORD1_FORM_PARAM) String password1,
            @FormParam(PASSWORD2_FORM_PARAM) String password2,
            @FormParam(BEARER1_FORM_PARAM) String bearer1,
            @FormParam(BEARER2_FORM_PARAM) String bearer2) {
        final Map<String, Set<Object>> result = new HashMap<>();
        Response response = null;
        final String effectiveServer1;
        effectiveServer1 = !Util.hasLength(server1) ? uriInfo.getBaseUri().getAuthority() : server1;
        if (!validateParameters(server2, uuidset, user1, user2, password1, password2, bearer1, bearer2)) {
            response = badRequest("Specify two server names and optionally a set of valid leaderboardgroup UUIDs.");
        } else {
            final String token1 = getService().getOrCreateTargetServerBearerToken(effectiveServer1, user1, password1, bearer1);
            final String token2 = getService().getOrCreateTargetServerBearerToken(server2, user2, password2, bearer2);
            result.put(effectiveServer1, new HashSet<>());
            result.put(server2, new HashSet<>());
            try {
                if (!uuidset.isEmpty()) {
                    for (String uuid : uuidset) {
                        Pair<Object, Object> jsonPair = fetchLeaderboardgroupDetailsAndRemoveDuplicates(effectiveServer1,
                                server2, uuid, token1, token2);
                        if (jsonPair.getA() != null && jsonPair.getB() != null) {
                            result.get(effectiveServer1).add(jsonPair.getA());
                            result.get(server2).add(jsonPair.getB());
                        }
                    }
                } else {
                    final JSONArray leaderboardgroupList1 = getLeaderboardgroupList(effectiveServer1, token1);
                    final JSONArray leaderboardgroupList2 = getLeaderboardgroupList(server2, token2);
                    for (Object lg1 : leaderboardgroupList1) {
                        if (!leaderboardgroupList2.contains(lg1)) {
                            result.get(effectiveServer1).add(lg1);
                        } else {
                            final String lgId = ((JSONObject) lg1).get(LeaderboardGroupConstants.ID).toString();
                            Pair<Object, Object> jsonPair = fetchLeaderboardgroupDetailsAndRemoveDuplicates(effectiveServer1,
                                    server2, lgId, token1, token2);
                            if (jsonPair.getA() != null && jsonPair.getB() != null) {
                                result.get(effectiveServer1).add(jsonPair.getA());
                                result.get(server2).add(jsonPair.getB());
                            }
                        }
                    }
                    for (Object lg2 : leaderboardgroupList2) {
                        if (!leaderboardgroupList1.contains(lg2)) {
                            result.get(server2).add(lg2);
                        }
                    }
                }
                JSONObject json = new JSONObject();
                for (Entry<String, Set<Object>> entry : result.entrySet()) {
                    json.put(entry.getKey(), entry.getValue());
                }
                if (result.get(effectiveServer1).isEmpty() && result.get(server2).isEmpty()) {
                    response = Response.ok(streamingOutput(json)).build();
                } else {
                    response = Response.status(Status.CONFLICT).entity(streamingOutput(json)).build();
                }
            } catch (FileNotFoundException e) {
                response = Response.status(Status.CONFLICT).entity(e.toString()).build();
                logger.warning(e.toString());
            } catch (ConnectException e) {
                response = Response.status(Status.CONFLICT).entity(e.toString()).build();
                logger.warning(e.toString());
            } catch (Exception e) {
                response = returnInternalServerError(e);
            }
        }
        return response;
    }

    private boolean validateParameters(String server2, Set<String> uuidset, String user1, String user2, String password1,
            String password2, String bearer1, String bearer2) {
        boolean result = validateAuthenticationParameters(user1, password1, bearer1) && validateAuthenticationParameters(user2, password2, bearer2) &&
                    validateParameters(server2, uuidset);
        return result;
    }
    
    /**
     * Validates the {@code uuidset} so that it contains only valid UUIDs that parse properly, and ensures that {@code server2}
     * is set
     */
    private boolean validateParameters(String server2, Set<String> uuidset) {
        boolean result = Util.hasLength(server2);
        for (String uuid : uuidset) {
            if (Util.hasLength(uuid) && !UUID.fromString(uuid).toString().equals(uuid)) {
                result = false;
                break;
            }
        }
        return result;
    }
    
    /**
     * Fetches the details for a given leaderboardgroup UUID and removes all the duplicates in the fields.
     */
    private Pair<Object, Object> fetchLeaderboardgroupDetailsAndRemoveDuplicates(String server1, String server2,
            String leaderboardgroupId, String bearer1, String bearer2) throws Exception {
        Object lgdetail1 = getLeaderboardgroupDetailsById(leaderboardgroupId, RemoteServerUtil.createBaseUrl(server1),
                bearer1);
        lgdetail1 = removeUnnecessaryFields(lgdetail1);
        Object lgdetail2 = getLeaderboardgroupDetailsById(leaderboardgroupId, RemoteServerUtil.createBaseUrl(server2),
                bearer2);
        lgdetail2 = removeUnnecessaryFields(lgdetail2);
        Pair<Object, Object> result = removeDuplicateEntries(lgdetail1, lgdetail2);
        return result;
    }
    /**
     * Fetches the leaderboardgrouplist from a server.
     */
    private JSONArray getLeaderboardgroupList(String server, String bearer) throws Exception {
        final JSONParser parser = new JSONParser();
        final URL baseUrl = RemoteServerUtil.createBaseUrl(server);
        final URLConnection leaderboardgroupListC = HttpUrlConnectionHelper.redirectConnectionWithBearerToken(
                RemoteServerUtil.createRemoteServerUrl(baseUrl, LEADERBOARDGROUPSIDENTIFIABLEPATH, null), bearer);
        if (((HttpURLConnection) leaderboardgroupListC).getResponseCode() == 404) {
            throw new FileNotFoundException(SERVERTOOLD);
        }
        final JSONArray result = (JSONArray) parser
                .parse(new InputStreamReader(leaderboardgroupListC.getInputStream(), "UTF-8"));
        return result;
    }
    
    /**
     * Fetches the JSON for a given leaderboardgroup UUID.
     */
    private Object getLeaderboardgroupDetailsById(String leaderboardgroupId, URL baseUrl, String bearer) throws Exception {
        final URLConnection lgdetailc = HttpUrlConnectionHelper.redirectConnectionWithBearerToken(
                RemoteServerUtil.createRemoteServerUrl(baseUrl, createLgDetailPath(leaderboardgroupId), null), bearer);
        Object result = JSONValue.parse(new InputStreamReader(lgdetailc.getInputStream(), "UTF-8"));
        return result;
    }

    private String createLgDetailPath(String leaderboardgroupId) throws URISyntaxException {
        final String result;
        final StringBuilder lgdetailpath = new StringBuilder(LEADERBOARDGROUPSPATH);
        lgdetailpath.append("/");
        lgdetailpath.append(leaderboardgroupId);
        result = lgdetailpath.toString();
        return result;
    }
    
    /**
     * Strips a (nested) {@link org.json.simple.JSONObject} from the fields specified in
     * {@link CompareServersResource#KEYSTOIGNORE}.
     *
     * @return The modified {@link org.json.simple.JSONObject}.
     */
    
    private Object removeUnnecessaryFields(Object json) {
        if (json instanceof JSONObject) {
            Iterator<Object> iter = ((JSONObject) json).keySet().iterator();
            while (iter.hasNext()) {
                Object key = iter.next();
                if (KEYSETTOIGNORE.contains(key)) {
                    iter.remove();
                } else {
                    Object value = ((JSONObject) json).get(key);
                    removeUnnecessaryFields(value);
                }
            }
        } else if (json instanceof JSONArray) {
            for (int i = 0; i < ((JSONArray) json).size(); i++) {
                removeUnnecessaryFields(((JSONArray) json).get(i));
            }
        }
        return json;
    }

    /**
     * Takes two (nested) {@link org.json.simple.JSONObject}'s and compares them recursively. They will be modified in
     * place, the keys by which they get compared are listed in {@link CompareServersResource#KEYLISTTOCOMPARE}.
     * 
     * @return the two (nested) {@link org.json.simple.JSONObject}'s, stripped by all fields and values that are equal
     *         for both.
     */
    private Pair<Object, Object> removeDuplicateEntries(Object lg1, Object lg2) {
        Pair<Object, Object> result = new Pair<Object, Object>(null, null);
        if (lg1.equals(lg2)) {
            return result;
        }
        else if (lg1 instanceof JSONObject && lg2 instanceof JSONObject) {
            removeDuplicateEntries((JSONObject) lg1, (JSONObject) lg2);
        } else if (lg1 instanceof JSONArray && lg2 instanceof JSONArray) {
            removeDuplicateEntries((JSONArray) lg1, (JSONArray) lg2);
        }
        result = new Pair<Object, Object>(lg1, lg2);
        return result;
    }
    
    
    private Pair<Object, Object> removeDuplicateEntries(JSONObject lg1, JSONObject lg2) {
        Pair<Object, Object> result = new Pair<Object, Object>(null, null);
        final Iterator<Object> iter1 = lg1.keySet().iterator();
        while (iter1.hasNext()) {
            Object key = iter1.next();
            if (lg2.containsKey(key)) {
                Object value1 = lg1.get(key);
                Object value2 = lg2.get(key);
                if (key.equals(LeaderboardGroupConstants.NAME) && !Util.equalsWithNull(value1, value2)) {
                    break;
                } else if (KEYSETTOPRINT.contains(key) && Util.equalsWithNull(value1, value2)) {
                    continue;
                } else if (Util.equalsWithNull(value1, value2) && KEYSETTOCOMPARE.contains(key)) {
                    iter1.remove();
                    lg2.remove(key);
                } else {
                    removeDuplicateEntries(value1, value2);
                }
            }
        }
        return result;
    }

    private Pair<Object, Object> removeDuplicateEntries(JSONArray json1, JSONArray json2) {
        Pair<Object, Object> result = new Pair<Object, Object>(null, null);
        if (json1.equals(json2)) {
            ((JSONArray) json1).clear();
            ((JSONArray) json2).clear();
        } else {
            final Iterator<Object> iter1 = ((JSONArray) json1).iterator();
            while (iter1.hasNext()) {
                Object item = iter1.next();
                if (((JSONArray) json2).contains(item)) {
                    ((JSONArray) json2).remove(item);
                    iter1.remove();
                } else {
                    final Iterator<Object> iter2 = ((JSONArray) json2).iterator();
                    while (iter2.hasNext()) {
                        removeDuplicateEntries(item, iter2.next());
                    }
                }
            }
        }
        return result;
    }
}
