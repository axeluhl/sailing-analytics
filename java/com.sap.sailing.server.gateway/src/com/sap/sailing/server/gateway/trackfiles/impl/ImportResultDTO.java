package com.sap.sailing.server.gateway.trackfiles.impl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.TimeRange;

/**
 * Convenience class that wraps the json objects used to render json result objects
 */
public class ImportResultDTO {
    private final List<ErrorImportDTO> errorList = new CopyOnWriteArrayList<>();
    private final List<TrackImportDTO> importResult = new CopyOnWriteArrayList<>();
    private final Logger logger;

    static class TrackImportDTO {
        private TimeRange range;
        private long amount;
        private UUID device;

        public TrackImportDTO(UUID device, TimeRange range, long amount) {
            this.device = device;
            this.range = range;
            this.amount = amount;
        }

        public TimeRange getRange() {
            return range;
        }
        
        public UUID getDevice() {
            return device;
        }

        public long getAmount() {
            return amount;
        }
    }

    static class ErrorImportDTO {
        private String exUUID;
        private String name;
        private String message;
        private String filename;
        private String requestedImporter;

        public ErrorImportDTO(String exUUID, String name, String message, String filename, String requestedImporter) {
            this.exUUID = exUUID;
            this.name = name;
            this.message = message;
            this.filename = filename;
            this.requestedImporter = requestedImporter;
        }

        public String getExUUID() {
            return exUUID;
        }

        public String getName() {
            return name;
        }

        public String getMessage() {
            return message;
        }

        public String getFilename() {
            return filename;
        }

        public String getRequestedImporter() {
            return requestedImporter;
        }
        
        
    }

    public ImportResultDTO(Logger logger) {
        this.logger = logger;
    }

    public void add(String requestedImporterName, String filename, Exception exception) {
        logger.log(Level.SEVERE, "Sensordata import importer: " + requestedImporterName);
        logger.log(Level.SEVERE, "Sensordata import filename: " + filename);
        logException(exception, filename, requestedImporterName);
    }

    public void add(Exception exception) {
        logException(exception, "", "");
    }

    private void logException(Exception e, String filename, String requestedImporterName) {
        final String exUUID = UUID.randomUUID().toString();
        logger.log(Level.SEVERE, "Sensordata import ExUUID: " + exUUID, e);
        errorList.add(
                new ErrorImportDTO(exUUID, e.getClass().getName(), e.getMessage(), filename, requestedImporterName));
    }

    public void noImporterSucceeded(String filename) {
        errorList.add(new ErrorImportDTO(null,null,"No importer succeeded to process file",filename,null));
    }

    public void addTrackData(TrackImportDTO trackImportDTO) {
        importResult.add(trackImportDTO);
    }
    
    public List<ErrorImportDTO> getErrorList() {
        return errorList;
    }
    
    public List<TrackImportDTO> getImportResult() {
        return importResult;
    }
}