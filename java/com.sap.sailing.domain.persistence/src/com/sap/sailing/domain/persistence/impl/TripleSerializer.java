package com.sap.sailing.domain.persistence.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sap.sse.common.UtilNew;

public class TripleSerializer {
    public static DBObject serialize(UtilNew.Triple<String, String, String> value) {
        DBObject container = new BasicDBObject();
        container.put("a", value.getA());
        container.put("b", value.getB());
        container.put("c", value.getC());
        return container;
    }
}
