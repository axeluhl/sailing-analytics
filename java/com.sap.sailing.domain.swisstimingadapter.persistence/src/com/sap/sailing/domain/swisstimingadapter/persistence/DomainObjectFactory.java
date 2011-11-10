package com.sap.sailing.domain.swisstimingadapter.persistence;

import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.Race;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.mongodb.Activator;

/**
 * Offers methods to load domain objects from a Mongo DB
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface DomainObjectFactory extends RaceSpecificMessageLoader {
    DomainObjectFactory INSTANCE = new DomainObjectFactoryImpl(Activator.getDefaultInstance().getDB(), SwissTimingFactory.INSTANCE);

    Iterable<SwissTimingConfiguration> getSwissTimingConfigurations();
    
    /**
     * Loads all messages received and stored to the DB starting with sequence number <code>firstSequenceNumber</code>.
     * If <code>firstSequenceNumber</code> is -1, all messages are loaded.
     * 
     * @return messages in ascending sequence number order; always a valid but perhaps empty list
     */
    List<SailMasterMessage> loadMessages(int firstSequenceNumber);

    /**
     * Loads all messages received and stored to the DB for a specific race.
     * @return messages of the specified race; null if the race does not exist
     */
    List<SailMasterMessage> loadRaceMessages(String raceID);

    /**
     * Loads all command messages (not race specific) received and stored to the DB.
     * @return command messages; always a valid but perhaps empty list
     */
    List<SailMasterMessage> loadCommandMessages();

    /**
     * Gets all races stored in the DB
     * @return the list of races
     */
    List<Race> getRaces();

    /**
     * Gets the race masterdata for a given raceID
     * @return the race or null if the race does not exist 
     */
    Race getRace(String raceID);
}
