package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

/**
 * Used by {@link RaceColumnInRegattaSeriesDialog} to capture the result of the user's configuration changes,
 * including the race columns that the series shall define, the medal status and the discarding rules, if any.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RegattaSeriesDescriptor {
    private final SeriesDTO series;
    private final List<RaceColumnDTO> races;
    
    public RegattaSeriesDescriptor(SeriesDTO series, List<RaceColumnDTO> races) {
        this.series = series;
        this.races = races;
    }

    public SeriesDTO getSeries() {
        return series;
    }
    
    public List<RaceColumnDTO> getRaces() {
        return races;
    }
}
