package com.sap.sailing.domain.common.racelog.tracking;

public interface TransformationHandler<T1, T2> {
	T1 transformBack(T2 object) throws TransformationException;
	T2 transformForth(T1 object) throws TransformationException;
}
