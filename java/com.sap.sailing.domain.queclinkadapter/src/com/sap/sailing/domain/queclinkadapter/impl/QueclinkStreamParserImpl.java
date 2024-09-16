package com.sap.sailing.domain.queclinkadapter.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.domain.queclinkadapter.Message;
import com.sap.sailing.domain.queclinkadapter.MessageFactory;
import com.sap.sailing.domain.queclinkadapter.MessageParser;
import com.sap.sailing.domain.queclinkadapter.MessageType;
import com.sap.sailing.domain.queclinkadapter.MessageType.Direction;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

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
public class QueclinkStreamParserImpl implements MessageParser {
    private final static Logger logger = Logger.getLogger(QueclinkStreamParserImpl.class.getName());
    private static final SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddhhmmss");
    static final Pattern messagePattern = Pattern.compile("((AT\\+)|(\\+ACK:)|(\\+RESP:)|(\\+SACK:))(GT([A-Z]{3})[=,])?(.*)\\$");
    
    static TimePoint parseTimeStamp(String yyyyMMddHHMMSS) throws ParseException {
        final TimePoint timeStamp;
        if (!Util.hasLength(yyyyMMddHHMMSS)) {
            timeStamp = null;
        } else {
            synchronized (timeStampFormat) {
                timeStamp = TimePoint.of(new SimpleDateFormat("yyyyMMddhhmmss").parse(yyyyMMddHHMMSS));
            }
        }
        return timeStamp;
    }

    static String formatAsYYYYMMDDHHMMSS(TimePoint timePoint) {
        final String result;
        if (timePoint == null) {
            result = "";
        } else {
            synchronized (timeStampFormat) {
                result = timeStampFormat.format(timePoint.asDate());
            }
        }
        return result;
    }
    
    static int parseProtocolVersionHex(String protocolVersion) {
        return Integer.parseInt(protocolVersion, 16);
    }

    static short parseCountNumberHex(String countNumber) {
        return Short.parseShort(countNumber, 16);
    }
    
    static String formatProtocolVersionHex(int protocolVersion) {
        return String.format("%06X", protocolVersion);
    }
    
    static String formatCountNumberHex(short countNumber) {
        return String.format("%04X", countNumber);
    }

    @Override
    public Message parse(String messageIncludingTailCharacter) throws ParseException {
        final Matcher matcher = messagePattern.matcher(messageIncludingTailCharacter);
        if (!matcher.matches()) {
            throw new ParseException("Message "+messageIncludingTailCharacter+" cannot be parsed as Queclink message", 0);
        }
        final String directionStringWithPrefixAndSuffix = matcher.group(1); // e.g., "+ACK:"
        final Direction direction = Direction.fromMessageStart(directionStringWithPrefixAndSuffix);
        final String messageTypeString = matcher.group(7); // e.g., "HBD"; may be null for +SACK: messages
        final MessageType messageType = messageTypeString == null ? null : MessageType.valueOf(messageTypeString);
        final String[] parameters = matcher.group(8).split(",");
        final Message result;
        final MessageFactory messageFactory = MessageType.getMessageFactory(direction, messageType);
        if (messageFactory == null) {
            logger.warning("Couldn't find a message factory for message type "+messageType+" and direction "+direction);
        }
        result = messageFactory == null ? null : messageFactory.createMessageWithParameters(parameters);
        return result;
    }
}
