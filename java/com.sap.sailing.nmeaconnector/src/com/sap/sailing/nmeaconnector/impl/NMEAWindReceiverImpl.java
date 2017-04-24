package com.sap.sailing.nmeaconnector.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.WindListener;
import com.sap.sailing.domain.tracking.impl.DynamicTrackImpl;
import com.sap.sailing.nmeaconnector.NMEAWindReceiver;
import com.sap.sailing.nmeaconnector.TimedBearing;

import net.sf.marineapi.nmea.event.AbstractSentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.MDASentence;
import net.sf.marineapi.nmea.sentence.MWDSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.VWRSentence;

public class NMEAWindReceiverImpl implements NMEAWindReceiver {
    private final ConcurrentHashMap<WindListener, WindListener> listeners;
    private final DynamicTrack<KnotSpeedWithBearingAndTimepoint> trueWindDirections;
    private final DynamicTrack<KnotSpeedWithBearingAndTimepoint> magneticWindDirections;
    private final DynamicTrack<GPSFix> sensorPositions;
    private final DynamicTrack<KnotSpeedWithBearingAndTimepoint> sensorSpeeds;
    private final DynamicTrack<TimedBearing> magneticHeadings;
    private final DynamicTrack<TimedBearing> trueHeadings;
    
    private class MWVSentenceListener extends AbstractSentenceListener<MWVSentence> {
        @Override
        public void sentenceRead(MWVSentence sentence) {
            // TODO Auto-generated method stub
        }
    }

    private class MWDSentenceListener extends AbstractSentenceListener<MWDSentence> {
        @Override
        public void sentenceRead(MWDSentence sentence) {
            // TODO Auto-generated method stub
        }
    }

    private class VWRSentenceListener extends AbstractSentenceListener<VWRSentence> {
        @Override
        public void sentenceRead(VWRSentence sentence) {
            // TODO Auto-generated method stub
        }
    }

    private class MDASentenceListener extends AbstractSentenceListener<MDASentence> {
        @Override
        public void sentenceRead(MDASentence sentence) {
            // TODO Auto-generated method stub
        }
    }

    private class HeadingSentenceListener extends AbstractSentenceListener<HeadingSentence> {
        @Override
        public void sentenceRead(HeadingSentence sentence) {
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
        this.trueWindDirections = new DynamicTrackImpl<>("trueWindDirection in "+getClass().getName());
        this.magneticWindDirections = new DynamicTrackImpl<>("magneticWindDirection in "+getClass().getName());
        this.sensorPositions = new DynamicTrackImpl<>("measurementPositions in "+getClass().getName());
        this.sensorSpeeds = new DynamicTrackImpl<>("sensorSpeeds in "+getClass().getName());
        this.magneticHeadings = new DynamicTrackImpl<>("headings in "+getClass().getName());
        this.trueHeadings = new DynamicTrackImpl<>("trueHeadings in "+getClass().getName());
        sentenceReader.addSentenceListener(new MWVSentenceListener(), SentenceId.MWV);
        sentenceReader.addSentenceListener(new MWDSentenceListener(), SentenceId.MWD);
        sentenceReader.addSentenceListener(new VWRSentenceListener(), SentenceId.VWR);
        sentenceReader.addSentenceListener(new MDASentenceListener(), SentenceId.MDA);
        sentenceReader.addSentenceListener(new HeadingSentenceListener());
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
