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

    /**
     * An instance of this deserializer class may be used to load competitors and their boats
     * from the persistent store. In this case, when creating the {@link Competitor} object,
     * the new object shall <em>not</em> be stored / updated again to the persistent store
     * because it just came from there. This behavior can be accomplished by setting this
     * property to {@code false} by means of using a corresponding constructor.<p>
     * 
     * See also bug 5106.
     */
    private final boolean storeDeserializedCompetitorsPersistently;
    
    public static BoatJsonDeserializer create(SharedDomainFactory baseDomainFactory, boolean storeDeserializedCompetitorsPersistently) {
        return new BoatJsonDeserializer(baseDomainFactory, new BoatClassJsonDeserializer(baseDomainFactory), storeDeserializedCompetitorsPersistently);
    }
    
    public static BoatJsonDeserializer create(SharedDomainFactory baseDomainFactory) {
        return create(baseDomainFactory, /* storeDeserializedCompetitorsPersistently */ true);
    }
    
    public BoatJsonDeserializer(BoatFactory boatFactory, BoatClassJsonDeserializer boatClassDeserializer) {
        this(boatFactory, boatClassDeserializer, /* storeDeserializedCompetitorsPersistently */ true);
    }
    
    public BoatJsonDeserializer(BoatFactory boatFactory, BoatClassJsonDeserializer boatClassDeserializer, boolean storeDeserializedCompetitorsPersistently) {
        super();
        this.boatFactory = boatFactory;
        this.boatClassDeserializer = boatClassDeserializer;
        this.storeDeserializedCompetitorsPersistently = storeDeserializedCompetitorsPersistently;
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
            DynamicBoat boat = (DynamicBoat) boatFactory.getOrCreateBoat(boatId, name, boatClass, sailId, color, storeDeserializedCompetitorsPersistently);
            return boat;
        } catch (Exception e) {
            throw new JsonDeserializationException(e);
        }
    }

}
