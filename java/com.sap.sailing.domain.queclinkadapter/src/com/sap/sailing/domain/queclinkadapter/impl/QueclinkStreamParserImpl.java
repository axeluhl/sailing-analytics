package com.sap.sailing.domain.queclinkadapter.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.sap.sailing.domain.queclinkadapter.Message;
import com.sap.sse.common.TimePoint;

/**
 * Parser utility for Queclink GL 300 data streams. The GL 300 is assumed to send a message stream over some connection,
 * usually a UDP or TCP connection, but theoretically also through one or more SMS text messages. The messages consist
 * of printable ASCII characters, introduced by a message type, and terminated by the special termination character "$"
 * (dollar sign).<p>
 * 
 * This parser utility can read and parse those messages into objects of type {@link Message}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class QueclinkStreamParserImpl {
    private static final SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddhhmmss");
    
    static TimePoint parseTimeStamp(String yyyyMMddHHMMSS) throws ParseException {
        synchronized (timeStampFormat) {
            return TimePoint.of(new SimpleDateFormat("yyyyMMddhhmmss").parse(yyyyMMddHHMMSS));
        }
    }

    static String formatAsYYYYMMDDHHMMSS(TimePoint timePoint) {
        synchronized (timeStampFormat) {
            return timeStampFormat.format(timePoint.asDate());
        }
    }

    static String formatProtocolVersionHex(int protocolVersion) {
        return String.format("%06X", protocolVersion);
    }
    
    static String formatCountNumberHex(short countNumber) {
        return String.format("%04X", countNumber);
    }

    public Message parse(String messageIncludingTailCharacter) {
        // TODO Auto-generated method stub
        return null;
    }
}
