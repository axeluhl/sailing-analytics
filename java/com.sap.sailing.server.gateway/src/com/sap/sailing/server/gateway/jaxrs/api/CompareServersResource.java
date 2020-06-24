package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.InputStreamReader;
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
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.util.RemoteServerUtil;
import com.sap.sse.util.HttpUrlConnectionHelper;

@Path("/v1/compareservers")
public class CompareServersResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(CompareServersResource.class.getName());

    private static final String LEADERBOARDGROUPSIDENTIFIABLEPATH = "/sailingserver/api/v1/leaderboardgroups/identifiable";
    private static final String LEADERBOARDGROUPSPATH = "/sailingserver/api/v1/leaderboardgroups";
    /**
     * The list of keys that are compared during a compare run.
     */
    private static final String[] KEYLISTTOCOMPARE = new String[] {"id", "description", "events", "leaderboards", "displayName",
            "isMetaLeaderboard", "isRegattaLeaderboard", "scoringComment", "lastScoringUpdate", "scoringScheme",
            "regattaName", "series", "isMedalSeries", "fleets", "color", "ordering", "races", "isMedalRace",
            "isTracked", "regattaName", "trackedRaceName", "hasGpsData", "hasWindData"};
    private static final Set<String> KEYSETTOCOMPARE = new HashSet<>(Arrays.asList(KEYLISTTOCOMPARE));
    /**
     * The list of keys that are not compared.
     */
    private static final String[] KEYSTOIGNORE = new String[] { "timepoint", "raceViewerUrls" };
    private static final Set<String> KEYSETTOIGNORE = new HashSet<>(Arrays.asList(KEYSTOIGNORE));
    /**
     * The list of keys that get always printed. "name" needs a special treatment, as it should be printed, but also
     * during a compare run there is no need to compare entries with different values for "name".
     */
    private static final String[] KEYSTOPRINT = new String[] { "id" };
    private static final Set<String> KEYSETTOPRINT = new HashSet<>(Arrays.asList(KEYSTOPRINT));

    public CompareServersResource() {
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response compareServers(@QueryParam("server1") String server1, @QueryParam("server2") String server2) {
        final Map<String, Set<Object>> result = new HashMap<>();
        Response response = null;
        if (!Util.hasLength(server1) || !Util.hasLength(server2)) {
            response = badRequest();
        } else {
            result.put(server1, new HashSet<>());
            result.put(server2, new HashSet<>());
            try {
                final URL base1 = RemoteServerUtil.createBaseUrl(server1);
                final URL base2 = RemoteServerUtil.createBaseUrl(server2);
                final URLConnection lgc1 = HttpUrlConnectionHelper.redirectConnection(
                        RemoteServerUtil.createRemoteServerUrl(base1, LEADERBOARDGROUPSIDENTIFIABLEPATH, null));
                final URLConnection lgc2 = HttpUrlConnectionHelper.redirectConnection(
                        RemoteServerUtil.createRemoteServerUrl(base2, LEADERBOARDGROUPSIDENTIFIABLEPATH, null));
                final JSONParser parser = new JSONParser();
                final JSONArray leaderboardgroupList1 = (JSONArray) parser
                        .parse(new InputStreamReader(lgc1.getInputStream(), "UTF-8"));
                final JSONArray leaderboardgroupList2 = (JSONArray) parser
                        .parse(new InputStreamReader(lgc2.getInputStream(), "UTF-8"));
                for (Object lg1 : leaderboardgroupList1) {
                    try {
                        if (!leaderboardgroupList2.contains(lg1)) {
                            result.get(server1).add(lg1);
                        } else {
                            final String lgId = ((JSONObject) lg1).get("id").toString();
                            final URLConnection lgdetailc1 = HttpUrlConnectionHelper.redirectConnection(
                                    RemoteServerUtil.createRemoteServerUrl(base1, createLgDetailPath(lgId), null));
                            final URLConnection lgdetailc2 = HttpUrlConnectionHelper.redirectConnection(
                                    RemoteServerUtil.createRemoteServerUrl(base2, createLgDetailPath(lgId), null));
                            Object lgdetail1 = JSONValue
                                    .parse(new InputStreamReader(lgdetailc1.getInputStream(), "UTF-8"));
                            Object lgdetail2 = JSONValue
                                    .parse(new InputStreamReader(lgdetailc2.getInputStream(), "UTF-8"));
                            lgdetail1 = removeUnnecessaryFields(lgdetail1);
                            lgdetail2 = removeUnnecessaryFields(lgdetail2);
                            if (!lgdetail1.equals(lgdetail2)) {
                                Pair<Object, Object> jsonPair = removeDuplicateEntries(lgdetail1, lgdetail2);
                                result.get(server1).add(jsonPair.getA());
                                result.get(server2).add(jsonPair.getB());
                            }
                        }
                    } catch (Exception e) {
                        response = returnInternalServerError(e);
                    }
                }
                for (Object lg2 : leaderboardgroupList2) {
                    if (!leaderboardgroupList1.contains(lg2)) {
                        result.get(server2).add(lg2);
                    }
                }
                JSONObject json = new JSONObject();
                for (Entry<String, Set<Object>> entry : result.entrySet()) {
                    json.put(entry.getKey(), entry.getValue());
                }
                if (result.get(server1).isEmpty() && result.get(server2).isEmpty()) {
                    response = Response.ok(streamingOutput(json)).build();
                }
                else {
                    response = Response.status(Status.CONFLICT).entity(streamingOutput(json)).build();
                }
            } catch (Exception e) {
                response = returnInternalServerError(e);
            }
        }
        return response;
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
     * @param json
     *            org.json.simple.JSONObject
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
    private Pair<Object, Object> removeDuplicateEntries(Object json1, Object json2) {
        Pair<Object, Object> result = new Pair<Object, Object>(null, null);
        if (Util.equalsWithNull(json1, json2)) {
            return result;
        } else if (json1 instanceof JSONObject && json2 instanceof JSONObject) {
            final Iterator<Object> iter1 = ((JSONObject) json1).keySet().iterator();
            while (iter1.hasNext()) {
                Object key = iter1.next();
                if (((JSONObject) json2).containsKey(key)) {
                    Object value1 = ((JSONObject) json1).get(key);
                    Object value2 = ((JSONObject) json2).get(key);
                    if (key.equals("name") && !Util.equalsWithNull(value1, value2)) {
                        break;
                    } else if (KEYSETTOPRINT.contains(key) && Util.equalsWithNull(value1, value2)) {
                        continue;
                    } else if (Util.equalsWithNull(value1, value2) && KEYSETTOCOMPARE.contains(key)) {
                        iter1.remove();
                        ((JSONObject) json2).remove(key);
                    } else {
                        removeDuplicateEntries(value1, value2);
                    }
                }
            }
        } else if (json1 instanceof JSONArray && json2 instanceof JSONArray) {
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
        }
        result = new Pair<Object, Object>(json1, json2);
        return result;
    }

    private Response returnInternalServerError(Throwable e) {
        final Response response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        logger.severe(e.toString());
        return response;
    }

    private Response badRequest() {
        final Response response = Response.status(Status.BAD_REQUEST).build();
        return response;
    }
    
}
