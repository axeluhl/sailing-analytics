package com.sap.sailing.aiagent.persistence.impl;


import java.util.UUID;

import org.bson.Document;

import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.aiagent.persistence.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final MongoCollection<Document> eventsToCommentCollection;

    public MongoObjectFactoryImpl(MongoDatabase db) {
        this.eventsToCommentCollection = getOrCreateEventsToCommentCollection(db);
    }
    
    static MongoCollection<Document> getOrCreateEventsToCommentCollection(MongoDatabase database) {
        final MongoCollection<Document> eventsToCommentCollection = database.getCollection(CollectionNames.AIAGENT_EVENTS_TO_COMMENT.name());
        return eventsToCommentCollection;
    }

    @Override
    public void clear(ClientSession clientSessionOrNull) {
        if (clientSessionOrNull == null) {
            eventsToCommentCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).drop();
        } else {
            eventsToCommentCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).drop(clientSessionOrNull);
        }
    }
    
    @Override
    public void addEventToComment(UUID eventId, ClientSession clientSessionOrNull) {
        final Document document = new Document(FieldNames.AIAGENT_EVENTS_TO_COMMENT_EVENT_ID.name(), eventId);
        if (clientSessionOrNull == null) {
            eventsToCommentCollection.insertOne(document);
        } else {
            eventsToCommentCollection.insertOne(clientSessionOrNull, document);
        }
    }

    @Override
    public void removeEventToComment(UUID eventId, ClientSession clientSessionOrNull) {
        final Document filter = new Document(FieldNames.AIAGENT_EVENTS_TO_COMMENT_EVENT_ID.name(), eventId);
        if (clientSessionOrNull == null) {
            eventsToCommentCollection.deleteOne(filter);
        } else {
            eventsToCommentCollection.deleteOne(clientSessionOrNull, filter);
        }
    }
}
