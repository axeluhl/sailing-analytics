package com.sap.sailing.domain.deckmanadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import com.sap.sailing.domain.deckmanadapter.DeckmanAdapter;
import com.sap.sailing.domain.deckmanadapter.Record;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class DeckmanGPSFixImporter implements GPSFixImporter {

    @Override
    public void importFixes(InputStream inputStream, Callback callback, boolean inferSpeedAndBearing)
            throws FormatNotSupportedException, IOException {
        DeckmanAdapter deckmanAdapter = DeckmanAdapterFactoryImpl.INSTANCE.createDeckmanAdapter();
        callback.startTrack(getType()+"@"+new Date(), /* properties */ null);
        for (Iterator<Record> i=deckmanAdapter.parseLogFile(new InputStreamReader(inputStream)); i.hasNext(); ) {
            Record record = i.next();
            callback.addFix(new GPSFixMovingImpl(record.getPosition(), record.getTimePoint(), record.getGpsFix().getSpeed()));
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
