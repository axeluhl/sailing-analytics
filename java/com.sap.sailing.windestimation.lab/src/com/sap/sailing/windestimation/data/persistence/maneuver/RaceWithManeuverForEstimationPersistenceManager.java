package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.deserializer.CompetitorTrackWithEstimationDataJsonDeserializer;
import com.sap.sailing.windestimation.data.deserializer.ManeuverForEstimationJsonDeserializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class RaceWithManeuverForEstimationPersistenceManager
        extends AbstractRaceWithEstimationDataPersistenceManager<ManeuverForEstimation> {

    public RaceWithManeuverForEstimationPersistenceManager() throws UnknownHostException {
    }

    @Override
    public String getCollectionName() {
        return "racesWithManeuversForEstimation";
    }

    @Override
    public CompetitorTrackWithEstimationDataJsonDeserializer<ManeuverForEstimation> getNewCompetitorTrackWithEstimationDataJsonDeserializer() {
        ManeuverForEstimationJsonDeserializer maneuverForEstimationJsonDeserializer = new ManeuverForEstimationJsonDeserializer();
        BoatClassJsonDeserializer boatClassDeserializer = new BoatClassJsonDeserializer(DomainFactory.INSTANCE);
        return new CompetitorTrackWithEstimationDataJsonDeserializer<>(boatClassDeserializer,
                maneuverForEstimationJsonDeserializer);
    }

}