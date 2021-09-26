package com.sap.sailing.domain.common.dto;

public class RaceColumnDTOFactory {
    public static final RaceColumnDTOFactory INSTANCE = new RaceColumnDTOFactory();

    /**
     * @param regattaName
     *            must not be <code>null</code> if <code>seriesName</code> is not <code>null</code>
     * @param seriesName
     *            when <code>null</code>, a regular {@link RaceColumnDTO} or {@link MetaLeaderboardRaceColumnDTO} will
     *            be produced (depending on <code>isMetaLeaderboardColumn</code>) and initialized from the properties
     *            passed; otherwise, a {@link RaceColumnInSeriesDTO} will be created.
     */
    public RaceColumnDTO createRaceColumnDTO(String columnName, boolean isMedal, Double explicitFactor,
            String regattaName, String seriesName, boolean isMetaLeaderboardColumn) {
        final RaceColumnDTO raceColumnDTO;
        if (seriesName != null) {
            raceColumnDTO = new RaceColumnInSeriesDTO(columnName, seriesName, regattaName);
        } else {
            raceColumnDTO = isMetaLeaderboardColumn ? new MetaLeaderboardRaceColumnDTO(columnName) : new RaceColumnDTO(columnName);
        }
        fillRaceColumnDTO(raceColumnDTO, isMedal, explicitFactor);
        return raceColumnDTO;
    }
    
    private void fillRaceColumnDTO(RaceColumnDTO raceColumnDTO, boolean isMedal, Double explicitFactor) {
        raceColumnDTO.setMedalRace(isMedal);
        raceColumnDTO.setExplicitFactor(explicitFactor);
    }
}
