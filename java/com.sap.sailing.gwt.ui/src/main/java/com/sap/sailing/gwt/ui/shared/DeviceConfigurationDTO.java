package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DeviceConfigurationDTO implements IsSerializable {

    public List<String> allowedCourseAreaNames;
    public Integer minRoundsForCourse;
    public Integer maxRoundsForCourse;
    public String resultsMailRecipient;

}
