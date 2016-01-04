package com.sap.sailing.server.trackfiles.impl;

import com.sap.sse.common.Timed;

interface TrackReaderRetriever<E, T extends Timed> {
    TrackReader<E, T> retrieveTrackReader(E e);
}