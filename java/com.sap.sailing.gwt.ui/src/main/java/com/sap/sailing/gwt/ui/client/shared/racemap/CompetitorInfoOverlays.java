package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.QuickRanksDTOProvider.QuickRanksListener;
import com.sap.sailing.gwt.ui.shared.GPSFixDTOWithSpeedWindTackAndLegType;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;

/**
 * Manages the collection of all {@link CompetitorInfoOverlay} objects shown in the {@link RaceMap}. In particular, it
 * keeps track of the information used for the text in the overlays and allows for incremental updates, e.g., if a
 * competitor's rank has been updated. It can act as a {@link QuickRanksListener} and as such be subscribed for rank
 * changes, e.g., with a {@link QuickRanksDTOProvider}. It will track all rank updates for any competitor whose info
 * overlay is managed by this instance.
 * <p>
 * 
 * Competitor positions are expected to be explicitly announced by calling
 * {@link #updatePosition(CompetitorWithBoatDTO, GPSFixDTOWithSpeedWindTackAndLegType, long)} in case new position data is
 * known.
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
    
    public CompetitorInfoOverlay createCompetitorInfoOverlay(int zIndex, final CompetitorDTO competitorDTO,
            GPSFixDTOWithSpeedWindTackAndLegType gpsFixDTO, Integer rank, long timeForPositionTransitionMillis) {
        CompetitorInfoOverlay result = new CompetitorInfoOverlay(raceMap.getMap(), zIndex,
                raceMap.getCompetitorSelection().getColor(competitorDTO, raceMap.getRaceIdentifier()),
                /* info text */ "", raceMap.getCoordinateSystem());
        competitorInfoOverlays.put(competitorDTO.getIdAsString(), result);
        updatePosition(competitorDTO, gpsFixDTO, timeForPositionTransitionMillis);
        result.setInfoText(createInfoText(competitorDTO));
        updateRank(competitorDTO, rank);
        return result;
    }
    
    /**
     * Removes the {@link CompetitorInfoOverlay} for the competitor specified as {@code competitorDTO} from the map and
     * from this structure.
     */
    public void remove(final CompetitorWithBoatDTO competitorDTO) {
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
    
    /**
     * Updates {@link #ranks} and re-draws the corresponding overlay
     */
    public void updateRank(CompetitorDTO competitorDTO, Integer rank) {
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
    
    /**
     * Updates the competitor's position data and and, if the competitor has an overlay managed by this instance,
     * adjusts the overlay's position on the map using the transition timeout specified by
     * {@code timeForPositionTransitionMillis}.
     */
    public void updatePosition(CompetitorDTO competitorDTO, GPSFixDTOWithSpeedWindTackAndLegType gpsFixDTO, long timeForPositionTransitionMillis) {
        lastPositions.put(competitorDTO.getIdAsString(), gpsFixDTO);
        CompetitorInfoOverlay overlay = competitorInfoOverlays.get(competitorDTO.getIdAsString());
        if (overlay != null) {
            overlay.setPosition(gpsFixDTO.position, timeForPositionTransitionMillis);
            overlay.draw();
        }
    }

    /**
     * Produces a text for the competitor info window for competitor {@code competitorDTO} based on the contents
     * of {@link #ranks} and {@link #lastPositions}. If no {@link #ranks} entry exists, no rank is shown. However,
     * a {@link #lastPosition} is required in order to produce the speed/course information.
     */
    private String createInfoText(CompetitorDTO competitorDTO) {
        StringBuilder infoText = new StringBuilder();
        infoText.append(competitorDTO.getShortInfo()).append("\n");
        infoText.append(NumberFormatterFactory.getDecimalFormat(1).format(getLastPosition(competitorDTO).speedWithBearing.speedInKnots))
                .append(" ").append(stringMessages.knotsUnit()).append("\n");
        final Integer rank = ranks.get(competitorDTO.getIdAsString());
        if (rank != null && rank != 0) {
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

    /**
     * @param quickRanks
     *            uses the {@link QuickRankDTO#oneBasedRank} and {@link QuickRankDTO#competitor} fields
     */
    @Override
    public void rankChanged(String competitorIdAsString, QuickRankDTO oldQuickRank, QuickRankDTO quickRanks) {
        final CompetitorInfoOverlay competitorInfoOverlay = competitorInfoOverlays.get(competitorIdAsString);
        ranks.put(competitorIdAsString, quickRanks.oneBasedRank);
        if (competitorInfoOverlay != null) {
            competitorInfoOverlay.setInfoText(createInfoText(quickRanks.competitor));
            competitorInfoOverlay.draw();
        }
    }
}
