package com.sap.sailing.domain.queclinkadapter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.queclinkadapter.HeartbeatAcknowledgement;
import com.sap.sailing.domain.queclinkadapter.Message;
import com.sap.sailing.domain.queclinkadapter.MessageType;
import com.sap.sailing.domain.queclinkadapter.MessageType.Direction;
import com.sap.sse.common.TimePoint;

public class TestQueclinkMessageParser {
    private QueclinkStreamParserImpl messageParser;

    @Before
    public void setUp() {
        this.messageParser = new QueclinkStreamParserImpl();
    }
    
    @Test
    public void testSimpleMessageString() {
        final TimePoint now = TimePoint.now();
        final Message ackHBD = new HeartbeatAcknowledgementImpl(Integer.parseInt("301303", 16), "860599004785994", null, now, Short.parseShort("033E", 16));
        final String ackHBDAsString = ackHBD.getMessageString();
        assertEquals("+ACK:GTHBD,301303,860599004785994,,"+QueclinkStreamParserImpl.formatAsYYYYMMDDHHMMSS(now)+",033E$", ackHBDAsString);
    }
    
    @Test
    public void testParsingSimpleHeartbeatAck() {
        final TimePoint now = TimePoint.now();
        final Message ackHBD = new HeartbeatAcknowledgementImpl(Integer.parseInt("301303", 16), "860599004785994", null, now, Short.parseShort("033E", 16));
        final String ackHBDAsString = ackHBD.getMessageString();
        final Message parsedMessage = messageParser.parse(ackHBDAsString);
        assertNotNull(parsedMessage);
        assertEquals(MessageType.HBD, parsedMessage.getType());
        assertEquals(Direction.ACK, parsedMessage.getDirection());
        assertTrue(parsedMessage instanceof HeartbeatAcknowledgement);
    }
}
