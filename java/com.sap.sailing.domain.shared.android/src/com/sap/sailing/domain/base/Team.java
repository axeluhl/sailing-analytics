package com.sap.sailing.domain.base;

import java.net.URI;

import com.sap.sse.common.Named;

public interface Team extends Named, WithNationality {
    Iterable<? extends Person> getSailors();
    Person getCoach();
    URI getImage();
}
