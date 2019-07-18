package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.windestimation.data.LabeledManeuverForEstimation;
import com.sap.sailing.windestimation.data.serialization.CompetitorTrackWithEstimationDataJsonDeserializer;
import com.sap.sailing.windestimation.data.serialization.LabeledManeuverForEstimationJsonDeserializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class RaceWithManeuverForEstimationPersistenceManager
        extends AbstractRaceWithEstimationDataPersistenceManager<LabeledManeuverForEstimation> {

    public static final String COLLECTION_NAME = "racesWithManeuversForEstimation";

    public RaceWithManeuverForEstimationPersistenceManager() throws UnknownHostException {
    }

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    public CompetitorTrackWithEstimationDataJsonDeserializer<LabeledManeuverForEstimation> getNewCompetitorTrackWithEstimationDataJsonDeserializer() {
        LabeledManeuverForEstimationJsonDeserializer maneuverForEstimationJsonDeserializer = new LabeledManeuverForEstimationJsonDeserializer();
        BoatClassJsonDeserializer boatClassDeserializer = new BoatClassJsonDeserializer(DomainFactory.INSTANCE);
        return new CompetitorTrackWithEstimationDataJsonDeserializer<>(boatClassDeserializer,
                maneuverForEstimationJsonDeserializer);
    }

}