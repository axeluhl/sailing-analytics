package com.sap.sailing.domain.base;

public interface Coach extends Person {
    Iterable<Sailor> getTrainees();
}
