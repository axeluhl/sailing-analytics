package com.sap.sailing.datamining;

import java.util.Collection;

public interface SingleThreadedFilter<DataType> extends ComponentWorker {

    public void setReceiver(FilterReceiver<DataType> receiver);
    
    public void setDataToFilter(Collection<DataType> data);

}
