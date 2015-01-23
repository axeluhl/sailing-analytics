package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

public class FileStorageServiceDTO implements Serializable {
    private static final long serialVersionUID = 6101940297792100418L;
    public String name;
    public String description;
    public FileStoragePropertyDTO[] properties;

    // for GWT
    FileStorageServiceDTO() {
    }

    public FileStorageServiceDTO(String name, String description, FileStoragePropertyDTO... properties) {
        this.name = name;
        this.description = description;
        this.properties = properties;
    }
}
