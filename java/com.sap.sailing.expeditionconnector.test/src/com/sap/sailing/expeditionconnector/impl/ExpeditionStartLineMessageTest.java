package com.sap.sailing.expeditionconnector.impl;

import static org.junit.Assert.assertEquals;

import java.net.SocketException;

import org.junit.Test;

import com.sap.sailing.expeditionconnector.UDPExpeditionReceiver;

public class ExpeditionStartLineMessageTest {
    @Test
    public void testCreatePingStartLineMessages() throws SocketException {
        final int checksum = new ExpeditionMessageParser(new UDPExpeditionReceiver(/* listening on port */ 0))
                .computeChecksum("#0,4,133.9,5,2.12,6,348.5,94,349.8,95,2.41*09");
        assertEquals(9, checksum);
    }
}
