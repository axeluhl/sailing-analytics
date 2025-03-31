package com.sap.sailing.gwt.home.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.gwt.home.communication.race.FleetMetadataDTO;
import com.sap.sailing.gwt.home.communication.regatta.RegattaProgressDTO;
import com.sap.sailing.gwt.home.communication.regatta.RegattaProgressFleetDTO;
import com.sap.sailing.gwt.home.communication.regatta.RegattaProgressSeriesDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil.RaceCallback;

/**
 * {@link RaceCallback} implementation, which prepares races information to display the {@link RegattaProgressDTO
 * progress of a regatta} including its {@link RegattaProgressSeriesDTO series} and {@link RegattaProgressFleetDTO
 * fleets}.
 */
public class RegattaProgressCalculator implements EventActionUtil.RaceCallback {

    private Map<String, Map<String, FleetInfo>> infoPerSeriesAndFleet = new LinkedHashMap<>();
    
    @Override
    public void doForRace(RaceContext context) {
        FleetInfo fleetInfo = getFleetInfo(context.getSeriesName(), context.getFleetName());
        if (fleetInfo.metadata == null) fleetInfo.metadata = context.getFleetMetadata();
        fleetInfo.numberOfRaces++;
        if (context.isFinished()) fleetInfo.numberOfFinishedRaces++;
        if (context.isLive()) fleetInfo.numberOfLiveRaces++;
    }
    
    private Map<String, FleetInfo> getSeriesInfo(String seriesName) {
        Map<String, FleetInfo> seriesInfo = infoPerSeriesAndFleet.get(seriesName);
        if (seriesInfo == null) {
            infoPerSeriesAndFleet.put(seriesName, seriesInfo = new HashMap<>());
        }
        return seriesInfo;
    }
    
    private FleetInfo getFleetInfo(String seriesName, String fleetName) {
        Map<String, FleetInfo> seriesInfo = getSeriesInfo(seriesName);
        FleetInfo fleetInfo = seriesInfo.get(fleetName);
        if (fleetInfo == null) {
            seriesInfo.put(fleetName, fleetInfo = new FleetInfo());
        }
        return fleetInfo;
    }
    
    /**
     * @return the {@link RegattaProgressDTO} instance, including the progresses for series and fleets.
     */
    public RegattaProgressDTO getResult() {
        RegattaProgressDTO progress = new RegattaProgressDTO();
        for (Entry<String, Map<String, FleetInfo>> seriesInfo : infoPerSeriesAndFleet.entrySet()) {
            RegattaProgressSeriesDTO series = new RegattaProgressSeriesDTO(seriesInfo.getKey());
            for (FleetInfo fleetInfo : seriesInfo.getValue().values()) {
                series.addFleet(fleetInfo.metadata, fleetInfo.getRegattaProgressFleetDTO());
            }
            progress.addSeries(series);
        }
        return progress;
    }
    
    private class FleetInfo {
        private FleetMetadataDTO metadata;
        private int numberOfRaces = 0;
        private int numberOfFinishedRaces = 0;
        private int numberOfLiveRaces = 0;
        
        private RegattaProgressFleetDTO getRegattaProgressFleetDTO() {
            return new RegattaProgressFleetDTO(numberOfRaces, numberOfFinishedRaces, numberOfLiveRaces);
        }
    }

}
