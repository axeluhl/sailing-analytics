package com.sap.sailing.aiagent.persistence;

import java.util.UUID;

import com.mongodb.client.ClientSession;

public interface DomainObjectFactory {
    Iterable<UUID> getEventsToComment(ClientSession clientSessionOrNull);
}
