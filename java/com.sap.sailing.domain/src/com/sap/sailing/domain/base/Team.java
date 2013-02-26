package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;

public interface Team extends Named, WithNationality {
    Iterable<? extends Person> getSailors();
    Person getCoach();
}
