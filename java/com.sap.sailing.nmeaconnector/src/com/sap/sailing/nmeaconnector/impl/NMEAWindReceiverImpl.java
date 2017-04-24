package com.sap.sailing.nmeaconnector.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.WindListener;
import com.sap.sailing.nmeaconnector.NMEAWindReceiver;

import net.sf.marineapi.nmea.event.AbstractSentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;

public class NMEAWindReceiverImpl implements NMEAWindReceiver {
    private final ConcurrentHashMap<WindListener, WindListener> listeners;
    
    private class MWVSentenceListener extends AbstractSentenceListener<MWVSentence> {
        @Override
        public void sentenceRead(MWVSentence sentence) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * @param sentenceReader
     *            The reader that this receiver will listen to for those NMEA sentences of interest for the construction
     *            of wind fixes
     */
    public NMEAWindReceiverImpl(SentenceReader sentenceReader) {
        super();
        this.listeners = new ConcurrentHashMap<>();
        sentenceReader.addSentenceListener(new MWVSentenceListener(), SentenceId.MWV);
    }

    @Override
    public void addWindListener(WindListener listener) {
        listeners.put(listener, listener);
    }

    @Override
    public void removeWindListener(WindListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Wind wind) {
        for (WindListener listener : listeners.values()) {
            listener.windDataReceived(wind);
        }
    }
}
