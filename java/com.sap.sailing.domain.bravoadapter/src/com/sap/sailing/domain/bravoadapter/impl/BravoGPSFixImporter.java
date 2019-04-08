package com.sap.sailing.domain.bravoadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.sensordata.BravoExtendedSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.server.gateway.windimport.bravo.FunnyDegreeConverter;
import com.sap.sailing.server.trackfiles.impl.BravoExtendedDataImporterImpl;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class BravoGPSFixImporter implements GPSFixImporter {
    @Override
    public boolean importFixes(InputStream inputStream, Callback callback, boolean inferSpeedAndBearing, final String filename)
            throws FormatNotSupportedException, IOException {
        final AtomicBoolean importedFixes = new AtomicBoolean(false);
        TrackFileImportDeviceIdentifier device = new TrackFileImportDeviceIdentifierImpl(filename, getType() + "@" + new Date());
        new BravoExtendedDataImporterImpl().importFixes(inputStream,
                (Iterable<DoubleVectorFix> fixes, TrackFileImportDeviceIdentifier deviceIdentifier)->{
                    for (final DoubleVectorFix fix : fixes) {
                        GPSFixMoving gpsFix = new GPSFixMovingImpl(
                                new DegreePosition(FunnyDegreeConverter.funnyLatLng(fix.get(BravoExtendedSensorDataMetadata.LAT.getColumnIndex())),
                                        FunnyDegreeConverter.funnyLatLng(fix.get(BravoExtendedSensorDataMetadata.LON.getColumnIndex()))),
                                fix.getTimePoint(),
                                new KnotSpeedWithBearingImpl(fix.get(BravoExtendedSensorDataMetadata.SOG.getColumnIndex()),
                                        new DegreeBearingImpl(fix.get(BravoExtendedSensorDataMetadata.COG.getColumnIndex()))));
                        callback.addFix(gpsFix, device);
                        importedFixes.set(true);
                    }
                }, filename, /* sourceName */ getType(), /* downsample */ false);
        return importedFixes.get();
    }

    @Override
    public Iterable<String> getSupportedFileExtensions() {
        return Arrays.asList(new String[] { "csv", "log", "txt" });
    }

    @Override
    public String getType() {
        return "Bravo";
    }
}
