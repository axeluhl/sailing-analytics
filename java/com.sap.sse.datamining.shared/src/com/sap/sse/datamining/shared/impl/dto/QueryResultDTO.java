package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;
import java.util.Map;

import com.sap.sse.datamining.shared.AdditionalResultData;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResultBase;
import com.sap.sse.datamining.shared.data.QueryResultState;
import com.sap.sse.datamining.shared.impl.NullAdditionalResultData;
import com.sap.sse.datamining.shared.impl.QueryResultBaseImpl;

public class QueryResultDTO<ResultType extends Serializable> extends QueryResultBaseImpl<ResultType>
        implements QueryResultBase<ResultType> {
    private static final long serialVersionUID = 3639302996859873603L;
    
    private String resultTypeName;

    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    QueryResultDTO() {
    }

    public QueryResultDTO(QueryResultState state, Class<ResultType> resultType, Map<GroupKey, ResultType> results) {
        this(state, resultType, results, new NullAdditionalResultData());
    }

    public QueryResultDTO(QueryResultState state, Class<ResultType> resultType, Map<GroupKey, ResultType> results,
            AdditionalResultData additionalData) {
        super(state, results, additionalData);
        this.resultTypeName = resultType.getName();
    }

    public String getResultType() {
        return resultTypeName;
    }

}
