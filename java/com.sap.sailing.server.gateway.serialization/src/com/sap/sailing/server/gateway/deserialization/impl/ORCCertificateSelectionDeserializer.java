package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.orc.ORCCertificateSelection;
import com.sap.sailing.domain.common.orc.ORCCertificateUploadConstants;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateSelectionImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.util.impl.UUIDHelper;

/**
 * Assumes to find a JSON array in a field {@link ORCCertificateUploadConstants#CERTIFICATE_SELECTION} containin
 * objects, each with a {@link ORCCertificateUploadConstants#BOAT_ID} and a
 * {@link ORCCertificateUploadConstants#CERTIFICATE_ID} key, describing the mapping.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ORCCertificateSelectionDeserializer implements JsonDeserializer<ORCCertificateSelection> {
    @Override
    public ORCCertificateSelection deserialize(JSONObject object) throws JsonDeserializationException {
        final Map<Serializable, String> mappings = new HashMap<>();
        final JSONArray array = (JSONArray) object.get(ORCCertificateUploadConstants.CERTIFICATE_SELECTION);
        for (final Object o : array) {
            final JSONObject mapping = (JSONObject) o;
            final Serializable boatId = UUIDHelper.tryUuidConversion((Serializable) mapping.get(ORCCertificateUploadConstants.BOAT_ID));
            final String certificateId = (String) mapping.get(ORCCertificateUploadConstants.CERTIFICATE_ID);
            if (mappings.put(boatId, certificateId) != null) {
                throw new IllegalArgumentException("Non-unique mapping found in ORC certificate selection for boat with ID "+boatId);
            }
        }
        return new ORCCertificateSelectionImpl(mappings);
    }
}
