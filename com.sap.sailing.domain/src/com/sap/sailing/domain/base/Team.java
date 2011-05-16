package com.sap.sailing.domain.base;

public interface Team extends Named {
    Iterable<Person> getSailors();
    Person getCoach();
}
