package com.sap.sailing.domain.persistence.impl;

import org.bson.Document;

import com.sap.sse.common.Util.Triple;

public class TripleSerializer {
    private static final String C = "c";
    private static final String B = "b";
    private static final String A = "a";

    public static Document serialize(Triple<String, String, String> value) {
        Document container = new Document();
        container.put(A, value.getA());
        container.put(B, value.getB());
        container.put(C, value.getC());
        return container;
    }
    
    public static Triple<String, String, String> deserialize(Document container) {
        return new Triple<>(container.get(A).toString(), container.get(B).toString(), container.get(C).toString());
    }
}
