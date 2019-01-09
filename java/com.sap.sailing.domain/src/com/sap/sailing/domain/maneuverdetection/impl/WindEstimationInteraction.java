package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;

public interface WindEstimationInteraction {

    PossibleChangeOfManeuverTypesInfo newManeuverSpotsDetected(Competitor competitor,
            Iterable<ManeuverSpot> newManeuverSpots);

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

    }

}
