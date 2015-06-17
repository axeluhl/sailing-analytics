package com.sap.sailing.racecommittee.app.domain;

import java.util.UUID;

import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;

public interface CoursePosition extends Named, WithID {

   UUID getId();

}
