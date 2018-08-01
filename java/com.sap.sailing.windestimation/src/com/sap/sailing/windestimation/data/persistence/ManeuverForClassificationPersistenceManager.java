package com.sap.sailing.windestimation.data.persistence;

import java.net.UnknownHostException;

import com.sap.sailing.server.gateway.deserialization.impl.DetailedBoatClassJsonDeserializer;
import com.sap.sailing.windestimation.data.ManeuverForClassification;
import com.sap.sailing.windestimation.data.deserializer.CompetitorTrackWithEstimationDataJsonDeserializer;
import com.sap.sailing.windestimation.data.deserializer.ManeuverForClassificationJsonDeserializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverForClassificationPersistenceManager
        extends AbstractEstimationDataPersistenceManager<ManeuverForClassification> {

    public ManeuverForClassificationPersistenceManager() throws UnknownHostException {
        super();
    }

    private static final String COLLECTION_NAME = "races_with_maneuvers_for_classification";

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    public CompetitorTrackWithEstimationDataJsonDeserializer<ManeuverForClassification> getNewCompetitorTrackWithEstimationDataJsonDeserializer() {
        ManeuverForClassificationJsonDeserializer maneuverForClassificationJsonDeserializer = new ManeuverForClassificationJsonDeserializer();
        DetailedBoatClassJsonDeserializer boatClassDeserializer = new DetailedBoatClassJsonDeserializer();
        return new CompetitorTrackWithEstimationDataJsonDeserializer<>(boatClassDeserializer,
                maneuverForClassificationJsonDeserializer);
    }

}