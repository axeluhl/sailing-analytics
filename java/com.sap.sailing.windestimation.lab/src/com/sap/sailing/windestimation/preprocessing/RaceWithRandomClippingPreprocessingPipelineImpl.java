package com.sap.sailing.windestimation.preprocessing;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

public class RaceWithRandomClippingPreprocessingPipelineImpl<FromElements, ToElements>
        implements RacePreprocessingPipeline<FromElements, ToElements> {

    private final int minNumberOfCompetitorTrackElementsToPreserve;
    private final int maxNumberOfCompetitorTrackElementsToPreserve;
    private final RacePreprocessingPipeline<FromElements, ToElements> preprocessingPipeline;
    private final boolean evaluationPerCompetitorTrack;

    public RaceWithRandomClippingPreprocessingPipelineImpl(
            RacePreprocessingPipeline<FromElements, ToElements> preprocessingPipeline,
            int minNumberOfCompetitorTrackElementsToPreserve, int maxNumberOfCompetitorTrackElementsToPreserve,
            boolean evaluationPerCompetitorTrack) {
        this.preprocessingPipeline = preprocessingPipeline;
        this.evaluationPerCompetitorTrack = evaluationPerCompetitorTrack;
        if (minNumberOfCompetitorTrackElementsToPreserve > maxNumberOfCompetitorTrackElementsToPreserve) {
            throw new IllegalArgumentException("Specified min is smaller than specified max");
        }
        this.minNumberOfCompetitorTrackElementsToPreserve = minNumberOfCompetitorTrackElementsToPreserve;
        this.maxNumberOfCompetitorTrackElementsToPreserve = maxNumberOfCompetitorTrackElementsToPreserve;
    }

    @Override
    public RaceWithEstimationData<ToElements> preprocessInput(RaceWithEstimationData<FromElements> race) {
        RaceWithEstimationData<ToElements> preprocessedRace = preprocessingPipeline.preprocessInput(race);
        RaceWithEstimationData<ToElements> newRace;
        if (evaluationPerCompetitorTrack) {
            newRace = getRaceWithRandomClippingPerCompetitorTrack(preprocessedRace);
        } else {
            newRace = getRaceWithRandomClippingConsideringAllCompetitorTracks(preprocessedRace);
        }
        return newRace;
    }

    private RaceWithEstimationData<ToElements> getRaceWithRandomClippingConsideringAllCompetitorTracks(
            RaceWithEstimationData<ToElements> preprocessedRace) {
        List<ToElements> allElements = preprocessedRace.getCompetitorTracks().stream()
                .flatMap(competitorTrack -> competitorTrack.getElements().stream()).sorted()
                .collect(Collectors.toList());
        List<ToElements> clippedElements = getRandomClippingOfElements(allElements);
        Map<CompetitorTrackWithEstimationData<ToElements>, List<ToElements>> clippedElementsPerCompetitorTrack = new HashMap<>();
        for (ToElements clippedElement : clippedElements) {
            CompetitorTrackWithEstimationData<ToElements> correspondingCompetitorTrack = preprocessedRace
                    .getCompetitorTracks().stream()
                    .filter(competitorTrack -> competitorTrack.getElements().contains(clippedElement)).findAny().get();
            List<ToElements> clippedElementsOfCompetitorTrack = clippedElementsPerCompetitorTrack
                    .get(correspondingCompetitorTrack);
            if (clippedElementsOfCompetitorTrack == null) {
                clippedElementsOfCompetitorTrack = new ArrayList<>();
                clippedElementsPerCompetitorTrack.put(correspondingCompetitorTrack, clippedElementsOfCompetitorTrack);
            }
            clippedElementsOfCompetitorTrack.add(clippedElement);
        }
        List<CompetitorTrackWithEstimationData<ToElements>> finalCompetitorTracks = new ArrayList<>();
        for (Entry<CompetitorTrackWithEstimationData<ToElements>, List<ToElements>> entry : clippedElementsPerCompetitorTrack
                .entrySet()) {
            CompetitorTrackWithEstimationData<ToElements> finalCompetitorTrack = entry.getKey()
                    .constructWithElements(entry.getValue());
            finalCompetitorTracks.add(finalCompetitorTrack);
        }
        RaceWithEstimationData<ToElements> newRace = preprocessedRace.constructWithElements(finalCompetitorTracks);
        return newRace;
    }

    private RaceWithEstimationData<ToElements> getRaceWithRandomClippingPerCompetitorTrack(
            RaceWithEstimationData<ToElements> preprocessedRace) {
        List<CompetitorTrackWithEstimationData<ToElements>> competitorTracks = preprocessedRace.getCompetitorTracks()
                .stream().map(competitorTrack -> {
                    List<ToElements> elementsToProcess = competitorTrack.getElements();
                    List<ToElements> newElements = getRandomClippingOfElements(elementsToProcess);
                    return competitorTrack.constructWithElements(newElements);
                }).collect(Collectors.toList());
        RaceWithEstimationData<ToElements> newRace = preprocessedRace.constructWithElements(competitorTracks);
        return newRace;
    }

    private List<ToElements> getRandomClippingOfElements(List<ToElements> elementsToProcess) {
        int elementsNumber = elementsToProcess.size();
        SecureRandom random = new SecureRandom();
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
                newElements = elementsToProcess.subList(fromIndex, toIndex);
            } else {
                if (elementsNumber - fromIndex < minNumberOfCompetitorTrackElementsToPreserve) {
                    fromIndex = elementsNumber - minNumberOfCompetitorTrackElementsToPreserve;
                }
                int toIndex = elementsNumber - fromIndex > maxNumberOfCompetitorTrackElementsToPreserve
                        ? fromIndex + maxNumberOfCompetitorTrackElementsToPreserve
                        : elementsNumber;
                newElements = elementsToProcess.subList(fromIndex, toIndex);
            }
        } else {
            newElements = new ArrayList<>();
            newElements.addAll(elementsToProcess);
        }
        boolean reverseOrder = random.nextBoolean();
        if (reverseOrder) {
            Collections.reverse(newElements);
        }
        return newElements;
    }

}
