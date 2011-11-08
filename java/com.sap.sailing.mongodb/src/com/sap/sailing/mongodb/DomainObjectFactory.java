package com.sap.sailing.mongodb;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.mongodb.impl.Activator;
import com.sap.sailing.mongodb.impl.DomainObjectFactoryImpl;

/**
 * Offers methods to load domain objects from a Mongo DB
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface DomainObjectFactory {
    DomainObjectFactory INSTANCE = new DomainObjectFactoryImpl(Activator.getDefaultInstance().getDB());

    WindTrack loadWindTrack(Event event, RaceDefinition race, WindSource windSource, long millisecondsOverWhichToAverage);

    Iterable<TracTracConfiguration> getTracTracConfigurations();
    
    Leaderboard loadLeaderboard(String name);

    Iterable<Leaderboard> getAllLeaderboards();

    Iterable<SwissTimingConfiguration> getSwissTimingConfigurations();
}
