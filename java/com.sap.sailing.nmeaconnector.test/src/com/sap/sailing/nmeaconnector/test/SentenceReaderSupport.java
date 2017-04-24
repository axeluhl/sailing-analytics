package com.sap.sailing.nmeaconnector.test;

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
    private final Object monitor;
    private final boolean[] paused;
    private final SentenceReader reader;

    protected SentenceReaderSupport(SentenceReader reader) {
        this.paused = new boolean[1];
        this.reader = reader;
        this.monitor = new Object();
        NmeaFactory.INSTANCE.getUtil().registerAdditionalParsers();
        reader.addSentenceListener(new AbstractSentenceListener<Sentence>() {
            @Override
            public void readingPaused() {
                synchronized (monitor) {
                    super.readingPaused();
                    paused[0] = true;
                    monitor.notifyAll();
                }
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
        synchronized (monitor) {
            while (!paused[0]) {
                monitor.wait(1000);
            }
        }
    }
}
