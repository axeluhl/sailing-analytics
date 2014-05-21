package com.sap.sailing.gwt.home.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.NamedDTO;

public class CourseAreaDTO extends NamedDTO implements IsSerializable {
    private static final long serialVersionUID = -5279690838452265454L;
    public String uuid;

    public CourseAreaDTO() {
    }

    public CourseAreaDTO(String name) {
        super(name);
    }
}
