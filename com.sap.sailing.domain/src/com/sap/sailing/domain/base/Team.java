package com.sap.sailing.domain.base;

public interface Team extends Named {
    Iterable<Sailor> getSailors();
}
