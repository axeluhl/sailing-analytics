package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.util.RemoteServerUtil;
import com.sap.sse.util.HttpUrlConnectionHelper;

@Path("/v1/compareservers")
public class CompareServersResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(CompareServersResource.class.getName());
    
    private static final String LEADERBOARDGROUPSPATH = "/sailingserver/api/v1/leaderboardgroups";
    private static final String[] KEYLIST = new String[] { "id", "description", "events", "leaderboards", "displayName",
            "isMetaLeaderboard", "isRegattaLeaderboard", "scoringComment", "lastScoringUpdate", "scoringScheme",
            "regattaName", "series", "isMedalSeries", "fleets", "color", "ordering", "races", "isMedalRace",
            "isTracked", "regattaName", "trackedRaceName", "hasGpsData", "hasWindData" };
    private static final Comparator<JSONObject> JSONOBJECTCOMPARATOR = new Comparator<JSONObject>() {
        @Override
        public int compare(JSONObject o1, JSONObject o2) {
            return o1.toString().compareTo(o2.toString());
        }
    };
    
    public CompareServersResource() {
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json;charset=UTF-8")
    public Response compareServers(@FormParam("server1") String server1, @FormParam("server2") String server2) {
        final Map<String, TreeSet<JSONObject>> result = new HashMap<>();
        Response response = null;
        if (!Util.hasLength(server1) || !Util.hasLength(server2)) {
            response = badRequest();
        } else {
            result.put(server1, new TreeSet<>(JSONOBJECTCOMPARATOR));
            result.put(server2, new TreeSet<>(JSONOBJECTCOMPARATOR));
            try {
                final URL base1 = RemoteServerUtil.createBaseUrl(server1);
                final URL base2 = RemoteServerUtil.createBaseUrl(server2);
                final URLConnection lgc1 = HttpUrlConnectionHelper
                        .redirectConnection(RemoteServerUtil.createRemoteServerUrl(base1, LEADERBOARDGROUPSPATH, null));
                final URLConnection lgc2 = HttpUrlConnectionHelper
                        .redirectConnection(RemoteServerUtil.createRemoteServerUrl(base2, LEADERBOARDGROUPSPATH, null));
                final JSONParser parser = new JSONParser();
                final JSONArray leaderboardgroupList1 = (JSONArray) parser
                        .parse(new InputStreamReader(lgc1.getInputStream(), "UTF-8"));
                final JSONArray leaderboardgroupList2 = (JSONArray) parser
                        .parse(new InputStreamReader(lgc2.getInputStream(), "UTF-8"));
                leaderboardgroupList1.sort(null);
                leaderboardgroupList2.sort(null);
                final StringBuilder lgdetailpath = new StringBuilder(LEADERBOARDGROUPSPATH);
                lgdetailpath.append("/");
                final int length = lgdetailpath.length();
                if (Util.equals(leaderboardgroupList1, leaderboardgroupList2)) {
                    for (Object lg1 : leaderboardgroupList1) {
                        URI uri = new URI(null, null, lg1.toString(), null, null);
                        lgdetailpath.replace(length, lgdetailpath.length(), uri.toASCIIString());
                        if (lg1.equals(leaderboardgroupList2.get(leaderboardgroupList1.indexOf(lg1)))) {
                            final URLConnection lgdetailc1 = HttpUrlConnectionHelper.redirectConnection(
                                    RemoteServerUtil.createRemoteServerUrl(base1, lgdetailpath.toString(), null));
                            final URLConnection lgdetailc2 = HttpUrlConnectionHelper.redirectConnection(
                                    RemoteServerUtil.createRemoteServerUrl(base2, lgdetailpath.toString(), null));
                            JSONObject lgdetail1 = (JSONObject) parser
                                    .parse(new InputStreamReader(lgdetailc1.getInputStream(), "UTF-8"));
                            JSONObject lgdetail2 = (JSONObject) parser
                                    .parse(new InputStreamReader(lgdetailc2.getInputStream(), "UTF-8"));
                            lgdetail1 = removeUnnecessaryFields(lgdetail1);
                            lgdetail2 = removeUnnecessaryFields(lgdetail2);
                            if (!lgdetail1.equals(lgdetail2)) {
                                Pair<JSONObject, JSONObject> jsonPair = removeDuplicatesFromJsonAndReturn(lgdetail1,
                                        lgdetail2);
                                result.get(server1).add(jsonPair.getA());
                                result.get(server2).add(jsonPair.getB());
                            }
                        }
                    }
                } else {
                    for (Object lg1 : leaderboardgroupList1) {
                        final JSONObject json1 = new JSONObject();
                        if (!leaderboardgroupList2.contains(lg1)) {
                            json1.put("name", lg1.toString());
                            result.get(server1).add(json1);
                        }
                    }
                    for (Object lg2 : leaderboardgroupList2) {
                        final JSONObject json2 = new JSONObject();
                        if (!leaderboardgroupList1.contains(lg2)) {
                            json2.put("name", lg2.toString());
                            result.get(server2).add(json2);
                        }
                    }
                }
            } catch (Exception e) {
                response = returnInternalServerError(e);
            }
            JSONObject json = new JSONObject();
            for (Entry<String, TreeSet<JSONObject>> entry : result.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
            response = Response.ok(json.toJSONString()).build();
        }
        return response;
    }
    
    private JSONObject removeUnnecessaryFields(JSONObject json) {
        JSONArray lbdetailsArray = (JSONArray) json.get("leaderboards");
        for (Object lb : lbdetailsArray) {
           JSONObject lbjson = (JSONObject) lb;
           JSONArray seriesArray = (JSONArray) lbjson.get("series");
           for (Object series : seriesArray) {
               JSONObject seriesjson = (JSONObject) series;
               JSONArray fleetsArray = (JSONArray) seriesjson.get("fleets");
               for (Object fleet : fleetsArray) {
                   JSONObject fleetjson = (JSONObject) fleet;
                   JSONArray racesArray = (JSONArray) fleetjson.get("races");
                   for (Object race : racesArray) {
                       JSONObject racejson = (JSONObject) race;
                       racejson.remove("raceViewerUrls");
                   }
               }
           }
        }
        json.remove("timepoint");
        return json;
    }

    private Pair<JSONObject, JSONObject> removeDuplicatesFromJsonAndReturn(JSONObject jsonObject1,
            JSONObject jsonObject2) {
        JSONObject json1 = jsonObject1;
        JSONObject json2 = jsonObject2;
        for (String key : KEYLIST) {
            if (Util.equalsWithNull(json1.get(key), json2.get(key))) {
                json1.remove(key);
                json2.remove(key);
            } else if (json1.get(key) == null || json2.get(key) == null) {
                break;
            } else {
                JSONArray keyArray1 = (JSONArray) json1.get(key);
                JSONArray keyArray2 = (JSONArray) json2.get(key);
                Iterator<Object> iter1 = keyArray1.iterator();
                while (iter1.hasNext()) {
                    JSONObject key1json = (JSONObject) iter1.next();
                    if (keyArray2.contains(key1json)) {
                        keyArray2.remove(key1json);
                        iter1.remove();
                    } else {
                        for (Object key2 : keyArray2) {
                            JSONObject key2json = (JSONObject) key2;
                            removeDuplicatesFromJsonAndReturn(key1json, key2json);
                        }
                    }
                }
            }
        }
        return new Pair<JSONObject, JSONObject>(json1, json2);
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
