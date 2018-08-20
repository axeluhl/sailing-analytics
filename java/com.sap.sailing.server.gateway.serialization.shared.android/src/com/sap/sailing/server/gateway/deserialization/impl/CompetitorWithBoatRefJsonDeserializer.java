package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatFactory;
import com.sap.sailing.domain.base.CompetitorFactory;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.common.tracking.impl.CompetitorJsonConstants;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.util.impl.UUIDHelper;

public class CompetitorWithBoatRefJsonDeserializer extends CompetitorJsonDeserializer {
    private final BoatFactory boatFactory;

    public static CompetitorWithBoatRefJsonDeserializer create(SharedDomainFactory baseDomainFactory) {
        return new CompetitorWithBoatRefJsonDeserializer(baseDomainFactory, baseDomainFactory, new TeamJsonDeserializer(new PersonJsonDeserializer(
                new NationalityJsonDeserializer(baseDomainFactory))));
    }

    public CompetitorWithBoatRefJsonDeserializer(CompetitorFactory competitorFactory, BoatFactory boatFactory) {
        this(competitorFactory, boatFactory, null);
    }

    public CompetitorWithBoatRefJsonDeserializer(CompetitorFactory competitorFactory, BoatFactory boatFactory, JsonDeserializer<DynamicTeam> teamJsonDeserializer) {
        super(competitorFactory, teamJsonDeserializer, /* boat deserializer */ null);
        this.boatFactory = boatFactory;
    }

    /**
     * Looks for {@link CompetitorJsonConstants#FIELD_BOAT_ID} in the JSON {@code object}. If found, the boat is looked
     * up in the {@link #boatFactory} and the lookup result is returned; otherwise, {@code null} is returned.
     */
    @Override
    protected DynamicBoat getBoat(JSONObject object, Serializable defaultId) throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        // if we find a boat identifier we try to find the boat with the boatfactory
        Serializable boatId = (Serializable) object.get(CompetitorJsonConstants.FIELD_BOAT_ID);
        if (boatId != null) {
            Class<?> boatIdClass = Class.forName((String) object.get(CompetitorJsonConstants.FIELD_BOAT_ID_TYPE));
            if (Number.class.isAssignableFrom(boatIdClass)) {
                Constructor<?> constructorFromString = boatIdClass.getConstructor(String.class);
                boatId = (Serializable) constructorFromString.newInstance(boatId.toString());
            } else if (UUID.class.isAssignableFrom(boatIdClass)) {
                boatId = UUIDHelper.tryUuidConversion(boatId);
            }
        }
        final DynamicBoat existingBoat = (DynamicBoat) (boatId != null ? boatFactory.getExistingBoatById(boatId) : null);
        return existingBoat;
    }
}
