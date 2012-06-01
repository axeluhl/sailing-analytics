package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CourseAreaDTO  extends NamedDTO implements IsSerializable {
    public CourseAreaDTO() {
    }

    public CourseAreaDTO(String name) {
        super(name);
    }
}
