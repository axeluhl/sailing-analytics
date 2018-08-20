package com.sap.sailing.udpconnector;

public interface UDPMessageListener<MessageType extends UDPMessage> {
    void received(MessageType message);
}
