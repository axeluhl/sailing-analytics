package com.sap.sailing.windestimation.data.persistence;

import java.util.Iterator;

public interface PersistedElementsIterator<T> extends Iterator<T> {

    long getNumberOfElements();

}
