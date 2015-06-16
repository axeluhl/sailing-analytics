package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RacesActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sse.common.Util.Pair;

public class GetRegattasAndLiveRacesForEventAction implements Action<ResultWithTTL<RegattasAndLiveRacesDTO>> {
    private static final Logger logger = Logger.getLogger(GetRegattasAndLiveRacesForEventAction.class.getName());
    
    private UUID eventId;
    
    public GetRegattasAndLiveRacesForEventAction() {
    }

    public GetRegattasAndLiveRacesForEventAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<RegattasAndLiveRacesDTO> execute(DispatchContext context) {
        long start = System.currentTimeMillis();
        final ArrayList<Pair<RegattaMetadataDTO, TreeSet<LiveRaceDTO>>> regattasWithRaces = new ArrayList<>();
        
        RacesActionUtil.forRacesOfEvent(context, eventId, new RaceCallback() {
            @Override
            public void doForRace(RaceContext context) {
                LiveRaceDTO liveRace = context.getLiveRaceOrNull();
                if(liveRace == null) {
                    Pair<RegattaMetadataDTO, TreeSet<LiveRaceDTO>> regatta = ensureRegatta(regattasWithRaces, context);
                    regatta.getB().add(liveRace);
                }
            }
        });
        
        RegattasAndLiveRacesDTO result = new RegattasAndLiveRacesDTO(regattasWithRaces);
        long duration = System.currentTimeMillis() - start;
        logger.log(Level.INFO, "Calculating live races for event "+ eventId + " took: "+ duration + "ms");
        return new ResultWithTTL<RegattasAndLiveRacesDTO>(1000 * 60 * 2, result);
    }

    protected Pair<RegattaMetadataDTO, TreeSet<LiveRaceDTO>> ensureRegatta(ArrayList<Pair<RegattaMetadataDTO, TreeSet<LiveRaceDTO>>> regattasWithRaces,
            RaceContext context) {
        String regattaName = context.getRegattaName();
        for(Pair<RegattaMetadataDTO, TreeSet<LiveRaceDTO>> regatta : regattasWithRaces) {
            if(regatta.getA().getId().equals(regattaName)) {
                return regatta;
            }
        }
        Pair<RegattaMetadataDTO, TreeSet<LiveRaceDTO>> newEntry = new Pair<RegattaMetadataDTO, TreeSet<LiveRaceDTO>>(HomeServiceUtil.toRegattaMetadataDTO(context.getLeaderboardGroup(), context.getLeaderboard()), new TreeSet<LiveRaceDTO>());
        regattasWithRaces.add(newEntry);
        return newEntry;
    }
}
