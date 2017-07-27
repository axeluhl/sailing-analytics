package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.impl.AnniversaryConflictResolver;
import com.sap.sailing.server.gateway.serialization.impl.AnniversaryRaceInfo;
import com.sap.sailing.server.gateway.serialization.impl.AnniversaryRaceInfoJsonSerializer;

@Path("/v1/anniversary")
public class AnniversaryListResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(AnniversaryListResource.class.getName());
    private static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON_UTF8 = MediaType.APPLICATION_JSON + ";charset=UTF-8";

    private static final AnniversaryConflictResolver resolver = new AnniversaryConflictResolver();
    
    private final AnniversaryRaceInfoJsonSerializer anniversaryRaceListJsonSerializer = new AnniversaryRaceInfoJsonSerializer();

    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("races")
    public Response getStatisticsByYear() {
        JSONArray json = new JSONArray();

        HashMap<RegattaAndRaceIdentifier, AnniversaryRaceInfo> anniversaryRaceList = new HashMap<RegattaAndRaceIdentifier, AnniversaryRaceInfo>();
        for (Event event : getService().getAllEvents()) {
            for (LeaderboardGroup group : event.getLeaderboardGroups()) {
                for (Leaderboard leaderboard : group.getLeaderboards()) {
                    for (RaceColumn race : leaderboard.getRaceColumns()) {
                        for (Fleet fleet : race.getFleets()) {
                            TrackedRace trackedRace = race.getTrackedRace(fleet);
                            if (trackedRace != null) {
                                RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
                                AnniversaryRaceInfo current = anniversaryRaceList.get(raceIdentifier);
                                AnniversaryRaceInfo raceInfo = new AnniversaryRaceInfo(raceIdentifier,
                                        leaderboard.getName(), trackedRace.getStartOfRace().asDate(),
                                        event.getId().toString(),null);
                                if(current == null){
                                    anniversaryRaceList.put(raceIdentifier, raceInfo);
                                }else{
                                    AnniversaryRaceInfo prefered = resolver.resolve(current,raceInfo);
                                    if(prefered != current){
                                        anniversaryRaceList.put(prefered.getIdentifier(), prefered);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (AnniversaryRaceInfo entry : anniversaryRaceList.values()) {
            json.add(anniversaryRaceListJsonSerializer.serialize(entry));

        }
        return getJsonResponse(json);
    }

    private Response getJsonResponse(JSONAware json) {
        return Response.ok(json.toJSONString()).header(HEADER_NAME_CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8).build();
    }
}
