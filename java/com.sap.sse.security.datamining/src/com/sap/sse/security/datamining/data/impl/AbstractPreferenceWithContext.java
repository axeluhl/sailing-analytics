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
        final String stringValue = getPreferenceValue();
        result = getNumberOfObjectsContained(stringValue);
        return result;
    }
    
    private int getNumberOfObjectsContained(final Object o) {
        int result = 0;
        if (o instanceof String) {
            result = getNumberOfObjectsContained((String) o);
        } else  if (o instanceof JSONArray) {
            final JSONArray array = (JSONArray) o;
            for (final Object a : array) {
                if (a != null) {
                    if (a instanceof String) {
                        result += getNumberOfObjectsContained((String) a);
                    } else if (a instanceof JSONObject) {
                        result += getNumberOfObjectsContained((JSONObject) a);
                    } else {
                        result++;
                    }
                }
            }
        } else if (o instanceof JSONObject) {
            result = getNumberOfObjectsContained((JSONObject) o);
        } else {
            result = 1;
        }
        return result;
    }

    private int getNumberOfObjectsContained(final String stringValueMaybeBase64SerializedMaybeJson) {
        int result = 0;
        if (Util.hasLength(stringValueMaybeBase64SerializedMaybeJson)) {
            final Object fromBase64 = Base64SerializerDeserializer.fromBase64(stringValueMaybeBase64SerializedMaybeJson, joinedClassLoader, Level.FINEST);
            if (fromBase64 == null) {
                // probably not Base64-encoded; try JSON:
                final JSONParser jsonParser = new JSONParser();
                try {
                    result += getNumberOfObjectsContained(jsonParser.parse(stringValueMaybeBase64SerializedMaybeJson));
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

    private int getNumberOfObjectsContained(JSONObject a) {
        int result = 0;
        for (final Object o : a.values()) {
            result += getNumberOfObjectsContained(o);
        }
        return result;
    }
}
