package com.sap.sailing.server.trackfiles.impl;

import com.sap.sailing.domain.base.Timed;

interface TrackReaderRetriever<E, T extends Timed> {
	TrackReader<E, T> retrieveTrackReader(E e);
}