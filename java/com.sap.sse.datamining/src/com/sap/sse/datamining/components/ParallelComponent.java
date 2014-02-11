package com.sap.sse.datamining.components;

import java.util.concurrent.Future;

public interface ParallelComponent<WorkingType, ResultType> extends Future<ResultType> {
    /**
     * @return <code>this</code> object
     */
    public ParallelComponent<WorkingType, ResultType> start(WorkingType data);

}
