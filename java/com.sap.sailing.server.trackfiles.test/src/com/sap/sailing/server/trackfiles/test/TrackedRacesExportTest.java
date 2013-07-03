package com.sap.sailing.server.trackfiles.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.server.trackfiles.common.FormatNotSupportedException;
import com.sap.sailing.server.trackfiles.impl.ExportImpl;

public class TrackedRacesExportTest extends OnlineTracTracBasedTest {

    public TrackedRacesExportTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Override
    protected String getExpectedEventName() {
        return "49er European Championship";
    }

    @Before
    public void setUp() throws URISyntaxException, IOException, InterruptedException {
        super.setUp();
        super.setUp("event_20120905_erEuropean",
        /* raceId */"03fa908e-fc03-11e1-9150-10bf48d758ce", new ReceiverType[] { ReceiverType.MARKPOSITIONS,
                ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS, ReceiverType.MARKPASSINGS });
    }

    @Test
    public void isSingleGpxCreated() throws FileNotFoundException, FormatNotSupportedException, IOException {
        TrackedRace race = getTrackedRace();

//        File file = File.createTempFile("gpxexport", ".gpx");
//        FileOutputStream out = new FileOutputStream(file);
        byte[] data = ExportImpl.INSTANCE.writeCompetitors(TrackFilesFormat.Gpx11, race, true, true);
//        out.close();

//        assertTrue(file.exists());
        assertTrue(data.length > 0);

//        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
        String secondLine = in.readLine();
        in.close();
        assertTrue(secondLine.contains("<gpx"));
    }

    @Test
    public void doAllFormatsWork() throws IOException, FormatNotSupportedException {
        TrackedRace race = getTrackedRace();

        for (TrackFilesFormat format : TrackFilesFormat.values()) {
//            File file = File.createTempFile(format.toString() + "export", "." + format.suffix);
//            FileOutputStream out = new FileOutputStream(file);
            byte[] data = ExportImpl.INSTANCE.writeCompetitors(format, race, true, true);
//            out.close();

//            assertTrue(file.exists());
            assertTrue(data.length > 0);
        }
    }
}
