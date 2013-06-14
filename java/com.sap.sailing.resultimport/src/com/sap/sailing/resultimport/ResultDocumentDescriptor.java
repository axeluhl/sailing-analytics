package com.sap.sailing.resultimport;

import java.io.InputStream;

import com.sap.sailing.domain.common.TimePoint;

public interface ResultDocumentDescriptor {
    String getEventName();
    String getRegattaName();
    String getBoatClass();

    TimePoint getLastModified();

    String getDocumentName();

    InputStream getInputStream();
}
