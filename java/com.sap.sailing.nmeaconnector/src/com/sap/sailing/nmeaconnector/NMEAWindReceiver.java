package com.sap.sailing.nmeaconnector;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.WindListener;
import com.sap.sse.common.TimePoint;

import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.DateSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TimeSentence;

/**
 * A stateful receiver that consumes sentences from an NMEA {@link SentenceReader}
 * for which it acts as a {@link SentenceListener} for sentences providing information
 * about wind measurements, date/time reference, location of measurements and data
 * that may be required to convert apparent wind speeds and angles into true wind
 * speeds and directions. Clients can {@link #addWindListener(WindListener) register}
 * as a {@link WindListener} and receive {@link WindListener#windDataReceived} callbacks
 * whenever a {@link Wind} fix was assembled out of the various NMEA sentences.<p>
 * 
 * The NMEA 0183 protocol specifies that a {@link SentenceId#MWV} sentence may provide
 * "true" (relative to true north) or "relative" (relative to bow) wind angles. However,
 * it is not entirely clear in all cases if those angles have the magnetic declination
 * already corrected. Only with {@link SentenceId#MWD} sentences does this become clear,
 * and a value for the magnetic declination could be inferred. For now, we will assume
 * that {@link SentenceId#MWV} values really provide angles based on true north if
 * {@link MWVSentence#isTrue()} is {@code true}.<p>
 * 
 * This receiver assumes that the sentences do not necessarily have to be entirely
 * time-synchronous. But different sentence types can provide "hints" as to the timing.
 * GPS fixes (@link SentenceId#GGA} and other position fixes {@link SentenceId#GLL} have
 * time of day but no date information. Other messages such as {@link SentenceId#RMC} and
 * {@link SentenceId#ZDA} contain date and optionally time zone information. When analyzing
 * the entire sequence, under certain assumptions such as fixes with time information being
 * less than 24h apart from each other and with at least one {@link DateSentence} per day
 * it is possible to combine {@link TimeSentence}s with the last date information into
 * a full {@link TimePoint}.<p>
 * 
 * In order to also work in streaming mode and to provide {@link Wind} fixes as soon as
 * all data relevant for a wind fix has been received, this receiver won't wait for the
 * next timing information to interpolate, but instead the last timing data received
 * before the wind measurement will be used for the {@link Wind} fix.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface NMEAWindReceiver {
    void addWindListener(WindListener listener);
    
    void removeWindListener(WindListener listener);
}
