package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.BoatFactory;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatJsonSerializer;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.util.impl.UUIDHelper;

public class BoatJsonDeserializer implements JsonDeserializer<DynamicBoat> {
    private static final Logger logger = Logger.getLogger(BoatJsonDeserializer.class.getName());
    
    private final BoatClassJsonDeserializer boatClassDeserializer;
    private final BoatFactory boatFactory;

    public static BoatJsonDeserializer create(SharedDomainFactory baseDomainFactory) {
        return new BoatJsonDeserializer(baseDomainFactory, new BoatClassJsonDeserializer(baseDomainFactory));
    }
    
    public BoatJsonDeserializer(BoatFactory boatFactory, BoatClassJsonDeserializer boatClassDeserializer) {
        super();
        this.boatFactory = boatFactory;
        this.boatClassDeserializer = boatClassDeserializer;
    }
    
    @Override
    public DynamicBoat deserialize(JSONObject object) throws JsonDeserializationException {
        return deserialize(object, /* default ID */ null);
    }

    /**
     * @param defaultId
     *            used as the boat ID in case no {@link BoatJsonSerializer#FIELD_ID} and/or
     *            {@link BoatJsonSerializer#FIELD_ID_TYPE} are found; if {@code null} and no ID is found in the JSON
     *            {@code object}, a {@link UUID#randomUUID() random UUID} will be generated and used instead.
     */
    public DynamicBoat deserialize(JSONObject object, Serializable defaultId) throws JsonDeserializationException {
        final Serializable boatIdFromJson = (Serializable) object.get(BoatJsonSerializer.FIELD_ID);
        final String idType = (String) object.get(BoatJsonSerializer.FIELD_ID_TYPE);
        final Serializable boatId;
        try {
            if (boatIdFromJson == null || idType == null) {
                if (defaultId != null) {
                    boatId = defaultId;
                } else {
                    boatId = UUID.randomUUID();
                    logger.warning("Trying to de-serialize a boat from "+object+
                            " that comes without an ID. Generated new ID "+boatId);
                }
            } else {
                Class<?> idClass = Class.forName(idType);
                if (Number.class.isAssignableFrom(idClass)) {
                    Constructor<?> constructorFromString = idClass.getConstructor(String.class);
                    boatId = (Serializable) constructorFromString.newInstance(boatIdFromJson.toString());
                } else if (UUID.class.isAssignableFrom(idClass)) {
                    boatId = UUIDHelper.tryUuidConversion(boatIdFromJson);
                } else {
                    boatId = boatIdFromJson;
                }
            }
            String name = (String) object.get(BoatJsonSerializer.FIELD_NAME);
            String sailId = (String) object.get(BoatJsonSerializer.FIELD_SAIL_ID);
            BoatClass boatClass = null;
            if (boatClassDeserializer != null) {
                boatClass = boatClassDeserializer.deserialize(Helpers.getNestedObjectSafe(object, BoatJsonSerializer.FIELD_BOAT_CLASS));
            }
            String colorAsString = (String) object.get(BoatJsonSerializer.FIELD_COLOR);
            final Color color = colorAsString == null || colorAsString.isEmpty() ? null
                    : new RGBColor(colorAsString);
            DynamicBoat boat = (DynamicBoat) boatFactory.getOrCreateBoat(boatId, name, boatClass, sailId, color);
            return boat;
        } catch (Exception e) {
            throw new JsonDeserializationException(e);
        }
    }

}
