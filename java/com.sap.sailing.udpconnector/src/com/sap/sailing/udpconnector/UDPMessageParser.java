package com.sap.sailing.udpconnector;

import java.net.DatagramPacket;

public interface UDPMessageParser<MessageType> {
    MessageType parse(DatagramPacket p);
}
