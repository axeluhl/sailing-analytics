package com.sap.sailing.server.trackfiles.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.trackfiles.common.FormatNotSupportedException;

public class MetaDataRetriever implements DataRetriever {

    @Override
    public byte[] getData(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter, boolean rawFixes)
            throws FormatNotSupportedException, IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        if (race.getStartOfRace() != null)
            pw.println("START_OF_RACE\t" + f.format(race.getStartOfRace().asDate()));
        if (race.getEndOfRace() != null)
            pw.println("END_OF_RACE\t" + f.format(race.getEndOfRace().asDate()));
        if (race.getStartOfTracking() != null)
            pw.println("START_OF_TRACKING\t" + f.format(race.getStartOfTracking().asDate()));
        if (race.getEndOfTracking() != null)
            pw.println("END_OF_TRACKING\t" + f.format(race.getEndOfTracking().asDate()));
        pw.println();
        pw.flush();
        return out.toByteArray();
    }

}
