package com.sap.sailing.domain.swisstimingadapter.persistence;

/**
 * Receives events from a SwissTiming SailMaster server, stores valid messages received persistently and forward them
 * to a port specified. The messages forwarded are augmented by sending a counter in ASCII encoding before the
 * message's <code>STX</code> start byte. This allows a receiver to optionally record the counter value after
 * having processed the message. When messages have to be retrieved from the database at a later point, a
 * client can request only those message starting at a specific counter value.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class StoreAndForward {
    public static void main(String[] args) {
        // TODO listen on configurable socket for SwissTiming messages; store to MongoDB using MongoObjectFactory, augment by counter and forward to one or more specified hosts/ports
    }
}
