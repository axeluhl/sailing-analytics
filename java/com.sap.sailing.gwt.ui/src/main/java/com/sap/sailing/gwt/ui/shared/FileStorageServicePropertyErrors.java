package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;
import java.util.Map;

public class FileStorageServicePropertyErrors implements Serializable {
    private static final long serialVersionUID = -7328897153875728802L;
    public Map<FileStorageServicePropertyDTO, String> perPropertyMessages;
    public String message;

    // for GWT
    FileStorageServicePropertyErrors() {
    }

    public FileStorageServicePropertyErrors(String message, Map<FileStorageServicePropertyDTO, String> perPropertyMessages) {
        this.message = message;
        this.perPropertyMessages = perPropertyMessages;
    }
}
