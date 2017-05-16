package com.sap.sse.datamining.data;

import com.sap.sse.datamining.shared.GroupKey;

public interface ExtensibleQueryResult<ResultType> extends QueryResult<ResultType> {
    void addResult(GroupKey key, ResultType result);
}
