package com.sap.sailing.domain.common.impl;

public interface Function<In, Out> {
    Out perform(In in);
}
