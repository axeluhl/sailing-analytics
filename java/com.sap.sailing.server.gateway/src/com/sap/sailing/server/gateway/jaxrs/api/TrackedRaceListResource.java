package com.sap.sailing.server.gateway.jaxrs.api;

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
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
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
    @Path("details")
    public Response getDetailsForRace(@QueryParam("raceName") String raceName,
            @QueryParam("regattaName") String regattaName) {
        HashSet<DetailedRaceInfo> detailedRaces = new HashSet<>();
        for (Event event : getService().getAllEvents()) {
            for (LeaderboardGroup group : event.getLeaderboardGroups()) {
                for (Leaderboard leaderboard : group.getLeaderboards()) {
                    for (RaceColumn race : leaderboard.getRaceColumns()) {
                        for (Fleet fleet : race.getFleets()) {
                            TrackedRace trackedRace = race.getTrackedRace(fleet);
                            if (trackedRace != null) {
                                RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
                                if (raceIdentifier.getRaceName().equals(raceName)
                                        && raceIdentifier.getRegattaName().equals(regattaName)) {
                                    // remoteurl is "" as the own url is only known by the caller, and the call is not
                                    // resolved transiently on other servers yet
                                    DetailedRaceInfo newMatch = new DetailedRaceInfo(raceIdentifier,
                                            leaderboard.getName(), trackedRace.getStartOfRace().asDate(), event.getId(),
                                            "");
                                    detailedRaces.add(newMatch);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (detailedRaces.isEmpty()) {
            return Response.status(Status.NOT_FOUND).header(HEADER_NAME_CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8).build();
        }
        JSONArray result = new JSONArray();
        for(DetailedRaceInfo detailedRace:detailedRaces){
            result.add(detailedRaceListJsonSerializer.serialize(detailedRace));
        }
        return getJsonResponse(result);
    }

    /**
     * If a conflict occurs between two ReggataAndRace identifies, due to being reachable via multiple
     * Events/Leaderboardgroups, prefer the one determined with a leaderboard of the same name
     */
    public DetailedRaceInfo resolve(DetailedRaceInfo current, DetailedRaceInfo contender) {
        if (current.getEventID().equals(contender.getEventID())
                && current.getLeaderboardName().equals(contender.getLeaderboardName())) {
            // same for relevant values, it does not matter
            return current;
        }
        if (current.getLeaderboardName().equals(contender.getIdentifier().getRaceName())) {
            System.out.println("Resolved conflict " + current + " " + contender + " with current");
            return current;
        }
        System.out.println("Resolved conflict " + current + " " + contender + " with contender");
        return contender;
    }

    /**
     * Returns a list of all locally tracked races, the list is not sorted
     */
    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("getRaces")
    public Response raceList() {
        HashMap<String, List<SimpleRaceInfo>> raceData = new HashMap<>();
        // replace with transitive call later!
        for (SimpleRaceInfo entry : getService().getLocalRaceList().values()) {
            String remoteUrl = entry.getRemoteUrl();
            List<SimpleRaceInfo> remoteList = raceData.get(remoteUrl);
            if (remoteList == null) {
                remoteList = new ArrayList<>();
                raceData.put(remoteUrl, remoteList);
            }
            remoteList.add(entry);
        }
        JSONObject json = new JSONObject();
        for (Entry<String, List<SimpleRaceInfo>> raced : raceData.entrySet()) {
            JSONArray list = new JSONArray();
            for (SimpleRaceInfo simpleRaceInfo : raced.getValue()) {
                list.add(simpleRaceListJsonSerializer.serialize(simpleRaceInfo));
            }
            json.put(raced.getKey(), list);
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
            single.add(current.getRemoteUrl());
            single.add(simpleRaceListJsonSerializer.serialize(current));
            json.add(single);

        }
        return getJsonResponse(json);
    }

    private Response getJsonResponse(JSONAware json) {
        return Response.ok(json.toJSONString()).header(HEADER_NAME_CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8).build();
    }
}
