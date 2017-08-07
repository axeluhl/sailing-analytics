package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;

import com.sap.sailing.domain.anniversary.SimpleRaceInfo;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.impl.SimpleRaceInfoJsonSerializer;

@Path("/v1/trackedRaces")
public class TrackedRaceListResource extends AbstractSailingServerResource {
    private static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON_UTF8 = MediaType.APPLICATION_JSON + ";charset=UTF-8";

    private final SimpleRaceInfoJsonSerializer simpleRaceListJsonSerializer = new SimpleRaceInfoJsonSerializer();

    /**
     * Returns a list of all locally tracked races, the list is not sorted
     */
    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("localRaces")
    public Response raceList() {
        JSONArray json = new JSONArray();
        for (SimpleRaceInfo entry : getService().getLocalRaceList().values()) {
            json.add(simpleRaceListJsonSerializer.serialize(entry));
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
            JSONArray single = new JSONArray();
            single.add(String.valueOf(i));
            single.add(simpleRaceListJsonSerializer.serialize(current));
            json.add(single);

        }
        return getJsonResponse(json);
    }

    private Response getJsonResponse(JSONAware json) {
        return Response.ok(json.toJSONString()).header(HEADER_NAME_CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8).build();
    }
}
