package com.sap.sailing.nmeaconnector.test;

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
import java.util.Date;
import java.util.Locale;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.Sentence;

import org.junit.Test;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;

public class ReadWindVaneDataTest {
    @Test
    public void testWindVaneFilePresence() throws IOException {
        InputStream is = getClass().getResourceAsStream("/windvane.nmea");
        assertNotNull(is);
        is.close();
    }
    
    @Test
    public void readWindVaneOutputWithInterlacedTimeStamps() throws IOException, InterruptedException {
        DateFormat df = new SimpleDateFormat("EE, dd. MMM yyyy hh:mm:ss", Locale.GERMANY);
        final TimePoint[] lastTimestampFromFile = new TimePoint[1];
        InputStream is = getClass().getResourceAsStream("/windvane.nmea");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);
        SentenceReader nmeaReader = new SentenceReader(pis);
        nmeaReader.addSentenceListener(new SentenceListener() {
            @Override
            public void sentenceRead(SentenceEvent event) {
                TimePoint timePoint = new MillisecondsTimePoint(event.getTimeStamp());
                Sentence sentence = event.getSentence();
                System.out.println("Last timestamp from file: "+lastTimestampFromFile[0]+", Timestamp in sentence: "+timePoint+
                        ", Talker ID: "+sentence.getTalkerId()+", Sentence ID: "+sentence.getSentenceId());
            }
            
            @Override
            public void readingStopped() {
            }
            
            @Override
            public void readingStarted() {
            }
            
            @Override
            public void readingPaused() {
            }
        });
        nmeaReader.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line=br.readLine()) != null) {
            if (line.startsWith("$") || line.startsWith("!")) {
                // NMEA0183 sentence
                pos.write(line.getBytes());
                pos.write(new byte[] { 13, 10 });
                pos.flush();
                if (lastTimestampFromFile[0] != null) {
                    lastTimestampFromFile[0] = lastTimestampFromFile[0].plus(1000);
                }
            } else {
                try {
                    // Maybe it's an interwoven timestamp. Try out...
                    Date date = df.parse(line);
                    lastTimestampFromFile[0] = new MillisecondsTimePoint(date);
                } catch (ParseException pe) {
                    pe.printStackTrace();
                }
            }
        }
        pos.close();
        br.close();
        Thread.sleep(10000);
        nmeaReader.stop();
    }
}
