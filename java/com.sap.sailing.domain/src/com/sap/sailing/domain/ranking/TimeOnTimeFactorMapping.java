package com.sap.sailing.domain.ranking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.util.SerializableFunction;

@FunctionalInterface
public interface TimeOnTimeFactorMapping extends SerializableFunction<Competitor, Double> {
}
