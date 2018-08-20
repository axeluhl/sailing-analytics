package com.sap.sailing.domain.ranking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.Duration;
import com.sap.sse.util.SerializableFunction;

@FunctionalInterface
public interface TimeOnDistanceAllowancePerNauticalMileMap extends SerializableFunction<Competitor, Duration> {
}
