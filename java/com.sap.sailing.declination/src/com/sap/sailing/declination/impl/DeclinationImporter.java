package com.sap.sailing.declination.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.domain.common.Position;
import com.sap.sse.common.TimePoint;

public abstract class DeclinationImporter {
    private static final Logger logger = Logger.getLogger(DeclinationImporter.class.getName());
    
    public abstract Declination importRecord(Position position, TimePoint timePoint)
            throws IOException, ParserConfigurationException, SAXException;

    /**
     * Tries two things in parallel: fetch a more or less precise response from the online service and load
     * the requested year's declination values from a stored resource to look up a value that comes close.
     * The online lookup will be given preference. However, should it take longer than
     * <code>timeoutForOnlineFetchInMilliseconds</code>, then the method will return whatever it found
     * in the stored file, or <code>null</code> if no file exists for the year of <code>timePoint</code>.
     * 
     * @param timeoutForOnlineFetchInMilliseconds if 0, this means wait forever for the online result
     * @throws ParseException 
     * @throws ClassNotFoundException 
     * @throws IOException 
     */
    public Declination getDeclination(final Position position, final TimePoint timePoint,
            long timeoutForOnlineFetchInMilliseconds) throws IOException, ParseException {
        final Declination[] result = new Declination[1];
        Thread fetcher = new Thread("Declination fetcher for "+position+"@"+timePoint) {
            @Override
            public void run() {
                try {
                    Declination fetched = importRecord(position, timePoint);
                    synchronized (result) {
                        result[0] = fetched;
                        result.notifyAll();
                    }
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    logger.log(Level.FINE, "Exception while trying to load magnetic declination online", e);
                    synchronized (result) {
                        result.notifyAll(); // wake up waiter; no result will show up anymore
                    }
                }
            }
        };
        fetcher.start();
        synchronized (result) {
            if (result[0] == null) {
                try {
                    result.wait(timeoutForOnlineFetchInMilliseconds);
                } catch (InterruptedException e) {
                    // ignore; simply use value from file in this case
                }
            }
        }
        return result[0];
    }
}
