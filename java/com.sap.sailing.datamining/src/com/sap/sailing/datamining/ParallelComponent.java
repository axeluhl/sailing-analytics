package com.sap.sailing.datamining;

import java.util.concurrent.Future;

public interface ParallelComponent<WorkingType, ResultType> extends Future<ResultType> {
    
    public ParallelComponent<WorkingType, ResultType> start(WorkingType data);

}
