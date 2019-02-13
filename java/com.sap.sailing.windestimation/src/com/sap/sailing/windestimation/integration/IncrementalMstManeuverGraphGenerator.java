package com.sap.sailing.windestimation.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.maneuverdetection.TrackTimeInfo;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.windestimation.aggregator.msthmm.MstGraphNodeTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraphGenerator;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

/**
 * Incremental
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class IncrementalMstManeuverGraphGenerator extends MstManeuverGraphGenerator {

    protected final Map<Competitor, ManeuverDataOfCompetitor> maneuverDataPerCompetitor = new HashMap<>();
    private final ManeuverClassifiersCache maneuverClassifiersCache;
    private final CompleteManeuverCurveToManeuverForEstimationConverter maneuverConverter;

    public IncrementalMstManeuverGraphGenerator(CompleteManeuverCurveToManeuverForEstimationConverter maneuverConverter,
            MstGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator,
            ManeuverClassifiersCache maneuverClassifiersCache) {
        super(transitionProbabilitiesCalculator);
        this.maneuverConverter = maneuverConverter;
        this.maneuverClassifiersCache = maneuverClassifiersCache;
    }

    public void add(Competitor competitor, CompleteManeuverCurve newManeuver, TrackTimeInfo trackTimeInfo) {
        ManeuverDataOfCompetitor maneuverData = maneuverDataPerCompetitor.get(competitor);
        if (maneuverData == null) {
            maneuverData = new ManeuverDataOfCompetitor();
            maneuverDataPerCompetitor.put(competitor, maneuverData);
        }
        CompleteManeuverCurve latestNonTemporaryAcceptedManeuver = maneuverData.getLatestNonTemporaryAcceptedManeuver();
        // ignore maneuvers older than the last non-temporary maneuver
        if (latestNonTemporaryAcceptedManeuver == null
                || newManeuver.getTimePoint().after(latestNonTemporaryAcceptedManeuver.getTimePoint())) {
            CompleteManeuverCurve latestTemporaryAcceptedManeuver = maneuverData.getLatestTemporaryAcceptedManeuver();
            CompleteManeuverCurve previousManeuver;
            CompleteManeuverCurve nextManeuver;
            if (latestTemporaryAcceptedManeuver != null
                    && isLocationOfManeuversNearlySame(latestTemporaryAcceptedManeuver, newManeuver)) {
                // replace temporary accepted maneuver
                previousManeuver = maneuverData.getPreviousManeuver(latestTemporaryAcceptedManeuver);
                nextManeuver = null;
            } else if (latestTemporaryAcceptedManeuver != null) {
                // ignore maneuvers before latest temporary accepted maneuver
                if (latestTemporaryAcceptedManeuver.getTimePoint().before(newManeuver.getTimePoint())) {
                    previousManeuver = latestTemporaryAcceptedManeuver;
                    nextManeuver = null;
                    // check if latestTemporaryAcceptedManeuver is cleaning considering the newManeuver as next maneuver
                    ManeuverForEstimation temporaryAcceptedManeuverForEstimation = maneuverConverter
                            .convertCleanManeuverSpotToManeuverForEstimation(latestTemporaryAcceptedManeuver,
                                    maneuverData.getPreviousManeuver(latestTemporaryAcceptedManeuver), newManeuver,
                                    competitor, trackTimeInfo);
                    if (temporaryAcceptedManeuverForEstimation.isClean()) {
                        maneuverData.getNonTemporaryAcceptedManeuverClassificationsToAdd()
                                .add(maneuverData.getLatestTemporaryAcceptedManeuverClassification());
                        maneuverData.setLatestNonTemporaryAcceptedManeuver(latestTemporaryAcceptedManeuver);
                        for (Iterator<CompleteManeuverCurve> iterator = maneuverData
                                .getAllManeuversAfterLatestNonTemporaryAcceptedManeuver().iterator(); iterator
                                        .hasNext();) {
                            CompleteManeuverCurve maneuver = iterator.next();
                            if (!maneuver.getTimePoint().after(latestTemporaryAcceptedManeuver.getTimePoint())) {
                                iterator.remove();
                            }
                        }
                    }
                } else {
                    return;
                }
            } else {
                previousManeuver = maneuverData.getPreviousManeuver(newManeuver);
                nextManeuver = null;
            }
            ManeuverForEstimation newManeuverForEstimation = maneuverConverter
                    .convertCleanManeuverSpotToManeuverForEstimation(newManeuver, previousManeuver, nextManeuver,
                            competitor, trackTimeInfo);
            if (newManeuverForEstimation == null || !newManeuverForEstimation.isClean()) {
                maneuverData.setLatestTemporaryAcceptedManeuver(null);
                maneuverData.setLatestTemporaryAcceptedManeuverClassification(null);
            } else {
                maneuverData.setLatestTemporaryAcceptedManeuver(newManeuver);
                ManeuverWithProbabilisticTypeClassification newManeuverClassification = maneuverClassifiersCache
                        .classifyInstance(newManeuverForEstimation);
                maneuverData.setLatestTemporaryAcceptedManeuverClassification(newManeuverClassification);
            }
            maneuverData.getAllManeuversAfterLatestNonTemporaryAcceptedManeuver().add(newManeuver);
        }
    }

    private boolean isLocationOfManeuversNearlySame(CompleteManeuverCurve maneuver1, CompleteManeuverCurve maneuver2) {
        if (maneuver1.getTimePoint().equals(maneuver2.getTimePoint())) {
            return true;
        }
        return false;
    }

    @Override
    public MstManeuverGraphComponents parseGraph() {
        List<ManeuverWithProbabilisticTypeClassification> nonTemporaryNodes = new ArrayList<>();
        List<ManeuverWithProbabilisticTypeClassification> temporaryNodes = new ArrayList<>();
        for (ManeuverDataOfCompetitor maneuverData : maneuverDataPerCompetitor.values()) {
            nonTemporaryNodes.addAll(maneuverData.getNonTemporaryAcceptedManeuverClassificationsToAdd());
            ManeuverWithProbabilisticTypeClassification temporaryNode = maneuverData
                    .getLatestTemporaryAcceptedManeuverClassification();
            if (temporaryNode != null) {
                temporaryNodes.add(temporaryNode);
            }
            maneuverData.getNonTemporaryAcceptedManeuverClassificationsToAdd().clear();
        }
        Collections.sort(nonTemporaryNodes);
        for (ManeuverWithProbabilisticTypeClassification node : nonTemporaryNodes) {
            addNode(node);
        }
        if (temporaryNodes.isEmpty()) {
            return super.parseGraph();
        }
        MstManeuverGraphGenerator clonedMstGenerator = clone();
        Collections.sort(temporaryNodes);
        for (ManeuverWithProbabilisticTypeClassification node : temporaryNodes) {
            clonedMstGenerator.addNode(node);
        }
        return clonedMstGenerator.parseGraph();
    }

    public CompleteManeuverCurveToManeuverForEstimationConverter getManeuverConverter() {
        return maneuverConverter;
    }

    protected static class ManeuverDataOfCompetitor {
        private CompleteManeuverCurve latestNonTemporaryAcceptedManeuver = null;
        private CompleteManeuverCurve latestTemporaryAcceptedManeuver = null;
        private ManeuverWithProbabilisticTypeClassification latestTemporaryAcceptedManeuverClassification = null;
        private final List<ManeuverWithProbabilisticTypeClassification> nonTemporaryAcceptedManeuverClassificationsToAdd = new ArrayList<>();
        private final SortedSet<CompleteManeuverCurve> allManeuversAfterLatestNonTemporaryAcceptedManeuver = new TreeSet<>(
                new Comparator<CompleteManeuverCurve>() {
                    @Override
                    public int compare(CompleteManeuverCurve o1, CompleteManeuverCurve o2) {
                        return o1.getTimePoint().compareTo(o2.getTimePoint());
                    }
                });

        public CompleteManeuverCurve getLatestNonTemporaryAcceptedManeuver() {
            return latestNonTemporaryAcceptedManeuver;
        }

        public void setLatestNonTemporaryAcceptedManeuver(CompleteManeuverCurve latestNonTemporaryAcceptedManeuver) {
            this.latestNonTemporaryAcceptedManeuver = latestNonTemporaryAcceptedManeuver;
        }

        public CompleteManeuverCurve getLatestTemporaryAcceptedManeuver() {
            return latestTemporaryAcceptedManeuver;
        }

        public void setLatestTemporaryAcceptedManeuver(CompleteManeuverCurve latestTemporaryAcceptedManeuver) {
            this.latestTemporaryAcceptedManeuver = latestTemporaryAcceptedManeuver;
        }

        public ManeuverWithProbabilisticTypeClassification getLatestTemporaryAcceptedManeuverClassification() {
            return latestTemporaryAcceptedManeuverClassification;
        }

        public void setLatestTemporaryAcceptedManeuverClassification(
                ManeuverWithProbabilisticTypeClassification latestTemporaryAcceptedManeuverClassification) {
            this.latestTemporaryAcceptedManeuverClassification = latestTemporaryAcceptedManeuverClassification;
        }

        public List<ManeuverWithProbabilisticTypeClassification> getNonTemporaryAcceptedManeuverClassificationsToAdd() {
            return nonTemporaryAcceptedManeuverClassificationsToAdd;
        }

        public SortedSet<CompleteManeuverCurve> getAllManeuversAfterLatestNonTemporaryAcceptedManeuver() {
            return allManeuversAfterLatestNonTemporaryAcceptedManeuver;
        }

        public CompleteManeuverCurve getPreviousManeuver(CompleteManeuverCurve maneuver) {
            CompleteManeuverCurve previousManeuver = getLatestNonTemporaryAcceptedManeuver();
            for (CompleteManeuverCurve nextManeuver : getAllManeuversAfterLatestNonTemporaryAcceptedManeuver()) {
                if (!nextManeuver.getTimePoint().before(maneuver.getTimePoint())) {
                    return previousManeuver;
                }
                previousManeuver = nextManeuver;
            }
            return previousManeuver;
        }
    }

}
