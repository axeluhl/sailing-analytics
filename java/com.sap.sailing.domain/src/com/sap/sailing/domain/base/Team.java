package com.sap.sailing.domain.base;

public interface Team extends Named, WithNationality {
    Iterable<? extends Person> getSailors();
    Person getCoach();
}
