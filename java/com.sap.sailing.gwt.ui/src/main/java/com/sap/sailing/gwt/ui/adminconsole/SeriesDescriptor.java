package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

/**
 * Used by {@link SeriesEditDialog} to capture the result of the user's configuration changes,
 * including the race columns that the series shall define, the medal status and the discarding rules, if any.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SeriesDescriptor {
    private final SeriesDTO series;
    private final List<RaceColumnDTO> races;
    private final boolean isMedal;
    private final int[] resultDiscardingThresholds;
    
    public SeriesDescriptor(SeriesDTO series, List<RaceColumnDTO> races, boolean isMedal, int[] resultDiscardingThresholds) {
        this.series = series;
        this.races = races;
        this.isMedal = isMedal;
        this.resultDiscardingThresholds = resultDiscardingThresholds;
    }

    public SeriesDTO getSeries() {
        return series;
    }
    
    public List<RaceColumnDTO> getRaces() {
        return races;
    }

    public boolean isMedal() {
        return isMedal;
    }

    public int[] getResultDiscardingThresholds() {
        return resultDiscardingThresholds;
    }
}
