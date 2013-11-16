package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

public class DeviceConfigurationDTO implements IsSerializable {

    public List<String> allowedCourseAreaNames;
    public String resultsMailRecipient;
    public RacingProcedureType defaultRacingProcedureType;
    public CourseDesignerMode defaultCourseDesignerMode;
    public List<String> byNameDesignerCourseNames;

}
