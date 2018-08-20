package com.sap.sailing.nmeaconnector.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.sap.sailing.nmeaconnector.BATSentence;
import com.sap.sailing.nmeaconnector.BATSentence.WindVaneBatteryStatus;
import com.sap.sailing.nmeaconnector.NmeaFactory;
import com.sap.sailing.nmeaconnector.impl.BATParser;

import net.sf.marineapi.nmea.event.AbstractSentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class BATTest {

    @Rule public Timeout AbstractTracTracLiveTestTimeout = new Timeout(10 * 1000);

    public static final String EXAMPLE = "$WIBAT,1,5";
    BATSentence empty;
    BATSentence bat;

    @Before
    public void setUp() throws Exception {
        empty = new BATParser(TalkerId.WI);
        bat = new BATParser(EXAMPLE);
    }

    @Test
    public void testBATParser() {
        assertEquals(TalkerId.WI, empty.getTalkerId());
        assertEquals("BAT", empty.getSentenceId());
        assertEquals(2, empty.getFieldCount());
    }
    
    @Test
    public void testFindingBATParserThroughFactory() throws UnsupportedEncodingException, InterruptedException {
        NmeaFactory.INSTANCE.getUtil().registerAdditionalParsers();
        final BATSentence[] sentences = new BATSentence[1];
        SentenceReader reader = new SentenceReader(new ByteArrayInputStream(EXAMPLE.getBytes("UTF-8")));
        final Object monitor = new Object();
        final boolean[] paused = new boolean[1];
        reader.addSentenceListener(new AbstractSentenceListener<BATSentence>() {
            @Override
            public void sentenceRead(BATSentence sentence) {
                sentences[0] = sentence;
            }

            @Override
            public void readingPaused() {
                super.readingPaused();
                setPausedAndTriggerWaiters();
            }

            private void setPausedAndTriggerWaiters() {
                synchronized (monitor) {
                    paused[0] = true;
                    monitor.notifyAll();
                }
            }

            /**
             * When reading stops without having been paused before, e.g., because the stream was
             * exhausted and closed, we will also set the {@code paused[0]} value to true and notify
             * waiters.
             */
            @Override
            public void readingStopped() {
                super.readingStopped();
                setPausedAndTriggerWaiters();
            }
        });
        reader.setPauseTimeout(100);
        reader.start();
        synchronized (monitor) {
            while (!paused[0]) {
                monitor.wait(1000);
            }
        }
        assertNotNull(sentences[0]);
        assertEquals(WindVaneBatteryStatus.GOOD, sentences[0].getWindVaneBatteryStatus());
    }

    @Test
    public void testBATParserString() {
        assertEquals(TalkerId.WI, bat.getTalkerId());
        assertEquals("BAT", bat.getSentenceId());
        assertEquals(2, bat.getFieldCount());
    }

    @Test
    public void testGetBaseUnitBatteryLevel() {
        assertEquals(5, bat.getBaseUnitBatteryLevel());
    }

    @Test
    public void testWindVaneBatteryStatus() {
        assertEquals(WindVaneBatteryStatus.GOOD, bat.getWindVaneBatteryStatus());
    }
}
