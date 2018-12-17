package com.sap.sailing.windestimation.data.persistence.twdtransition;

import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.maneuver.AbstractPersistenceManager;
import com.sap.sailing.windestimation.data.serialization.TwdTransitionJsonDeserializer;
import com.sap.sailing.windestimation.data.serialization.TwdTransitionJsonSerializer;

public class TwdTransitionPersistenceManager extends AbstractPersistenceManager<TwdTransition> {

    private static final String COLLECTION_NAME = "twdTransitions";
    private final TwdTransitionJsonSerializer serializer = new TwdTransitionJsonSerializer();

    public TwdTransitionPersistenceManager() throws UnknownHostException {
        super();
    }

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    protected JsonDeserializer<TwdTransition> getNewJsonDeserializer() {
        return new TwdTransitionJsonDeserializer();
    }

    public void add(TwdTransition twdTransition) {
        JSONObject jsonObject = serializer.serialize(twdTransition);
        DBObject dbObject = (DBObject) JSON.parse(jsonObject.toString());
        getDb().getCollection(getCollectionName()).insert(dbObject);
    }

    public void add(List<TwdTransition> twdTransitions) {
        List<DBObject> dbObjects = twdTransitions.stream()
                .map(twdTransition -> (DBObject) JSON.parse(serializer.serialize(twdTransition).toString()))
                .collect(Collectors.toList());
        getDb().getCollection(getCollectionName()).insert(dbObjects);
    }

}
