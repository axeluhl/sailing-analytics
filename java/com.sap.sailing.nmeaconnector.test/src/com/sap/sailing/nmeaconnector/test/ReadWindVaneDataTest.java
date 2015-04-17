package com.sap.sailing.nmeaconnector.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.BATSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.sentence.BATSentence.WindVaneBatteryStatus;

import org.junit.Test;

import slash.navigation.base.BaseRoute;
import slash.navigation.base.NavigationFormat;
import slash.navigation.base.NavigationFormatParser;
import slash.navigation.base.ParserResult;
import slash.navigation.gpx.Gpx10Format;
import slash.navigation.gpx.GpxPosition;
import slash.navigation.gpx.GpxRoute;

import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.WindComparator;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.nmeaconnector.NmeaFactory;
import com.sap.sailing.nmeaconnector.NmeaUtil;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ReadWindVaneDataTest {
    @Test
    public void testWindVaneFilePresence() throws IOException {
        InputStream is = getClass().getResourceAsStream("/windvane.nmea");
        assertNotNull(is);
        is.close();
    }
    
    @Test
    public void readWindVaneOutputWithInterlacedTimeStamps() throws IOException, InterruptedException {
        final List<Sentence> sentences = new ArrayList<>();
        final List<TimePoint> timePointsForSentences = new ArrayList<>();
        readWindVaneNmeaFile(sentences, timePointsForSentences);
        assertEquals(674, sentences.size()); // we have 684 sentences altogether; 74 of those are WIBAT which are now understood;
                                             // 10 messages (one of them a WIBAT) have no timestamp yet
    }
    
    @Test
    public void findProprietaryBatteryMessages() throws IOException, InterruptedException {
        final List<Sentence> sentences = new ArrayList<>();
        final List<TimePoint> timePointsForSentences = new ArrayList<>();
        readWindVaneNmeaFile(sentences, timePointsForSentences);
        boolean found = false;
        for (Sentence sentence : sentences) {
            if (sentence.getTalkerId() == TalkerId.WI) {
                if (sentence.getSentenceId().equals("BAT")) {
                    found = true;
                    assertTrue(sentence instanceof BATSentence);
                    assertEquals(WindVaneBatteryStatus.GOOD, ((BATSentence) sentence).getWindVaneBatteryStatus());
                    assertEquals(5, ((BATSentence) sentence).getBaseUnitBatteryLevel());
                }
            }
        }
        assertTrue("Expected to find BAT message", found);
    }

    private void readWindVaneNmeaFile(final List<Sentence> sentences, final List<TimePoint> timePointsForSentences)
            throws IOException, InterruptedException {
        NmeaUtil nmeaUtil = NmeaFactory.INSTANCE.getUtil();
        DateFormat df = new SimpleDateFormat("EE, dd. MMM yyyy HH:mm:ss", Locale.GERMANY);
        TimePoint lastTimestampFromFile = null;
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
                // Special hack for a bug with the WindVane's speed unit output which erroneously uses "K" for "knots":
                line = nmeaUtil.replace(line, ",K,", ",N,");
                if (lastTimestampFromFile != null) {
                    lastTimestampFromFile = lastTimestampFromFile.plus(1000);
                    timePointsForSentences.add(lastTimestampFromFile);
                    // write the sentence only if we have a time stamp for it
                    pos.write(line.getBytes());
                    pos.write(new byte[] { 13, 10 });
                    pos.flush();
                }
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
        DynamicGPSFixMovingTrackImpl<Object> track = new DynamicGPSFixMovingTrackImpl<Object>("Wind Vane", /* millisecondsOverWhichToAverage */ 5000);
        loadGpxFileIntoTrack(track);
        track.lockForRead();
        try {
            assertEquals(489, track.getRawFixes().size());
        } finally {
            track.unlockAfterRead();
        }
    }
    
    @Test
    public void readWindVaneNmeaIntoWindTrack() throws IOException, InterruptedException {
        NmeaUtil nmeaUtil = NmeaFactory.INSTANCE.getUtil();
        final List<Sentence> sentences = new ArrayList<>();
        DynamicGPSFixMovingTrackImpl<Object> gpsTrack = new DynamicGPSFixMovingTrackImpl<Object>("Wind Vane", /* millisecondsOverWhichToAverage */ 5000);
        loadGpxFileIntoTrack(gpsTrack);
        final List<TimePoint> timePointsForSentences = new ArrayList<>();
        WindTrack windTrack = new WindTrackImpl(/* millisecondsOverWhichToAverage */ 5000, /* useSpeed */ true,
                /* nameForReadWriteLock */ "readWindVaneNmeaIntoWindTrack");
        readWindVaneNmeaFile(sentences, timePointsForSentences);
        Iterator<TimePoint> timePointIter = timePointsForSentences.iterator();
        for (Sentence sentence : sentences) {
            TimePoint timePoint = timePointIter.next();
            Position position = gpsTrack.getEstimatedPosition(timePoint, /* extrapolate */ true);
            if (SentenceId.MWV == SentenceId.valueOf(sentence.getSentenceId())) {
                MWVSentence mwvSentence = (MWVSentence) sentence;
                final Wind wind = nmeaUtil.getWind(timePoint, position, mwvSentence);
                Wind oldWindAtOrAfter = windTrack.getFirstFixAtOrAfter(timePoint);
                if (oldWindAtOrAfter != null && WindComparator.INSTANCE.compare(oldWindAtOrAfter, wind) == 0) {
                    System.err.println("Surprise: two wind fixes for the same time point and the same position. Old: "
                            + oldWindAtOrAfter + ". New: " + wind);
                }
                windTrack.add(wind);
            }
        }
        windTrack.lockForRead();
        try {
            assertEquals(601, Util.size(windTrack.getRawFixes()));
            System.out.println("\n\nTimePoint"+"\t"
                              +"AWS/kts"+"\t"
                              +"AWA"+"\t"
                              +"SOG/kts"+"\t"
                              +"COG");
            for (Wind rawWind : windTrack.getRawFixes()) {
                final SpeedWithBearing estimatedSpeed = gpsTrack.getEstimatedSpeed(rawWind.getTimePoint());
                System.out.println(rawWind.getTimePoint()+"\t"
                                  +rawWind.getKnots()+"\t"
                                  +rawWind.getBearing().getDegrees()+"\t"
                                  +estimatedSpeed.getKnots()+"\t"
                                  +estimatedSpeed.getBearing().getDegrees());
            }
        } finally {
            windTrack.unlockAfterRead();
        }
    }

    private void loadGpxFileIntoTrack(DynamicGPSFixMovingTrackImpl<Object> track) throws IOException {
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
        for (GpxPosition gpxPosition : gpsPositions) {
            GPSFixMoving gpsFixMoving = new GPSFixMovingImpl(new DegreePosition(gpxPosition.getLatitude(), gpxPosition.getLongitude()),
                    new MillisecondsTimePoint(gpxPosition.getTime().getTimeInMillis()), new KilometersPerHourSpeedWithBearingImpl(
                            gpxPosition.getSpeed(), new DegreeBearingImpl(gpxPosition.getHeading())));
            track.addGPSFix(gpsFixMoving);
        }
    }
}
