package com.sap.sailing.domain.swisstimingadapter.persistence;

import java.util.List;

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
public interface DomainObjectFactory {
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
