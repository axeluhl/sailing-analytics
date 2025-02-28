package com.sap.sailing.aiagent.persistence.impl;

import java.util.UUID;

import org.bson.Document;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.aiagent.persistence.DomainObjectFactory;
import com.sap.sse.common.Util;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private final MongoCollection<Document> eventsToCommentCollection;

    public DomainObjectFactoryImpl(MongoDatabase db) {
        eventsToCommentCollection = MongoObjectFactoryImpl.getOrCreateEventsToCommentCollection(db);
    }

    @Override
    public Iterable<UUID> getEventsToComment(ClientSession clientSessionOrNull) {
        final FindIterable<Document> documents;
        if (clientSessionOrNull == null) {
            documents = eventsToCommentCollection.find();
        } else {
            documents = eventsToCommentCollection.find(clientSessionOrNull);
        }
        return Util.map(documents, d->(UUID) d.get(FieldNames.AIAGENT_EVENTS_TO_COMMENT_EVENT_ID.name()));
    }
}
