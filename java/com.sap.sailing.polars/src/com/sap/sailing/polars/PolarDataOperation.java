package com.sap.sailing.polars;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sse.replication.OperationWithResult;

/**
 * 
 * Needed in the context of the initial replication of the polar data service.
 * 
 * @author D054528 Frederik Petersen
 *
 * @param <T>
 */
public interface PolarDataOperation<T> extends OperationWithResult<PolarDataService, T> {

}
