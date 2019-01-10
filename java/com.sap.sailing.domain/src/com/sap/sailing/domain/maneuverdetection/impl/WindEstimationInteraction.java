package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.WindSource;

public interface WindEstimationInteraction {

    PossibleChangeOfManeuverTypesInfo newManeuverSpotsDetected(Competitor competitor,
            Iterable<ManeuverSpot> newManeuverSpots);
    
    WindSource getWindSource();

    public static class PossibleChangeOfManeuverTypesInfo {
        private final Map<Competitor, List<ManeuverSpotWithTypedManeuvers>> maneuverSpotsWithChangedWindPerCompetitor;

        public PossibleChangeOfManeuverTypesInfo(
                Map<Competitor, List<ManeuverSpotWithTypedManeuvers>> maneuverSpotsWithChangedWindPerCompetitor) {
            this.maneuverSpotsWithChangedWindPerCompetitor = maneuverSpotsWithChangedWindPerCompetitor;
        }

        public boolean isWindChanged() {
            return !maneuverSpotsWithChangedWindPerCompetitor.isEmpty();
        }

        public boolean isWindChangedForCompetitor(Competitor competitor) {
            return maneuverSpotsWithChangedWindPerCompetitor.containsKey(competitor);
        }

        public List<ManeuverSpotWithTypedManeuvers> getManeuverSpotsWithChangedWindForCompetitor(
                Competitor competitor) {
            return maneuverSpotsWithChangedWindPerCompetitor.get(competitor);
        }

        public List<ManeuverSpotWithTypedManeuvers> getManeuverSpotsWithChangedWindOfAllCompetitors() {
            return maneuverSpotsWithChangedWindPerCompetitor.values().stream()
                    .flatMap(maneuverSpotsOfCompetitor -> maneuverSpotsOfCompetitor.stream())
                    .collect(Collectors.toList());
        }

    }

}
