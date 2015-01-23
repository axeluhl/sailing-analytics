package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

public class FileStoragePropertyDTO implements Serializable {
    private static final long serialVersionUID = -2721807793068803143L;
    public boolean isRequired;
    public String name;
    public String value;
    public String description;

    // for GWT
    FileStoragePropertyDTO() {
    }

    public FileStoragePropertyDTO(boolean isRequired, String name, String value, String description) {
        this.isRequired = isRequired;
        this.name = name;
        this.value = value;
        this.description = description;
    }
}
