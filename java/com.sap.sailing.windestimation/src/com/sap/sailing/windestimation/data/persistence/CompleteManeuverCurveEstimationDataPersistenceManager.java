package com.sap.sailing.windestimation.data.persistence;

import java.net.UnknownHostException;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.server.gateway.deserialization.impl.CompleteManeuverCurveWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DetailedBoatClassJsonDeserializer;
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
public class CompleteManeuverCurveEstimationDataPersistenceManager
        extends AbstractEstimationDataPersistenceManager<CompleteManeuverCurveWithEstimationData> {

    public CompleteManeuverCurveEstimationDataPersistenceManager() throws UnknownHostException {
        super();
    }

    private static final String COLLECTION_NAME = "races_with_complete_maneuvers";

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    public CompetitorTrackWithEstimationDataJsonDeserializer<CompleteManeuverCurveWithEstimationData> getNewCompetitorTrackWithEstimationDataJsonDeserializer() {
        CompleteManeuverCurveWithEstimationDataJsonDeserializer completeManeuverCurveDeserializer = new CompleteManeuverCurveWithEstimationDataJsonDeserializer(
                new ManeuverMainCurveWithEstimationDataJsonDeserializer(),
                new ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonDeserializer(),
                new ManeuverWindJsonDeserializer(), new PositionJsonDeserializer());
        DetailedBoatClassJsonDeserializer boatClassDeserializer = new DetailedBoatClassJsonDeserializer();
        return new CompetitorTrackWithEstimationDataJsonDeserializer<>(boatClassDeserializer,
                completeManeuverCurveDeserializer);
    }

}