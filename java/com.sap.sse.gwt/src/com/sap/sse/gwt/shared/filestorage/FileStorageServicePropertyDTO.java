package com.sap.sse.gwt.shared.filestorage;

import java.io.Serializable;

public class FileStorageServicePropertyDTO implements Serializable {
    private static final long serialVersionUID = -2721807793068803143L;
    public boolean isRequired;
    public String name;
    public String value;
    public String description;

    // for GWT
    FileStorageServicePropertyDTO() {
    }

    public FileStorageServicePropertyDTO(boolean isRequired, String name, String value, String description) {
        this.isRequired = isRequired;
        this.name = name;
        this.value = value;
        this.description = description;
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
        FileStorageServicePropertyDTO other = (FileStorageServicePropertyDTO) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
