package com.sap.sse.datamining.impl.data;

import java.util.Map;

import com.sap.sse.datamining.data.ExtensibleQueryResult;
import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.QueryResultState;
import com.sap.sse.datamining.shared.impl.NullAdditionalResultData;
import com.sap.sse.datamining.shared.impl.QueryResultBaseImpl;

public class QueryResultImpl<ResultType> extends QueryResultBaseImpl<ResultType> implements ExtensibleQueryResult<ResultType> {
    private static final long serialVersionUID = 5173796619174827696L;
    
    private Class<ResultType> resultType;
    
    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    QueryResultImpl() {
        super();
    }
    
    public QueryResultImpl(QueryResultState state, Class<ResultType> resultType, Map<GroupKey, ResultType> results) {
        this(state, resultType, results, new NullAdditionalResultData());
    }
    
    public QueryResultImpl(QueryResultState state, Class<ResultType> resultType, Map<GroupKey, ResultType> results, AdditionalResultData additionalData) {
        super(state, results, additionalData);
        this.resultType = resultType;
    }
    
    @Override
    public Class<ResultType> getResultType() {
        return resultType;
    }
    
    @Override
    public AdditionalResultData getAdditionalData() {
        return super.getAdditionalData();
    }
}
