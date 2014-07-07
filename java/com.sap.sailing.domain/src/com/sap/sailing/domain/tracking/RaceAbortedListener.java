package com.sap.sailing.domain.tracking;

import java.io.IOException;
import java.net.MalformedURLException;

import com.sap.sailing.domain.common.racelog.Flags;

public interface RaceAbortedListener {
    void raceAborted(Flags flag) throws MalformedURLException, IOException;
}
