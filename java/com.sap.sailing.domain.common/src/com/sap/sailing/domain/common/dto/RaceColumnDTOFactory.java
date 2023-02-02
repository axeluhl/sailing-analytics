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
     * @param oneAlwaysStaysOne
     *            When scores in this column are scaled by some factor, either based on the {@link #getExplicitFactor()
     *            explicit factor} set for this column, or implicitly, e.g., because the {@link ScoringScheme} mandates
     *            the doubling of medal race scores and this column {@link #isMedalRace() represents a medal race}, then
     *            some configurations still want the 1.0 score still to be 1.0. For example, with a column factor of 2.0
     *            scores 1, 2, 3 would end up as 1, 3, 5; or with a column factor of 3.0 scores 1, 2, 3 would end up as
     *            1, 4, 7. This method tells whether this column shall apply such a scheme.
     */
    public RaceColumnDTO createRaceColumnDTO(String columnName, boolean isMedal, Double explicitFactor,
            String regattaName, String seriesName, boolean isMetaLeaderboardColumn, boolean oneAlwaysStaysOne) {
        final RaceColumnDTO raceColumnDTO;
        if (seriesName != null) {
            raceColumnDTO = new RaceColumnInSeriesDTO(columnName, seriesName, regattaName, oneAlwaysStaysOne);
        } else {
            raceColumnDTO = isMetaLeaderboardColumn ? new MetaLeaderboardRaceColumnDTO(columnName) : new RaceColumnDTO(columnName, oneAlwaysStaysOne);
        }
        fillRaceColumnDTO(raceColumnDTO, isMedal, explicitFactor);
        return raceColumnDTO;
    }
    
    private void fillRaceColumnDTO(RaceColumnDTO raceColumnDTO, boolean isMedal, Double explicitFactor) {
        raceColumnDTO.setMedalRace(isMedal);
        raceColumnDTO.setExplicitFactor(explicitFactor);
    }
}
