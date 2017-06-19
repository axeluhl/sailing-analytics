package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.QuickRanksDTOProvider.QuickRanksListener;
import com.sap.sailing.gwt.ui.shared.GPSFixDTOWithSpeedWindTackAndLegType;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;

/**
 * Manages the collection of all {@link CompetitorInfoOverlay} objects shown in the {@link RaceMap}.
 * In particular, it keeps track of the information used for the text in the overlays and allows for
 * incremental updates, e.g., if a competitor's rank has been updated.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompetitorInfoOverlays implements QuickRanksListener {
    private final RaceMap raceMap;
    
    /**
     * html5 canvases used for competitor info display on the map; keys are the competitor IDs as strings
     */
    private final Map<String, CompetitorInfoOverlay> competitorInfoOverlays;
    
    /**
     * keys are the competitor IDs as strings
     */
    private final Map<String, Integer> ranks;
    
    /**
     * keys are the competitor IDs as strings
     */
    private final Map<String, GPSFixDTOWithSpeedWindTackAndLegType> lastPositions;

    private final StringMessages stringMessages;
    
    public CompetitorInfoOverlays(RaceMap raceMap, final StringMessages stringMessages) {
        competitorInfoOverlays = new HashMap<>();
        ranks = new HashMap<>();
        lastPositions = new HashMap<>();
        this.stringMessages = stringMessages;
        this.raceMap = raceMap;
    }
    
    public CompetitorInfoOverlay createCompetitorInfoOverlay(int zIndex, final CompetitorDTO competitorDTO, GPSFixDTOWithSpeedWindTackAndLegType gpsFixDTO, Integer rank, long timeForPositionTransitionMillis) {
        updatePosition(competitorDTO, gpsFixDTO);
        updateRank(competitorDTO, rank);
        CompetitorInfoOverlay result = new CompetitorInfoOverlay(raceMap.getMap(), zIndex,
                raceMap.getCompetitorSelection().getColor(competitorDTO, raceMap.getRaceIdentifier()),
                createInfoText(competitorDTO), raceMap.getCoordinateSystem());
        result.setPosition(gpsFixDTO.position, timeForPositionTransitionMillis);
        competitorInfoOverlays.put(competitorDTO.getIdAsString(), result);
        return result;
    }
    
    /**
     * Removes the {@link CompetitorInfoOverlay} for the competitor specified as {@code competitorDTO} from the map and
     * from this structure.
     */
    public void remove(final CompetitorDTO competitorDTO) {
        remove(competitorDTO.getIdAsString());
    }
    
    public void remove(final String competitorIdAsString) {
        CompetitorInfoOverlay overlay = competitorInfoOverlays.remove(competitorIdAsString);
        if (overlay != null) {
            overlay.removeFromMap();
        }
        ranks.remove(competitorIdAsString);
        lastPositions.remove(competitorIdAsString);
    }
    
    public CompetitorInfoOverlay get(final CompetitorDTO competitorDTO) {
        return competitorInfoOverlays.get(competitorDTO.getIdAsString());
    }
    
    public Iterable<CompetitorInfoOverlay> getCompetitorInfoOverlays() {
        return competitorInfoOverlays.values();
    }
    
    public void updateRank(CompetitorDTO competitorDTO, int rank) {
        CompetitorInfoOverlay overlay = competitorInfoOverlays.get(competitorDTO.getIdAsString());
        if (overlay != null) {
            ranks.put(competitorDTO.getIdAsString(), rank);
            overlay.setInfoText(createInfoText(competitorDTO));
            overlay.draw();
        }
    }
    
    private GPSFixDTOWithSpeedWindTackAndLegType getLastPosition(CompetitorDTO competitorDTO) {
        return lastPositions.get(competitorDTO.getIdAsString());
    }

    public void removeTransitions() {
        for (CompetitorInfoOverlay infoOverlay : getCompetitorInfoOverlays()) {
            infoOverlay.removeCanvasPositionAndRotationTransition();
        }
    }
    
    public void updatePosition(CompetitorDTO competitorDTO, GPSFixDTOWithSpeedWindTackAndLegType gpsFixDTO) {
        lastPositions.put(competitorDTO.getIdAsString(), gpsFixDTO);
    }

    /**
     * Produces a text for the competitor info window for competitor {@code competitorDTO} based on the contents
     * of {@link #ranks} and {@link #lastPositions}.
     */
    private String createInfoText(CompetitorDTO competitorDTO) {
        StringBuilder infoText = new StringBuilder();
        infoText.append(competitorDTO.getSailID()).append("\n");
        infoText.append(NumberFormatterFactory.getDecimalFormat(1).format(getLastPosition(competitorDTO).speedWithBearing.speedInKnots))
                .append(" ").append(stringMessages.knotsUnit()).append("\n");
        final Integer rank = ranks.get(competitorDTO.getIdAsString());
        if (rank != null) {
            infoText.append(stringMessages.rank()).append(" : ").append(rank);
        }
        return infoText.toString();
    }

    public Iterable<String> getCompetitorIdsAsStrings() {
        return Collections.unmodifiableCollection(competitorInfoOverlays.keySet());
    }

    /**
     * Removes all overlays managed by this structure from the map and from this structure
     */
    public void clear() {
        for (final Iterator<Entry<String, CompetitorInfoOverlay>> i=competitorInfoOverlays.entrySet().iterator(); i.hasNext(); ) {
            final Entry<String, CompetitorInfoOverlay> e = i.next();
            e.getValue().removeFromMap();
            i.remove();
        }
        lastPositions.clear();
        ranks.clear();
    }

    @Override
    public void rankChanged(String competitorIdAsString, QuickRankDTO quickRanks) {
        if (competitorInfoOverlays.containsKey(competitorIdAsString)) {
            ranks.put(competitorIdAsString, quickRanks.rank);
            competitorInfoOverlays.get(competitorIdAsString).draw();
        }
    }
}
