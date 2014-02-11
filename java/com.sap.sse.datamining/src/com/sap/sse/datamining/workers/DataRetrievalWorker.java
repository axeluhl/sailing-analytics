package com.sap.sse.datamining.workers;

import java.util.Collection;

public interface DataRetrievalWorker<SourceType, DataType> extends ComponentWorker<Collection<DataType>> {

    public void setSource(SourceType source);

}
