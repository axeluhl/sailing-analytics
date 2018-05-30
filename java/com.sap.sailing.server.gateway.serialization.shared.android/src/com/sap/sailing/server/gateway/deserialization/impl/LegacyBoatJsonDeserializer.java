package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.BoatFactory;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatJsonSerializer;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.util.impl.UUIDHelper;

public class LegacyBoatJsonDeserializer implements JsonDeserializer<DynamicBoat> {
    private final BoatClassJsonDeserializer boatClassDeserializer;

    public static LegacyBoatJsonDeserializer create(SharedDomainFactory baseDomainFactory) {
        return new LegacyBoatJsonDeserializer(baseDomainFactory, new BoatClassJsonDeserializer(baseDomainFactory));
    }
    
    public LegacyBoatJsonDeserializer(BoatFactory boatFactory, BoatClassJsonDeserializer boatClassDeserializer) {
        super();
        this.boatClassDeserializer = boatClassDeserializer;
    }

    public DynamicBoat deserialize(JSONObject object) throws JsonDeserializationException {
        Serializable boatId = (Serializable) object.get(BoatJsonSerializer.FIELD_ID);
        try {
            if (boatId == null) {
                // no boatId - probably a legacy boat -> create an UUID
                boatId = UUID.randomUUID();
            } else {
                Class<?> idClass = Class.forName((String) object.get(BoatJsonSerializer.FIELD_ID_TYPE));
                if (Number.class.isAssignableFrom(idClass)) {
                    Constructor<?> constructorFromString = idClass.getConstructor(String.class);
                    boatId = (Serializable) constructorFromString.newInstance(boatId.toString());
                } else if (UUID.class.isAssignableFrom(idClass)) {
                    boatId = UUIDHelper.tryUuidConversion(boatId);
                }
            }
            String name = (String) object.get(BoatJsonSerializer.FIELD_NAME);
            final Object sailID = object.get(BoatJsonSerializer.FIELD_SAIL_ID);
            String sailId = sailID == null ? null : sailID.toString();
            BoatClass boatClass = null;
            if (boatClassDeserializer != null) {
                boatClass = boatClassDeserializer.deserialize(Helpers.getNestedObjectSafe(object, BoatJsonSerializer.FIELD_BOAT_CLASS));
            }
            String colorAsString = (String) object.get(BoatJsonSerializer.FIELD_COLOR);
            final Color color = colorAsString == null || colorAsString.isEmpty() ? null : new RGBColor(colorAsString);
            DynamicBoat boat = new BoatImpl(boatId, name, boatClass, sailId, color);
    
            return boat;
        } catch (Exception e) {
            throw new JsonDeserializationException(e);
        }
    }

}
