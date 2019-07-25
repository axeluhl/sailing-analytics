package com.sap.sailing.windestimation.data;

import java.util.List;

/**
 * Race with competitor tracks which has been fetched during data import.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <T>
 *            The type of elements within a competitor track. E.g. maneuver, or gps-fix.
 */
public class RaceWithEstimationData<T> {

    private final String regattaName;
    private final String raceName;
    private final List<CompetitorTrackWithEstimationData<T>> competitorTracks;
    private final WindQuality windQuality;

    public RaceWithEstimationData(String regattaName, String raceName, WindQuality windQuality,
            List<CompetitorTrackWithEstimationData<T>> competitorTracks) {
        this.regattaName = regattaName;
        this.raceName = raceName;
        this.windQuality = windQuality;
        this.competitorTracks = competitorTracks;
    }

    public String getRaceName() {
        return raceName;
    }

    public List<CompetitorTrackWithEstimationData<T>> getCompetitorTracks() {
        return competitorTracks;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public <S> RaceWithEstimationData<S> constructWithElements(List<CompetitorTrackWithEstimationData<S>> elements) {
        RaceWithEstimationData<S> newRace = new RaceWithEstimationData<>(getRegattaName(), getRaceName(),
                getWindQuality(), elements);
        return newRace;
    }

    public WindQuality getWindQuality() {
        return windQuality;
    }

}
