package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorFactory;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.impl.RGBColor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;

public class CompetitorJsonDeserializer implements JsonDeserializer<Competitor> {
    protected final CompetitorFactory competitorStore;
    protected final JsonDeserializer<DynamicTeam> teamJsonDeserializer;
    protected final JsonDeserializer<DynamicBoat> boatJsonDeserializer;

    public static CompetitorJsonDeserializer create(SharedDomainFactory baseDomainFactory) {
        return new CompetitorJsonDeserializer(baseDomainFactory, new TeamJsonDeserializer(new PersonJsonDeserializer(
                new NationalityJsonDeserializer(baseDomainFactory))), new BoatJsonDeserializer(new BoatClassJsonDeserializer(baseDomainFactory)));
    }

    public CompetitorJsonDeserializer(CompetitorStore store) {
        this(store, null, /* boatDeserializer */ null);
    }

    public CompetitorJsonDeserializer(CompetitorFactory competitorStore, JsonDeserializer<DynamicTeam> teamJsonDeserializer, JsonDeserializer<DynamicBoat> boatDeserializer) {
        this.competitorStore = competitorStore;
        this.teamJsonDeserializer = teamJsonDeserializer;
        this.boatJsonDeserializer = boatDeserializer;
    }

    @Override
    public Competitor deserialize(JSONObject object) throws JsonDeserializationException {
        Serializable competitorId = (Serializable) object.get(CompetitorJsonSerializer.FIELD_ID);
        try {
            Class<?> idClass = Class.forName((String) object.get(CompetitorJsonSerializer.FIELD_ID_TYPE));
            if (Number.class.isAssignableFrom(idClass)) {
                Constructor<?> constructorFromString = idClass.getConstructor(String.class);
                competitorId = (Serializable) constructorFromString.newInstance(competitorId.toString());
            } else if (UUID.class.isAssignableFrom(idClass)) {
                competitorId = Helpers.tryUuidConversion(competitorId);
            }
            String name = (String) object.get(CompetitorJsonSerializer.FIELD_NAME);
            String displayColorAsString = (String) object.get(CompetitorJsonSerializer.FIELD_DISPLAY_COLOR);
            final Color displayColor;
            if (displayColorAsString == null || displayColorAsString.isEmpty()) {
                displayColor = null;
            } else {
                displayColor = new RGBColor(displayColorAsString);
            }
            DynamicTeam team = null;
            DynamicBoat boat = null;
            if (teamJsonDeserializer != null && object.get(CompetitorJsonSerializer.FIELD_TEAM) != null) {
                team = teamJsonDeserializer.deserialize(Helpers.getNestedObjectSafe(object,
                        CompetitorJsonSerializer.FIELD_TEAM));
            }
            if (boatJsonDeserializer != null && object.get(CompetitorJsonSerializer.FIELD_BOAT) != null) {
                boat = boatJsonDeserializer.deserialize(Helpers.getNestedObjectSafe(object,
                        CompetitorJsonSerializer.FIELD_BOAT));
            }
            Competitor competitor = competitorStore.getOrCreateCompetitor(competitorId, name, displayColor, team, boat);
            return competitor;
        } catch (Exception e) {
            throw new JsonDeserializationException(e);
        }
    }
}
