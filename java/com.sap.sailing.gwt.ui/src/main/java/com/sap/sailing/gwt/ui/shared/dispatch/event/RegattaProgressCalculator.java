package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressFleetDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaProgressSeriesDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;

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
