package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;
import java.util.Map;

public class FileStoragePropertyErrors implements Serializable {
    private static final long serialVersionUID = -7328897153875728802L;
    public Map<FileStoragePropertyDTO, String> perPropertyMessages;
    public String message;

    // for GWT
    FileStoragePropertyErrors() {
    }

    public FileStoragePropertyErrors(String message, Map<FileStoragePropertyDTO, String> perPropertyMessages) {
        this.message = message;
        this.perPropertyMessages = perPropertyMessages;
    }
}
