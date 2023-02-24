package com.sap.sse.security.datamining.data.impl;

import java.util.Collections;
import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sse.common.Util;
import com.sap.sse.security.datamining.data.HasPreferenceContext;
import com.sap.sse.serialization.Base64SerializerDeserializer;
import com.sap.sse.shared.classloading.JoinedClassLoader;

public abstract class AbstractPreferenceWithContext implements HasPreferenceContext {
    private final static JoinedClassLoader joinedClassLoader = new JoinedClassLoader(Collections.singleton(AbstractPreferenceWithContext.class.getClassLoader()));
    private final String preferenceName;
    private final String preferenceValue;
    
    public AbstractPreferenceWithContext(String preferenceName, String preferenceValue) {
        this.preferenceName = preferenceName;
        this.preferenceValue = preferenceValue;
    }

    @Override
    public String getPreferenceName() {
        return preferenceName;
    }

    @Override
    public String getPreferenceValue() {
        return preferenceValue;
    }

    @Override
    public int getNumberOfObjectsContained() {
        int result;
        if (!Util.hasLength(getPreferenceValue())) {
            result = 0;
        } else {
            final Object fromBase64 = Base64SerializerDeserializer.fromBase64(getPreferenceValue(), joinedClassLoader, Level.FINEST);
            if (fromBase64 == null) {
                // probably not Base64-encoded; try JSON:
                final JSONParser jsonParser = new JSONParser();
                try {
                    final Object o = jsonParser.parse(getPreferenceValue());
                    if (o instanceof JSONArray) {
                        result = ((JSONArray) o).size();
                    } else if (o instanceof JSONObject) {
                        result = ((JSONObject) o).size();
                    } else {
                        result = 0;
                    }
                } catch (ParseException e) {
                    result = 1; // it's a single string
                }
            } else if (fromBase64 instanceof Iterable) {
                result = Util.size((Iterable<?>) fromBase64);
            } else {
                result = 1; // it's a single Java object
            }
        }
        return result;
    }
}
