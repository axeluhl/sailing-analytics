package com.sap.sailing.nmeaconnector.test;

import java.util.concurrent.CountDownLatch;

import com.sap.sailing.nmeaconnector.NmeaFactory;

import net.sf.marineapi.nmea.event.AbstractSentenceListener;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.Sentence;

/**
 * Abstract test support class that adds a listener to a {@link SentenceReader} passed to the constructor. This listener
 * will react to the reading being {@link SentenceListener#readingPaused() paused and unblocks all calls to
 * {@link #waitUntilAllMessagesHaveBeenRead()}.
 * <p>
 * 
 * Subclasses can start the reading process by calling {@link #startReading()} which is shorthand for calling
 * {@link SentenceReader#start()} on the {@link SentenceReader} passed to the constructor.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SentenceReaderSupport {
    private final SentenceReader reader;
    private final CountDownLatch finishedLatch;

    protected SentenceReaderSupport(SentenceReader reader) {
        finishedLatch = new CountDownLatch(1);
        this.reader = reader;
        NmeaFactory.INSTANCE.getUtil().registerAdditionalParsers();
        reader.addSentenceListener(new AbstractSentenceListener<Sentence>() {
            @Override
            public void readingPaused() {
                finishedLatch.countDown();
            }

            @Override
            public void readingStopped() {
                finishedLatch.countDown();
            }

            @Override
            public void sentenceRead(Sentence sentence) {
            }
        });
        reader.setPauseTimeout(100);
    }
    
    protected void startReading() {
        reader.start();
    }
    
    protected void waitUntilAllMessagesHaveBeenRead() throws InterruptedException {
        finishedLatch.await();
    }
}
