package com.sap.sse.replication.impl;

import java.util.List;

public interface ReplicationMessageSender {
    void send(byte[] message, List<Class<?>> typesInMessage);
}
