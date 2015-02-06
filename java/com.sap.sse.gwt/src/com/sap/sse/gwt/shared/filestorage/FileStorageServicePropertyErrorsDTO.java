package com.sap.sse.gwt.shared.filestorage;

import java.io.Serializable;
import java.util.Map;

public class FileStorageServicePropertyErrorsDTO implements Serializable {
    private static final long serialVersionUID = -7328897153875728802L;
    public Map<FileStorageServicePropertyDTO, String> perPropertyMessages;
    public String message;

    // for GWT
    FileStorageServicePropertyErrorsDTO() {
    }

    public FileStorageServicePropertyErrorsDTO(String message, Map<FileStorageServicePropertyDTO, String> perPropertyMessages) {
        this.message = message;
        this.perPropertyMessages = perPropertyMessages;
    }
}
