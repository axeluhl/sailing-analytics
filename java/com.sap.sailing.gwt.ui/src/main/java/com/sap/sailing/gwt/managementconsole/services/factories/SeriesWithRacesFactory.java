package com.sap.sailing.gwt.managementconsole.services.factories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public abstract class SeriesWithRacesFactory {

    private static final String DEFAULT_RACE_NAME = "R";

    public static SeriesDTO createSeriesWithRaces(final String regattaName, final int numberOfRaces, boolean oneAlwaysStaysOne) {
        final SeriesDTO series = new SeriesDTO();
        series.setName(LeaderboardNameConstants.DEFAULT_SERIES_NAME);
        series.setMedal(false);
        series.setStartsWithZeroScore(false);
        series.setSplitFleetContiguousScoring(false);
        series.setFirstColumnIsNonDiscardableCarryForward(false);
        series.setFleets(Collections.singletonList(new FleetDTO(LeaderboardNameConstants.DEFAULT_FLEET_NAME, 0, null)));
        final List<RaceColumnDTO> races = new ArrayList<>();
        for (int i = 1; i <= numberOfRaces; i++) {
            races.add(new RaceColumnInSeriesDTO(DEFAULT_RACE_NAME + i, series.getName(), regattaName, oneAlwaysStaysOne));
        }
        series.setRaceColumns(races);
        return series;
    }

    public static LinkedHashMap<String, SeriesCreationParametersDTO> createSeriesStructure(final SeriesDTO seriesDTO) {
        final LinkedHashMap<String, SeriesCreationParametersDTO> seriesStructure = new LinkedHashMap<>();
        final SeriesCreationParametersDTO seriesPair = new SeriesCreationParametersDTO(seriesDTO.getFleets(),
                seriesDTO.isMedal(), seriesDTO.isFleetsCanRunInParallel(), seriesDTO.isStartsWithZeroScore(),
                seriesDTO.isFirstColumnIsNonDiscardableCarryForward(), seriesDTO.getDiscardThresholds(),
                seriesDTO.hasSplitFleetContiguousScoring(), seriesDTO.hasCrossFleetMergedRanking(),
                seriesDTO.getMaximumNumberOfDiscards(), seriesDTO.isOneAlwaysStaysOne());
        seriesStructure.put(seriesDTO.getName(), seriesPair);
        return seriesStructure;
    }
}
