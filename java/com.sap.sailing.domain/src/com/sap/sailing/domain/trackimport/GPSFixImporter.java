package com.sap.sailing.domain.trackimport;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.sap.sailing.domain.tracking.GPSFix;

/**
 * Importer for extracting GPS fixes from an InputStream. Instances are picked up via
 * the OSGi service registry.
 * 
 * @author Fredrik Teschke
 *
 */
public interface GPSFixImporter {    
    String FILE_EXTENSION_PROPERTY = "fileExt";
    
    /**
     * Callback through which fixes and track metadata found in the source stream
     * are passed back.
     * @author Fredrik Teschke
     *
     */
    interface Callback {
        /**
         * Is called when the beginning of a track is found. Some track file formats (e.g. GPX)
         * can contain multiple tracks.
         * Implementations of the {@link GPSFixImporter} are expected to call this method before
         * calling {@link #addFix}, even if no metadata is known (and then pass {@code null} values
         * for the {@code name} and {@code properties}).
         */
        void startTrack(String name, Map<String, String> properties);
        void addFix(GPSFix fix);
    }
    
    /**
     * Retrieves the fixes from the {@code inputStream}, and calls the
     * {@code callback} with every new fix.
     * @param inferSpeedAndBearing Should speed and bearing be inferred by looking
     * at the previous fix, if that data is not directly present within the file?
     * @throws FormatNotSupportedException If the input format cannot be read. The import process
     * might then decide to try attempt importing fixes using the next suitable importer.
     */
    void importFixes(InputStream inputStream, Callback callback, boolean inferSpeedAndBearing)
            throws FormatNotSupportedException, IOException;

    /**
     * Return the file extensions supported by this importer. If the importer is not intended
     * for file-based input, this may return an empty iterable.
     * @return
     */
    Iterable<String> getSupportedFileExtensions();
    
    /**
     * Return a unique type name, that should be human readable.
     * @return
     */
    String getType();
}
