package com.sap.sailing.datamining.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryResultImpl<AggregatedType> implements QueryResult<AggregatedType> {
    private static final long serialVersionUID = 351213515082955565L;
    
    private Map<GroupKey, AggregatedType> results;
    private int dataSize;
    
    private String resultSignifier;

    @Deprecated
    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    public QueryResultImpl() { }
    
    public QueryResultImpl(int dataSize, String resultSiginifier) {
        this.dataSize = dataSize;
        results = new HashMap<GroupKey, AggregatedType>();
        this.resultSignifier = resultSiginifier;
    }

    @Override
    public int getDataSize() {
        return dataSize;
    }

    @Override
    public Map<GroupKey, AggregatedType> getResults() {
        return results;
    }

    @Override
    public List<GroupKey> getSortedKeys() {
        List<GroupKey> sortedKeys = new ArrayList<GroupKey>(getResults().keySet());
        Collections.sort(sortedKeys, new Comparator<GroupKey>() {
            @Override
            public int compare(GroupKey key1, GroupKey key2) {
                return key1.asString().compareTo(key2.asString());
            }
        });
        return null;
    }

    @Override
    public String getResultSignifier() {
        return resultSignifier;
    }

    public void addResult(GroupKey key, AggregatedType value) {
        results.put(key, value);
    }

}
