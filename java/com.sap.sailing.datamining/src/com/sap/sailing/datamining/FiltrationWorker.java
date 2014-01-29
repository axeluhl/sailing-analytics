package com.sap.sailing.datamining;

import java.util.Collection;

public interface FiltrationWorker<DataType> extends ComponentWorker<Collection<DataType>> {
    
    public void setDataToFilter(Collection<DataType> data);

}
