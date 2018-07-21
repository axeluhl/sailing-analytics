package com.sap.sailing.server.gateway.trackfiles.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.server.gateway.trackfiles.impl.ImportResult.ErrorImportDTO;

public class AllinOneImportException extends Exception {
    private static final long serialVersionUID = 1L;
    List<ErrorImportDTO> additionalErrors = new ArrayList<>();

    public AllinOneImportException(String message, Throwable cause, List<ErrorImportDTO> additionalErrors) {
        super(message, cause);
        this.additionalErrors.addAll(additionalErrors);
    }
    
    public AllinOneImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public AllinOneImportException(String message, List<ErrorImportDTO> additionalErrors) {
        super(message);
        this.additionalErrors.addAll(additionalErrors);
    }
    
    public AllinOneImportException(String message) {
        super(message);
    }

    public AllinOneImportException(Throwable cause, List<ErrorImportDTO> additionalErrors) {
        super(cause);
        this.additionalErrors.addAll(additionalErrors);
    }

}
