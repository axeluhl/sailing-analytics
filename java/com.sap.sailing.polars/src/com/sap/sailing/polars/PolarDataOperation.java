package com.sap.sailing.polars;

import com.sap.sailing.polars.impl.PolarDataServiceImpl;
import com.sap.sse.replication.OperationWithResult;

public interface PolarDataOperation<T> extends OperationWithResult<PolarDataServiceImpl, T>{

}
