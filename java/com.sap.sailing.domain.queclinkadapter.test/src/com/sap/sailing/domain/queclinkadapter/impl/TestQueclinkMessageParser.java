package com.sap.sailing.domain.queclinkadapter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.regex.Matcher;

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
        final Message ackHBD = new HeartbeatAcknowledgementImpl(QueclinkStreamParserImpl.parseProtocolVersionHex("301303"), "860599004785994", null, now, QueclinkStreamParserImpl.parseCountNumberHex("033E"));
        final String ackHBDAsString = ackHBD.getMessageString();
        assertEquals("+ACK:GTHBD,301303,860599004785994,,"+QueclinkStreamParserImpl.formatAsYYYYMMDDHHMMSS(now)+",033E$", ackHBDAsString);
    }
    
    @Test
    public void testParsingSimpleHeartbeatAck() throws ParseException {
        final TimePoint now = TimePoint.now();
        final Message ackHBD = new HeartbeatAcknowledgementImpl(QueclinkStreamParserImpl.parseProtocolVersionHex("301303"), "860599004785994", null, now, QueclinkStreamParserImpl.parseCountNumberHex("033E"));
        final String ackHBDAsString = ackHBD.getMessageString();
        final Message parsedMessage = messageParser.parse(ackHBDAsString);
        assertNotNull(parsedMessage);
        assertEquals(MessageType.HBD, parsedMessage.getType());
        assertEquals(Direction.ACK, parsedMessage.getDirection());
        assertTrue(parsedMessage instanceof HeartbeatAcknowledgement);
    }
    
    @Test
    public void testParsingSimpleHeartbeatSack() throws ParseException {
        final Message sackHBD = new HeartbeatServerAcknowledgementImpl(QueclinkStreamParserImpl.parseProtocolVersionHex("301303"), QueclinkStreamParserImpl.parseCountNumberHex("033E"));
        final String sackHBDAsString = sackHBD.getMessageString();
        final Message parsedMessage = messageParser.parse(sackHBDAsString);
        assertNotNull(parsedMessage);
        assertEquals(MessageType.HBD, parsedMessage.getType());
        assertEquals(Direction.ACK, parsedMessage.getDirection());
        assertTrue(parsedMessage instanceof HeartbeatAcknowledgement);
    }
    
    @Test
    public void simplePatternTestForACKMessage() {
        final TimePoint now = TimePoint.now();
        final Message ackHBD = new HeartbeatAcknowledgementImpl(QueclinkStreamParserImpl.parseProtocolVersionHex("301303"), "860599004785994", null, now, QueclinkStreamParserImpl.parseCountNumberHex("033E"));
        final Matcher matcher = QueclinkStreamParserImpl.messagePattern.matcher(ackHBD.getMessageString());
        assertTrue("Pattern "+QueclinkStreamParserImpl.messagePattern.toString()+" doesn't match "+ackHBD.getMessageString(), matcher.matches());
        assertEquals("+ACK:", matcher.group(1));
        assertEquals("HBD", matcher.group(7));
        assertEquals("301303,860599004785994,,"+QueclinkStreamParserImpl.formatAsYYYYMMDDHHMMSS(now)+",033E", matcher.group(8));
    }

    @Test
    public void simplePatternTestForSACKHeartbeatMessage() {
        final Message sackHBD = new HeartbeatServerAcknowledgementImpl(QueclinkStreamParserImpl.parseProtocolVersionHex("301303"), QueclinkStreamParserImpl.parseCountNumberHex("033E"));
        final Matcher matcher = QueclinkStreamParserImpl.messagePattern.matcher(sackHBD.getMessageString());
        assertTrue("Pattern "+QueclinkStreamParserImpl.messagePattern.toString()+" doesn't match "+sackHBD.getMessageString(), matcher.matches());
        assertEquals("+SACK:", matcher.group(1));
        assertEquals("HBD", matcher.group(7));
        assertEquals("301303,033E", matcher.group(8));
    }

    @Test
    public void simplePatternTestForBasicSACKMessage() {
        final String sackMessage = "+SACK:11F0$";
        final Matcher matcher = QueclinkStreamParserImpl.messagePattern.matcher(sackMessage);
        assertTrue("Pattern "+QueclinkStreamParserImpl.messagePattern.toString()+" doesn't match "+sackMessage, matcher.matches());
        assertEquals("+SACK:", matcher.group(1));
        assertEquals(null, matcher.group(7));
        assertEquals("11F0", matcher.group(8));
    }
}
