package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.ColorMap;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.impl.ColorMapImpl;
import com.sap.sse.common.Color;

public class CompetitorColorProviderImpl implements CompetitorColorProvider {
    private final ColorMap<CompetitorDTO> competitorsColorMap;
    
    public CompetitorColorProviderImpl() {
        this.competitorsColorMap = new ColorMapImpl<CompetitorDTO>();
    }

    @Override
    public Color getColor(CompetitorDTO competitor) {
        final Color result;
        if (competitor.getColor() != null) {
            result = competitor.getColor();
        } else {
            result = competitorsColorMap.getColorByID(competitor); 
        }
        return result;
    }

    @Override
    public Color getColor(CompetitorDTO competitor, RaceColumnDTO raceColumn) {
        return null;
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
