package com.sap.sailing.domain.persistence.impl;

import org.bson.Document;

import com.sap.sse.common.Util;

public class TripleSerializer {
    public static Document serialize(Util.Triple<String, String, String> value) {
        Document container = new Document();
        container.put("a", value.getA());
        container.put("b", value.getB());
        container.put("c", value.getC());
        return container;
    }
}
