package com.sap.sailing.server.trackfiles.impl;

import com.sap.sailing.domain.base.Timed;

interface TrackReader<E, T extends Timed> {

	Iterable<T> getRawTrack(E e);

	Iterable<T> getTrack(E e);

	IterableLocker getLocker();
}