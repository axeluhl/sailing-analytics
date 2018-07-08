package com.sap.sse.datamining.ui.test.client.presentation;

import java.io.Serializable;
import java.util.HashMap;

import com.sap.sse.datamining.shared.data.QueryResultState;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public class DummyQueryResultDTO<ResultType extends Serializable> extends QueryResultDTO<ResultType> {

    private static final long serialVersionUID = 4163808455941696613L;

    public DummyQueryResultDTO(Class<ResultType> resultType) {
        super(QueryResultState.NORMAL, resultType, new HashMap<>());
    }
}
