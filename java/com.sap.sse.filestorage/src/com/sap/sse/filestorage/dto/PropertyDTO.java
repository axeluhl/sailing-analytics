package com.sap.sse.filestorage.dto;

import java.io.Serializable;

public class PropertyDTO implements Serializable {
    private static final long serialVersionUID = -2721807793068803143L;
    public boolean isRequired;
    public String name;
    public String value;
    public String description;

    // for GWT
    PropertyDTO() {
    }

    public PropertyDTO(boolean isRequired, String name, String value, String description) {
        this.isRequired = isRequired;
        this.name = name;
        this.value = value;
        this.description = description;
    }
}
