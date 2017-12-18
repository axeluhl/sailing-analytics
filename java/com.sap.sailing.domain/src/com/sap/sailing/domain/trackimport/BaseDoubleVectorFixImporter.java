package com.sap.sailing.domain.trackimport;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;

/**
 * Definition of importers used by SensorDataImportServlet to do the import of a specific file type.
 */
public interface BaseDoubleVectorFixImporter {    
    public interface Callback {
        void addFixes(Iterable<DoubleVectorFix> fixes, TrackFileImportDeviceIdentifier device);
    }

    /**
     * Parses the uploaded file and calls the callback for every fix that's contained in the input.
     * 
     * @param inputStream
     *            the uploaded file as {@link InputStream}
     * @param callback
     *            the callback to call for every new fix
     * @param sourceName
     *            the uploaded file's name. This can be used to identify the file type if the importer can import
     *            different formats.
     * @param downsample
     *            whether or not the importer shall reduce the sampling frequency during import; if {@code false}, all
     *            fixes read from the {@code inputStream} will be forwarded to the {@code callback}; otherwise, multiple
     *            fixes may be averaged, smoothened and then forwarded as only one combined fix to the {@code callback}.
     * @throws FormatNotSupportedException
     *             if the uploaded file can't be parsed by the importer
     * @throws IOException
     *             if there is a problem while reading the file
     */
    boolean importFixes(InputStream inputStream, Callback callback, String filename, String sourceName,
            boolean downsample)
            throws FormatNotSupportedException, IOException;

    /**
     * The type is a String that's presented to the user for selection of the type when doing an import.
     * The type is used on import to identify the importer to use depending on the user's selection.
     * 
     * @return name of the file type to be imported by the importer
     */
    String getType();
}
