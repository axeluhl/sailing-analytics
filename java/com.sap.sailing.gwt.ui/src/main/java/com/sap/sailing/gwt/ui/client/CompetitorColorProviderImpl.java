package com.sap.sailing.gwt.ui.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.common.ColorMap;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.impl.ColorMapImpl;
import com.sap.sse.common.Color;

public class CompetitorColorProviderImpl implements CompetitorColorProvider {
    private final ColorMap<CompetitorDTO> competitorsColorMap;
    private final Map<RegattaAndRaceIdentifier, Map<CompetitorDTO, Color>> competitorsBoatColorsPerRace;
    
    public CompetitorColorProviderImpl() {
        this(null, Collections.<CompetitorDTO, BoatDTO> emptyMap());
    }

    public CompetitorColorProviderImpl(RegattaAndRaceIdentifier raceIdentifier, Map<CompetitorDTO, BoatDTO> competitorsAndTheirBoats) {
        this.competitorsColorMap = new ColorMapImpl<CompetitorDTO>();
        this.competitorsBoatColorsPerRace = new HashMap<RegattaAndRaceIdentifier, Map<CompetitorDTO, Color>>();

        if(raceIdentifier != null) {
            for (Entry<CompetitorDTO, BoatDTO> competitorAndBoat : competitorsAndTheirBoats.entrySet()) {
                if (competitorAndBoat.getValue() != null) {
                    Map<CompetitorDTO, Color> raceColors = competitorsBoatColorsPerRace.get(raceIdentifier);
                    if(raceColors == null) {
                        raceColors = new HashMap<CompetitorDTO, Color>();
                        competitorsBoatColorsPerRace.put(raceIdentifier, raceColors);
                    }
                    raceColors.put(competitorAndBoat.getKey(), competitorAndBoat.getValue().getColor());
                }
            }
        }
    }

    @Override
    public Color getColor(CompetitorDTO competitor) {
        return getColor(competitor, null);
    }

    @Override
    public Color getColor(CompetitorDTO competitor, RegattaAndRaceIdentifier raceIdentfier) {
        Color result = null;
        if (raceIdentfier != null) {
            Map<CompetitorDTO, Color> raceColors = competitorsBoatColorsPerRace.get(raceIdentfier);
            if (raceColors != null) {
                result = raceColors.get(competitor);
            }
        }
        if (result == null && competitor.getColor() != null) {
            result = competitor.getColor();
        }
        // fallback
        if (result == null) {
            result = competitorsColorMap.getColorByID(competitor);
        }
        return result;
    }

    @Override
    public void addBlockedColor(Color color) {
        competitorsColorMap.addBlockedColor(color);
    }

    @Override
    public void removeBlockedColor(Color color) {
        competitorsColorMap.removeBlockedColor(color);
    }
}
