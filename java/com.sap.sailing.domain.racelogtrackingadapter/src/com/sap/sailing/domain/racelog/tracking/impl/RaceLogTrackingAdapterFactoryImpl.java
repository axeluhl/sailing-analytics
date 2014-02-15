package com.sap.sailing.domain.racelog.tracking.impl;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.racelog.tracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelog.tracking.RaceLogTrackingAdapterFactory;

public enum RaceLogTrackingAdapterFactoryImpl implements RaceLogTrackingAdapterFactory {
	INSTANCE;
	private RaceLogTrackingAdapter adapter;

	@Override
	public RaceLogTrackingAdapter getAdapter(DomainFactory baseDomainFactory) {
		if (adapter == null) {
			adapter = new RaceLogTrackingAdapterImpl(baseDomainFactory);
		}
		return adapter;
	}

}
