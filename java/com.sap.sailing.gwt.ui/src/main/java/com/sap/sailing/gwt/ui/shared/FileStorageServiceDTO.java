package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

public class FileStorageServiceDTO implements Serializable {
    private static final long serialVersionUID = 6101940297792100418L;
    public String name;
    public String description;
    public FileStorageServicePropertyDTO[] properties;

    // for GWT
    FileStorageServiceDTO() {
    }

    public FileStorageServiceDTO(String name, String description, FileStorageServicePropertyDTO... properties) {
        this.name = name;
        this.description = description;
        this.properties = properties;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileStorageServiceDTO other = (FileStorageServiceDTO) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }    
}
