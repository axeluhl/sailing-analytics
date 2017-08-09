package com.sap.sailing.server.gateway.jaxrs.api;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.anniversary.SimpleRaceInfo;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.impl.DetailedRaceInfoJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.SimpleRaceInfoJsonSerializer;

@Path("/v1/trackedRaces")
public class TrackedRaceListResource extends AbstractSailingServerResource {
    private static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON_UTF8 = MediaType.APPLICATION_JSON + ";charset=UTF-8";

    private final SimpleRaceInfoJsonSerializer simpleRaceListJsonSerializer = new SimpleRaceInfoJsonSerializer();
    private final DetailedRaceInfoJsonSerializer detailedRaceListJsonSerializer = new DetailedRaceInfoJsonSerializer();

    /**
     * Allows to query for more details on a specific race, implemented to allow for example to retrieve more
     * information about an anniversary. Can be implemented to work transient with further remote servers, this
     * implementation does not, but has the same api as a future transient implementation.
     */
    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("raceDetails")
    public Response getDetailsForRace(@QueryParam("raceName") String raceName,
            @QueryParam("regattaName") String regattaName) {
        HashSet<DetailedRaceInfo> detailedRaces = new HashSet<>();
        detailedRaces.addAll(getService().getFullDetailsForRace(new RegattaNameAndRaceName(regattaName, raceName)));
        if (detailedRaces.isEmpty()) {
            return Response.status(Status.NOT_FOUND).header(HEADER_NAME_CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8).build();
        }
        JSONArray result = new JSONArray();
        for (DetailedRaceInfo detailedRace : detailedRaces) {
            result.add(detailedRaceListJsonSerializer.serialize(detailedRace));
        }
        return getJsonResponse(result);
    }

    /**
     * Returns a list of all locally tracked races, the list is not sorted
     */
    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("getRaces")
    public Response raceList() {
        HashMap<URL, List<SimpleRaceInfo>> raceData = new HashMap<>();
        // replace with transitive call later!
        for (SimpleRaceInfo entry : getService().getLocalRaceList().values()) {
            URL remoteUrl = entry.getRemoteUrl();
            List<SimpleRaceInfo> remoteList = raceData.get(remoteUrl);
            if (remoteList == null) {
                remoteList = new ArrayList<>();
                raceData.put(remoteUrl, remoteList);
            }
            remoteList.add(entry);
        }
        JSONArray json = new JSONArray();
        for (Entry<URL, List<SimpleRaceInfo>> raced : raceData.entrySet()) {
            JSONArray list = new JSONArray();
            for (SimpleRaceInfo simpleRaceInfo : raced.getValue()) {
                list.add(simpleRaceListJsonSerializer.serialize(simpleRaceInfo));
            }
            JSONObject remote = new JSONObject();
            if(raced.getKey() == null){
                remote.put(DetailedRaceInfoJsonSerializer.FIELD_REMOTEURL, "");
            }else{
                remote.put(DetailedRaceInfoJsonSerializer.FIELD_REMOTEURL, raced.getKey().toExternalForm());
            }
            remote.put(DetailedRaceInfoJsonSerializer.FIELD_RACES, list);
            json.add(remote);
        }
        return getJsonResponse(json);
    }

    /**
     * Returns a list of all locally and remote tracked races that are currently known, The list is sorted by Startdate,
     * and each SimpleRaceInfo object is put together with an incrementing number starting at 0
     */
    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("allRaces")
    public Response fullRaceList() {
        JSONArray json = new JSONArray();
        Map<RegattaAndRaceIdentifier, SimpleRaceInfo> store = new HashMap<>();
        store.putAll(getService().getRemoteRaceList());
        store.putAll(getService().getLocalRaceList());

        ArrayList<SimpleRaceInfo> sorted = new ArrayList<>(store.values());
        Collections.sort(sorted, new Comparator<SimpleRaceInfo>() {
            @Override
            public int compare(SimpleRaceInfo o1, SimpleRaceInfo o2) {
                return o1.getStartOfRace().compareTo(o2.getStartOfRace());
            }
        });
        for (int i = 0; i < sorted.size(); i++) {
            SimpleRaceInfo current = sorted.get(i);
            JSONObject raceInfo = new JSONObject();
            raceInfo.put("racenumber", String.valueOf(i));
            raceInfo.put("remoteUrl", current.getRemoteUrl());
            raceInfo.put("raceinfo", simpleRaceListJsonSerializer.serialize(current));
            json.add(raceInfo);
        }
        return getJsonResponse(json);
    }

    private Response getJsonResponse(JSONAware json) {
        return Response.ok(json.toJSONString()).header(HEADER_NAME_CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8).build();
    }
}
