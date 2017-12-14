package com.sap.sailing.domain.deckmanadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.deckmanadapter.DeckmanAdapter;
import com.sap.sailing.domain.deckmanadapter.Record;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;

public class DeckmanGPSFixImporter implements GPSFixImporter {

    @Override
    public boolean importFixes(InputStream inputStream, Callback callback, boolean inferSpeedAndBearing,
            String sourceName)
            throws FormatNotSupportedException, IOException {
        DeckmanAdapter deckmanAdapter = DeckmanAdapterFactoryImpl.INSTANCE.createDeckmanAdapter();
        TrackFileImportDeviceIdentifier device = new TrackFileImportDeviceIdentifierImpl(sourceName, getType() + "@" + new Date());
        final AtomicBoolean importedFixes = new AtomicBoolean(false);
        try {
            for (Iterator<Record> i = deckmanAdapter.parseLogFile(new InputStreamReader(inputStream)); i.hasNext();) {
                Record record = i.next();
                callback.addFix(new GPSFixMovingImpl(record.getPosition(), record.getTimePoint(), record.getGpsFix().getSpeed()),
                        device);
                importedFixes.set(true);
            }
            return importedFixes.get();
        } catch (RuntimeException e) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                } else {
                    throw new FormatNotSupportedException("Error reading Deckman CSV file", e.getCause());
                }
            } else {
                throw new FormatNotSupportedException("Error reading Deckman CSV file", e);
            }
        }
    }

    @Override
    public Iterable<String> getSupportedFileExtensions() {
        return Arrays.asList(new String[] { "csv" });
    }

    @Override
    public String getType() {
        return "Deckman";
    }

}
