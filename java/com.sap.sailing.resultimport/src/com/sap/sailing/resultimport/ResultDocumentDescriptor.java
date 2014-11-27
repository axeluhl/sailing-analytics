package com.sap.sailing.resultimport;

import java.io.InputStream;

import com.sap.sailing.domain.common.CompetitorGenderType;
import com.sap.sse.common.TimePoint;

public interface ResultDocumentDescriptor {
    String getEventName();
    String getRegattaName();
    String getBoatClass();
    CompetitorGenderType getCompetitorGenderType();

    TimePoint getLastModified();

    String getDocumentName();

    InputStream getInputStream();
}
