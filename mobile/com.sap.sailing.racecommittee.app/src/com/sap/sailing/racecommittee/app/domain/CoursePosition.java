package com.sap.sailing.racecommittee.app.domain;

import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;

import java.util.UUID;

public interface CoursePosition extends Named, WithID {

   UUID getId();

}
