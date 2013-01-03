package com.sap.sailing.nmeaconnector.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.Sentence;

import org.junit.Test;

import slash.navigation.base.BaseRoute;
import slash.navigation.base.NavigationFormat;
import slash.navigation.base.NavigationFormatParser;
import slash.navigation.base.ParserResult;
import slash.navigation.gpx.Gpx10Format;
import slash.navigation.gpx.GpxPosition;
import slash.navigation.gpx.GpxRoute;

import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class ReadWindVaneDataTest {
    @Test
    public void testWindVaneFilePresence() throws IOException {
        InputStream is = getClass().getResourceAsStream("/windvane.nmea");
        assertNotNull(is);
        is.close();
    }
    
    @Test
    public void readWindVaneOutputWithInterlacedTimeStamps() throws IOException, InterruptedException {
        DateFormat df = new SimpleDateFormat("EE, dd. MMM yyyy HH:mm:ss", Locale.GERMANY);
        TimePoint lastTimestampFromFile = null;
        final List<Sentence> sentences = new ArrayList<>();
        final List<TimePoint> timePointsForSentences = new ArrayList<>();
        InputStream is = getClass().getResourceAsStream("/windvane.nmea");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);
        SentenceReader nmeaReader = new SentenceReader(pis);
        nmeaReader.addSentenceListener(new SentenceListener() {
            @Override
            public void sentenceRead(SentenceEvent event) {
                TimePoint timePoint = new MillisecondsTimePoint(event.getTimeStamp());
                Sentence sentence = event.getSentence();
                sentences.add(sentence);
                System.out.println("Time from file: "+timePointsForSentences.get(sentences.size()-1)+", Timestamp in sentence: "+timePoint+
                        ", Talker ID: "+sentence.getTalkerId()+", Sentence ID: "+sentence.getSentenceId());
            }
            @Override public void readingStopped() {}
            @Override public void readingStarted() {}
            @Override public void readingPaused() {}
        });
        nmeaReader.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line=br.readLine()) != null) {
            if (line.startsWith("$") || line.startsWith("!")) {
                // NMEA0183 sentence
                if (lastTimestampFromFile != null) {
                    lastTimestampFromFile = lastTimestampFromFile.plus(1000);
                    timePointsForSentences.add(lastTimestampFromFile);
                }
                pos.write(line.getBytes());
                pos.write(new byte[] { 13, 10 });
                pos.flush();
            } else {
                try {
                    // Maybe it's an interwoven timestamp. Try out...
                    Date date = df.parse(line);
                    final MillisecondsTimePoint newTimePointFromFile = new MillisecondsTimePoint(date);
                    if (lastTimestampFromFile != null && newTimePointFromFile.before(lastTimestampFromFile)) {
                        System.err.println("Surprise! Received more than five NMEA messages in five seconds at "+newTimePointFromFile);
                    }
                    lastTimestampFromFile = newTimePointFromFile;
                } catch (ParseException pe) {
                    pe.printStackTrace();
                }
            }
        }
        pos.close();
        br.close();
        Thread.sleep(3000);
        nmeaReader.stop();
    }
    
    @Test
    public void readGpxFile() throws IOException {
        ParserResult read = new NavigationFormatParser().read(getClass().getResource("/20121219114023.gpx"),
                Arrays.asList(new NavigationFormat[] { new Gpx10Format() }));
        @SuppressWarnings("rawtypes")
        List<BaseRoute> routes = read.getAllRoutes();
        assertNotNull(routes);
        assertEquals(1, routes.size());
        assertEquals(489, routes.get(0).getPositionCount());
        GpxRoute route = (GpxRoute) routes.get(0);
        List<GpxPosition> gpsPositions = route.getPositions();
        assertNotNull(gpsPositions);
        DynamicGPSFixMovingTrackImpl<Object> track = new DynamicGPSFixMovingTrackImpl<Object>("Wind Vane", /* millisecondsOverWhichToAverage */ 5000);
        for (GpxPosition gpxPosition : gpsPositions) {
            GPSFixMoving gpsFixMoving = new GPSFixMovingImpl(new DegreePosition(gpxPosition.getLatitude(), gpxPosition.getLongitude()),
                    new MillisecondsTimePoint(gpxPosition.getTime().getTimeInMillis()), new KilometersPerHourSpeedWithBearingImpl(
                            gpxPosition.getSpeed(), new DegreeBearingImpl(gpxPosition.getHeading())));
            track.addGPSFix(gpsFixMoving);
        }
        track.lockForRead();
        try {
            assertEquals(489, track.getRawFixes().size());
        } finally {
            track.unlockAfterRead();
        }
    }
}
