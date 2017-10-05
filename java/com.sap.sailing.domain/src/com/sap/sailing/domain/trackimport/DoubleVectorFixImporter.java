package com.sap.sailing.domain.trackimport;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sse.common.TimePoint;

/**
 * Definition of importers used by SensorDataImportServlet to do the import of a specific file type.
 */
public interface DoubleVectorFixImporter {    

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
     * @param downsample TODO
     * @throws FormatNotSupportedException
     *             if the uploaded file can't be parsed by the importer
     * @throws IOException
     *             if there is a problem while reading the file
     */
    void importFixes(InputStream inputStream, Callback callback, String filename, String sourceName, boolean downsample)
            throws FormatNotSupportedException, IOException;

    /**
     * The type is a String that's presented to the user for selection of the type when doing an import.
     * The type is used on import to identify the importer to use depending on the user's selection.
     * 
     * @return name of the file type to be imported by the importer
     */
    String getType();
    
    /**
     * Creates the {@link RegattaLogEvent} for the DeviceMapping.
     */
    RegattaLogDeviceCompetitorSensorDataMappingEvent createEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Competitor mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to);
    
}
