package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.util.Iterator;

public interface PersistedElementsIterator<T> extends Iterator<T> {

    long getNumberOfElements();
    
    PersistedElementsIterator<T> limit(long limit);

}
