package com.sap.sailing.datamining;

import com.sap.sailing.datamining.impl.QueryImpl;

public class QueryFactory {
    
    public static Query createQuery(Selector selector) {
        return new QueryImpl(selector);
    }
}
