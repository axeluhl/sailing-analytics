package com.sap.sailing.domain.swisstimingadapter.persistence;

import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.SwissTimingAdapterPersistenceImpl;
import com.sap.sailing.mongodb.MongoDBService;

public interface SwissTimingAdapterPersistence extends RaceSpecificMessageLoader {

    SwissTimingAdapterPersistence INSTANCE = new SwissTimingAdapterPersistenceImpl(MongoDBService.INSTANCE, SwissTimingFactory.INSTANCE);

    Iterable<SwissTimingConfiguration> getSwissTimingConfigurations();

    Iterable<SwissTimingArchiveConfiguration> getSwissTimingArchiveConfigurations();
    
    /**
     * Loads all messages received and stored to the DB starting with sequence number <code>firstSequenceNumber</code>.
     * If <code>firstSequenceNumber</code> is -1, all messages are loaded.
     * 
     * @return messages in ascending sequence number order; always a valid but perhaps empty list
     */
    List<SailMasterMessage> loadCommandMessages(int firstSequenceNumber);

    /**
     * Loads all command messages (not race specific) received and stored to the DB.
     * @return command messages; always a valid but perhaps empty list
     */
    List<SailMasterMessage> loadCommandMessages();

    /**
     * Gets the race masterdata for a given raceID
     * @return the race or null if the race does not exist 
     */
    Race getRace(String raceID);
    
    void storeSwissTimingConfiguration(SwissTimingConfiguration swissTimingConfiguration);
    
    void storeSwissTimingArchiveConfiguration(SwissTimingArchiveConfiguration createSwissTimingArchiveConfiguration);

    void storeRace(Race race);
    
    void dropAllRaceMasterData();

    void dropAllMessageData();
}
