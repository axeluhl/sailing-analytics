package com.sap.sailing.nmeaconnector.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.WindListener;
import com.sap.sailing.nmeaconnector.NMEAWindReceiver;
import com.sap.sailing.nmeaconnector.NmeaFactory;
import com.sap.sailing.nmeaconnector.NmeaUtil;

import net.sf.marineapi.nmea.event.AbstractSentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NmeaFactoryImpl implements NmeaFactory {

    @Override
    public NmeaUtil getUtil() {
        return new NmeaUtilImpl();
    }

    @Override
    public Iterable<Wind> readWind(InputStream inputStream) throws InterruptedException {
        final CountDownLatch finishedLatch = new CountDownLatch(1);
        final List<Wind> result = new ArrayList<>();
        final SentenceReader sentenceReader = new SentenceReader(inputStream);
        sentenceReader.addSentenceListener(new AbstractSentenceListener<Sentence>() {
            @Override
            public void readingStopped() {
                finishedLatch.countDown();
            }

            @Override
            public void sentenceRead(Sentence sentence) {}
        });
        final NMEAWindReceiver receiver = new NMEAWindReceiverImpl(sentenceReader);
        receiver.addWindListener(new WindListener() {
            @Override
            public void windDataReceived(Wind wind) {
                result.add(wind);
            }
            @Override public void windDataRemoved(Wind wind) {}
            @Override public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {}
        });
        sentenceReader.start();
        finishedLatch.await();
        return result;
    }
}
