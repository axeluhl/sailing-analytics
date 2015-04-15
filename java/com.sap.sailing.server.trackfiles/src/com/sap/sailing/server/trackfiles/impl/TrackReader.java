package com.sap.sailing.server.trackfiles.impl;

import com.sap.sse.common.Timed;

interface TrackReader<E, T extends Timed> {

    Iterable<T> getRawTrack(E e);

    Iterable<T> getTrack(E e);

    IterableLocker getLocker();
}