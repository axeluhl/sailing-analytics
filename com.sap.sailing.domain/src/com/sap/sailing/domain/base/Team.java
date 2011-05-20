package com.sap.sailing.domain.base;

public interface Team extends Named, WithNationality {
    Iterable<Person> getSailors();
    Person getCoach();
}
