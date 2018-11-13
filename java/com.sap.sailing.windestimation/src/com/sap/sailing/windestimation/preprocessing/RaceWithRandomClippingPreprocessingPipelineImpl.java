package com.sap.sailing.windestimation.preprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

public class RaceWithRandomClippingPreprocessingPipelineImpl<FromElements, ToElements>
        implements RacePreprocessingPipeline<FromElements, ToElements> {

    private final int minNumberOfCompetitorTrackElementsToPreserve;
    private final int maxNumberOfCompetitorTrackElementsToPreserve;
    private final RacePreprocessingPipeline<FromElements, ToElements> preprocessingPipeline;

    public RaceWithRandomClippingPreprocessingPipelineImpl(
            RacePreprocessingPipeline<FromElements, ToElements> preprocessingPipeline,
            int minNumberOfCompetitorTrackElementsToPreserve, int maxNumberOfCompetitorTrackElementsToPreserve) {
        this.preprocessingPipeline = preprocessingPipeline;
        if (minNumberOfCompetitorTrackElementsToPreserve > maxNumberOfCompetitorTrackElementsToPreserve) {
            throw new IllegalArgumentException("Specified min is smaller than specified max");
        }
        this.minNumberOfCompetitorTrackElementsToPreserve = minNumberOfCompetitorTrackElementsToPreserve;
        this.maxNumberOfCompetitorTrackElementsToPreserve = maxNumberOfCompetitorTrackElementsToPreserve;
    }

    @Override
    public RaceWithEstimationData<ToElements> preprocessRace(RaceWithEstimationData<FromElements> race) {
        RaceWithEstimationData<ToElements> preprocessedRace = preprocessingPipeline.preprocessRace(race);
        Random random = new Random(1234);
        List<CompetitorTrackWithEstimationData<ToElements>> competitorTracks = preprocessedRace.getCompetitorTracks()
                .stream().map(competitorTrack -> {
                    int elementsNumber = competitorTrack.getElements().size();
                    List<ToElements> newElements;
                    if (elementsNumber > minNumberOfCompetitorTrackElementsToPreserve) {
                        int fromIndex = random.nextInt(elementsNumber);
                        boolean rightHalfClipping = random.nextBoolean();
                        if (!rightHalfClipping) {
                            int toIndex = fromIndex + 1;
                            if (toIndex < minNumberOfCompetitorTrackElementsToPreserve) {
                                toIndex = minNumberOfCompetitorTrackElementsToPreserve;
                            }
                            fromIndex = toIndex > maxNumberOfCompetitorTrackElementsToPreserve
                                    ? toIndex - maxNumberOfCompetitorTrackElementsToPreserve
                                    : 0;
                            newElements = competitorTrack.getElements().subList(fromIndex, toIndex);
                        } else {
                            if (elementsNumber - fromIndex < minNumberOfCompetitorTrackElementsToPreserve) {
                                fromIndex = elementsNumber - minNumberOfCompetitorTrackElementsToPreserve;
                            }
                            int toIndex = elementsNumber - fromIndex > maxNumberOfCompetitorTrackElementsToPreserve
                                    ? fromIndex + maxNumberOfCompetitorTrackElementsToPreserve
                                    : elementsNumber;
                            newElements = competitorTrack.getElements().subList(fromIndex, toIndex);
                        }
                    } else {
                        newElements = new ArrayList<>();
                        newElements.addAll(competitorTrack.getElements());
                    }
                    boolean reverseOrder = random.nextBoolean();
                    if (reverseOrder) {
                        Collections.reverse(newElements);
                    }
                    return competitorTrack.constructWithElements(newElements);
                }).collect(Collectors.toList());
        RaceWithEstimationData<ToElements> newRace = race.constructWithElements(competitorTracks);
        return newRace;
    }

}
