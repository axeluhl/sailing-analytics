package com.sap.sailing.nmeaconnector.test;

import static org.junit.Assert.assertFalse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.sf.marineapi.nmea.event.AbstractSentenceListener;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.util.Position;

public class GeneralizedSentenceListenerTest {
    private SentenceReader sentenceReader;
    private SentenceReaderSupport readerSupport;

    @Before
    public void setUp() throws FileNotFoundException {
        sentenceReader = new SentenceReader(new FileInputStream("resources/LogSS.txt"));
        readerSupport = new SentenceReaderSupport(sentenceReader);
    }
    
    @Test
    public void testRegisterPositionListener() throws InterruptedException {
        final List<Position> positions = new ArrayList<>();
        SentenceListener positionListener = new AbstractSentenceListener<PositionSentence>() {
            @Override
            public void sentenceRead(PositionSentence sentence) {
                try {
                    final Position position = sentence.getPosition();
                    positions.add(position);
                } catch (DataNotAvailableException e) {
                    // this can happen and simply means we don't have a position; so we
                    // can safely ignore this exception
                }
            }
        };
        sentenceReader.addSentenceListener(positionListener);
        readerSupport.startReading();
        readerSupport.waitUntilAllMessagesHaveBeenRead();
        assertFalse(positions.isEmpty());
    }
}
