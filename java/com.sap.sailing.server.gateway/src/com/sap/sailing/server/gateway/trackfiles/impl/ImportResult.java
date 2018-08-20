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
class ImportResult {

    private final List<ErrorImportDTO> errorList = new CopyOnWriteArrayList<>();
    private final List<TrackImportDTO> importResult = new CopyOnWriteArrayList<>();
    private final Logger logger;

    static class TrackImportDTO {

        private final TimeRange range;
        private final long amount;
        private final UUID device;

        TrackImportDTO(UUID device, TimeRange range, long amount) {
            this.device = device;
            this.range = range;
            this.amount = amount;
        }

        TimeRange getRange() {
            return range;
        }

        UUID getDevice() {
            return device;
        }

        long getAmount() {
            return amount;
        }
    }

    static class ErrorImportDTO {

        private final String exUUID;
        private final String name;
        private final String message;
        private final String filename;
        private final String requestedImporter;

        ErrorImportDTO(String message) {
            this(null, null, message, null, null);
        }
        
        ErrorImportDTO(String classname, String message) {
            this(null, classname, message, null, null);
        }

        ErrorImportDTO(String exUUID, String name, String message, String filename, String requestedImporter) {
            this.exUUID = exUUID;
            this.name = name;
            this.message = message;
            this.filename = filename;
            this.requestedImporter = requestedImporter;
        }

        String getExUUID() {
            return exUUID;
        }

        String getName() {
            return name;
        }

        String getMessage() {
            return message;
        }

        String getFilename() {
            return filename;
        }

        String getRequestedImporter() {
            return requestedImporter;
        }

    }

    ImportResult(Logger logger) {
        this.logger = logger;
    }

    void add(String requestedImporterName, String filename, Exception exception) {
        logger.log(Level.SEVERE, "Sensordata import importer: " + requestedImporterName);
        logger.log(Level.SEVERE, "Sensordata import filename: " + filename);
        logException(exception, filename, requestedImporterName);
    }

    void add(Exception exception) {
        logException(exception, "", "");
    }

    private void logException(Exception exception, String filename, String requestedImporterName) {
        final String exUUID = UUID.randomUUID().toString(), exClass = exception.getClass().getName();
        logger.log(Level.SEVERE, "Sensordata import ExUUID: " + exUUID, exception);
        errorList.add(new ErrorImportDTO(exUUID, exClass, exception.getMessage(), filename, requestedImporterName));
    }

    void noImporterSucceeded(String filename) {
        errorList.add(new ErrorImportDTO(null, null, "No importer succeeded to process file", filename, null));
    }

    void addTrackData(TrackImportDTO trackImportDTO) {
        importResult.add(trackImportDTO);
    }

    List<ErrorImportDTO> getErrorList() {
        return errorList;
    }

    List<TrackImportDTO> getImportResult() {
        return importResult;
    }
}