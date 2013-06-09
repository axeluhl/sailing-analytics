package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.NamedDTO;

public class CourseAreaDTO  extends NamedDTO implements IsSerializable {
    private static final long serialVersionUID = -5279690838452265454L;
    public String id;
    
    public CourseAreaDTO() {
    }

    public CourseAreaDTO(String name) {
        super(name);
    }
}
