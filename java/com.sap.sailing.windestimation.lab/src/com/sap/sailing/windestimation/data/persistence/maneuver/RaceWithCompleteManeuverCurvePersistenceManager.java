package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CompleteManeuverCurveWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ManeuverMainCurveWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ManeuverWindJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.PositionJsonDeserializer;
import com.sap.sailing.windestimation.data.deserializer.CompetitorTrackWithEstimationDataJsonDeserializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class RaceWithCompleteManeuverCurvePersistenceManager
        extends AbstractRaceWithEstimationDataPersistenceManager<CompleteManeuverCurveWithEstimationData> {

    public RaceWithCompleteManeuverCurvePersistenceManager() throws UnknownHostException {
    }

    @Override
    public String getCollectionName() {
        return "racesWithCompleteManeuvers";
    }

    @Override
    public CompetitorTrackWithEstimationDataJsonDeserializer<CompleteManeuverCurveWithEstimationData> getNewCompetitorTrackWithEstimationDataJsonDeserializer() {
        CompleteManeuverCurveWithEstimationDataJsonDeserializer completeManeuverCurveDeserializer = new CompleteManeuverCurveWithEstimationDataJsonDeserializer(
                new ManeuverMainCurveWithEstimationDataJsonDeserializer(),
                new ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonDeserializer(),
                new ManeuverWindJsonDeserializer(), new PositionJsonDeserializer());
        BoatClassJsonDeserializer boatClassDeserializer = new BoatClassJsonDeserializer(DomainFactory.INSTANCE);
        return new CompetitorTrackWithEstimationDataJsonDeserializer<>(boatClassDeserializer,
                completeManeuverCurveDeserializer);
    }

}