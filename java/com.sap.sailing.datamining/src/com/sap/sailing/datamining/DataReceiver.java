package com.sap.sailing.datamining;

import java.util.Collection;

public interface DataReceiver<T> {
    
    public void addData(Collection<T> data);

}
