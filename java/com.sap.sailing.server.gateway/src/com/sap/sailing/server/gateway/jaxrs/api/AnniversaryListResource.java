package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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

import com.sap.sailing.domain.anniversary.AnniversaryConflictResolver;
import com.sap.sailing.domain.anniversary.AnniversaryRaceInfo;
import com.sap.sailing.domain.anniversary.SimpleAnniversaryRaceInfo;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.impl.AnniversaryRaceInfoJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.SimpleAnniversaryRaceInfoJsonSerializer;

@Path("/v1/anniversary")
public class AnniversaryListResource extends AbstractSailingServerResource {
    private static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON_UTF8 = MediaType.APPLICATION_JSON + ";charset=UTF-8";

    private final AnniversaryConflictResolver resolver = new AnniversaryConflictResolver();
    private final SimpleAnniversaryRaceInfoJsonSerializer simpleAnniversaryRaceListJsonSerializer = new SimpleAnniversaryRaceInfoJsonSerializer();
    private final AnniversaryRaceInfoJsonSerializer anniversaryRaceListJsonSerializer = new AnniversaryRaceInfoJsonSerializer();

    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("details")
    public Response getDetailsForRace(@QueryParam("raceName") String raceName,
            @QueryParam("regattaName") String regattaName) {

        AnniversaryRaceInfo match = null;
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
                                    AnniversaryRaceInfo newMatch = new AnniversaryRaceInfo(raceIdentifier,
                                            leaderboard.getName(), trackedRace.getStartOfRace().asDate(),
                                            event.getId());
                                    if (match == null) {
                                        match = newMatch;
                                    } else {
                                        match = resolver.resolve(match, newMatch);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (match == null) {
            return Response.status(Status.NOT_FOUND).header(HEADER_NAME_CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8).build();
        }
        JSONObject serialized = anniversaryRaceListJsonSerializer.serialize(match);
        return getJsonResponse(serialized);
    }

    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("races")
    public Response getStatisticsByYear() {
        JSONArray json = new JSONArray();

        HashMap<RegattaAndRaceIdentifier, SimpleAnniversaryRaceInfo> anniversaryRaceList = new HashMap<RegattaAndRaceIdentifier, SimpleAnniversaryRaceInfo>();
        for (Event event : getService().getAllEvents()) {
            for (LeaderboardGroup group : event.getLeaderboardGroups()) {
                for (Leaderboard leaderboard : group.getLeaderboards()) {
                    for (RaceColumn race : leaderboard.getRaceColumns()) {
                        for (Fleet fleet : race.getFleets()) {
                            TrackedRace trackedRace = race.getTrackedRace(fleet);
                            if (trackedRace != null) {
                                RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
                                SimpleAnniversaryRaceInfo raceInfo = new SimpleAnniversaryRaceInfo(raceIdentifier,
                                        trackedRace.getStartOfRace().asDate());
                                anniversaryRaceList.put(raceInfo.getIdentifier(), raceInfo);
                            }
                        }
                    }
                }
            }
        }

        for (SimpleAnniversaryRaceInfo entry : anniversaryRaceList.values()) {
            json.add(simpleAnniversaryRaceListJsonSerializer.serialize(entry));

        }
        return getJsonResponse(json);
    }

    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("fullRacelist")
    public Response fullRaceList() {
        JSONArray json = new JSONArray();
        HashMap<RegattaAndRaceIdentifier, SimpleAnniversaryRaceInfo> store = new HashMap<>();
        getService().getRemoteRaceList(store);

        for (Event event : getService().getAllEvents()) {
            for (LeaderboardGroup group : event.getLeaderboardGroups()) {
                for (Leaderboard leaderboard : group.getLeaderboards()) {
                    for (RaceColumn race : leaderboard.getRaceColumns()) {
                        for (Fleet fleet : race.getFleets()) {
                            TrackedRace trackedRace = race.getTrackedRace(fleet);
                            if (trackedRace != null) {
                                RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
                                SimpleAnniversaryRaceInfo raceInfo = new SimpleAnniversaryRaceInfo(raceIdentifier,
                                        trackedRace.getStartOfRace().asDate());
                                store.put(raceInfo.getIdentifier(), raceInfo);
                            }
                        }
                    }
                }
            }
        }

        ArrayList<SimpleAnniversaryRaceInfo> sorted = new ArrayList<>(store.values());
        Collections.sort(sorted, new Comparator<SimpleAnniversaryRaceInfo>() {
            @Override
            public int compare(SimpleAnniversaryRaceInfo o1, SimpleAnniversaryRaceInfo o2) {
                return o1.getStartOfRace().compareTo(o2.getStartOfRace());
            }
        });
        for (int i = 0; i < sorted.size(); i++) {
            SimpleAnniversaryRaceInfo current = sorted.get(i);
            JSONArray single = new JSONArray();
            single.add(String.valueOf(i));
            single.add(simpleAnniversaryRaceListJsonSerializer.serialize(current));
            json.add(single);

        }
        return getJsonResponse(json);
    }

    private Response getJsonResponse(JSONAware json) {
        return Response.ok(json.toJSONString()).header(HEADER_NAME_CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8).build();
    }
}
