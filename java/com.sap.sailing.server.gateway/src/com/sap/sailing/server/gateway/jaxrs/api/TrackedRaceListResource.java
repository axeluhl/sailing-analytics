package com.sap.sailing.server.gateway.jaxrs.api;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
     * information about an anniversary. This call works transitively by asking a server that is known to have the race
     * in question in case that the race isn't found locally.
     */
    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("raceDetails")
    public Response getDetailsForRace(@QueryParam("raceName") String raceName,
            @QueryParam("regattaName") String regattaName) {
        final DetailedRaceInfo detailedRaceInfo = getService().getFullDetailsForRaceLocal(new RegattaNameAndRaceName(regattaName, raceName));
        if (detailedRaceInfo == null) {
            return Response.status(Status.NOT_FOUND).header(HEADER_NAME_CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8).build();
        }
        return getJsonResponse(detailedRaceListJsonSerializer.serialize(detailedRaceInfo));
    }

    /**
     * Returns a list of tracked races. By default, only TrackedRaces from the local instance are returned. The entries
     * are grouped by the remote URL from where they originated. Local entries have a {@code null} value for the
     * {@link DetailedRaceInfoJsonSerializer# FIELD_REMOTEURL remote URL} field. The order of the list returned is
     * undefined.<br>
     */
    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("getRaces")
    public Response raceList(@QueryParam("transitive") Boolean transitive) {
        final boolean includeRemotes = transitive != null && Boolean.TRUE.equals(transitive);
        final Map<RegattaAndRaceIdentifier, SimpleRaceInfo> distinctRaces = getDistinctRaces(includeRemotes);

        final HashMap<String, List<SimpleRaceInfo>> raceData = new HashMap<>();
        distinctRaces.values().forEach(raceInfo -> {
            final String remoteUrl = raceInfo.getRemoteUrl() == null ? null : raceInfo.getRemoteUrl().toExternalForm();
            List<SimpleRaceInfo> remoteList = raceData.get(remoteUrl);
            if (remoteList == null) {
                raceData.put(remoteUrl, remoteList = new ArrayList<>());
            }
            remoteList.add(raceInfo);
        });

        final JSONArray json = new JSONArray();
        for (Entry<String, List<SimpleRaceInfo>> raced : raceData.entrySet()) {
            JSONArray list = new JSONArray();
            for (SimpleRaceInfo simpleRaceInfo : raced.getValue()) {
                list.add(simpleRaceListJsonSerializer.serialize(simpleRaceInfo));
            }
            final JSONObject remote = new JSONObject();
            remote.put(DetailedRaceInfoJsonSerializer.FIELD_REMOTEURL, raced.getKey());
            remote.put(DetailedRaceInfoJsonSerializer.FIELD_RACES, list);
            json.add(remote);
        }
        return getJsonResponse(json);
    }

    /**
     * Returns a list of all locally and remote tracked races that are currently known. The list is sorted by the
     * {@link SimpleRaceInfoJsonSerializer#FIELD_START_OF_RACE} field, and each {@link SimpleRaceInfo} object is put
     * together with an incrementing number starting at 0. Duplicate races are eliminated such that local copies take
     * precedence over remote ones. Races hosted on the server on which this method is invoked will be grouped under the
     * {@code null} value for the {@code remoteUrl} field.
     */
    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("allRaces")
    public Response fullRaceList() {
        JSONArray json = new JSONArray();
        Map<RegattaAndRaceIdentifier, SimpleRaceInfo> store = getDistinctRaces(/* include remotes */ true);
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
            final URL remoteUrl = current.getRemoteUrl();
            raceInfo.put("remoteUrl", remoteUrl == null ? null : remoteUrl.toExternalForm());
            raceInfo.put("raceinfo", simpleRaceListJsonSerializer.serialize(current));
            json.add(raceInfo);
        }
        return getJsonResponse(json);
    }

    private Map<RegattaAndRaceIdentifier, SimpleRaceInfo> getDistinctRaces(boolean includeRemotes) {
        final Map<RegattaAndRaceIdentifier, SimpleRaceInfo> distinctRaces = new HashMap<>();
        if (includeRemotes) {
            distinctRaces.putAll(getService().getRemoteRaceList());
        }
        distinctRaces.putAll(getService().getLocalRaceList());
        return distinctRaces;
    }

    private Response getJsonResponse(JSONAware json) {
        return Response.ok(json.toJSONString()).header(HEADER_NAME_CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8).build();
    }
}
