package com.sap.sailing.server.gateway.trackfiles.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.server.gateway.trackfiles.impl.ImportResult.ErrorImportDTO;

public class AllInOneImportException extends Exception {
    private static final long serialVersionUID = 1L;
    List<ErrorImportDTO> additionalErrors = new ArrayList<>();

    public AllInOneImportException(String message, Throwable cause, List<ErrorImportDTO> additionalErrors) {
        super(message, cause);
        this.additionalErrors.addAll(additionalErrors);
    }
    
    public AllInOneImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public AllInOneImportException(String message, List<ErrorImportDTO> additionalErrors) {
        super(message);
        this.additionalErrors.addAll(additionalErrors);
    }
    
    public AllInOneImportException(String message) {
        super(message);
    }

    public AllInOneImportException(Throwable cause, List<ErrorImportDTO> additionalErrors) {
        super(cause);
        this.additionalErrors.addAll(additionalErrors);
    }

}
