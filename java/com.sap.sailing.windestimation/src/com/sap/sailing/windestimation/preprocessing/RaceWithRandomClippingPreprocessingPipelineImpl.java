package com.sap.sailing.windestimation.preprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

public class RaceWithRandomClippingPreprocessingPipelineImpl<T>
        implements PreprocessingPipeline<RaceWithEstimationData<T>, RaceWithEstimationData<T>> {

    private final int minNumberOfCompetitorTrackElementsToPreserve;
    private final int maxNumberOfCompetitorTrackElementsToPreserve;

    public RaceWithRandomClippingPreprocessingPipelineImpl(int minNumberOfCompetitorTrackElementsToPreserve,
            int maxNumberOfCompetitorTrackElementsToPreserve) {
        if (minNumberOfCompetitorTrackElementsToPreserve > maxNumberOfCompetitorTrackElementsToPreserve) {
            throw new IllegalArgumentException("Specified min is smaller than specified max");
        }
        this.minNumberOfCompetitorTrackElementsToPreserve = minNumberOfCompetitorTrackElementsToPreserve;
        this.maxNumberOfCompetitorTrackElementsToPreserve = maxNumberOfCompetitorTrackElementsToPreserve;
    }

    @Override
    public RaceWithEstimationData<T> preprocessRace(RaceWithEstimationData<T> race) {
        Random random = new Random(1234);
        List<CompetitorTrackWithEstimationData<T>> competitorTracks = race.getCompetitorTracks().stream()
                .map(competitorTrack -> {
                    int elementsNumber = competitorTrack.getElements().size();
                    List<T> newElements;
                    if (elementsNumber > minNumberOfCompetitorTrackElementsToPreserve) {
                        int fromIndex = random.nextInt(elementsNumber);
                        boolean rightHalfClipping = random.nextBoolean();
                        if (!rightHalfClipping) {
                            int toIndex = fromIndex + 1;
                            if (toIndex < minNumberOfCompetitorTrackElementsToPreserve) {
                                toIndex = minNumberOfCompetitorTrackElementsToPreserve;
                            }
                            fromIndex = toIndex > maxNumberOfCompetitorTrackElementsToPreserve
                                    ? toIndex - maxNumberOfCompetitorTrackElementsToPreserve : 0;
                            newElements = competitorTrack.getElements().subList(fromIndex, toIndex);
                        } else {
                            if (elementsNumber - fromIndex < minNumberOfCompetitorTrackElementsToPreserve) {
                                fromIndex = elementsNumber - minNumberOfCompetitorTrackElementsToPreserve;
                            }
                            int toIndex = elementsNumber - fromIndex > maxNumberOfCompetitorTrackElementsToPreserve
                                    ? fromIndex + maxNumberOfCompetitorTrackElementsToPreserve : elementsNumber;
                            newElements = competitorTrack.getElements().subList(fromIndex, toIndex);
                        }
                    } else {
                        newElements = new ArrayList<>();
                        newElements.addAll(newElements);
                    }
                    boolean reverseOrder = random.nextBoolean();
                    if (reverseOrder) {
                        Collections.reverse(newElements);
                    }
                    return competitorTrack.constructWithElements(newElements);
                }).collect(Collectors.toList());
        RaceWithEstimationData<T> newRace = race.constructWithElements(competitorTracks);
        return newRace;
    }

}
