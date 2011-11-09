package com.sap.sailing.domain.swisstimingadapter.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.swisstimingadapter.SailMasterTransceiver;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.CollectionNames;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.FieldNames;
import com.sap.sailing.mongodb.Activator;
import com.sap.sailing.util.Util.Pair;

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
public class StoreAndForward implements Runnable {
    private static final Logger logger = Logger.getLogger(StoreAndForward.class.getName());
    
    private final DB db;
    private final int listenPort;
    private final SailMasterTransceiver transceiver;
    private final List<Pair<String, Integer>> hostnamesAndPorts;
    private long lastMessageCount;

    private final DBCollection lastMessageCountCollection;
    
    public StoreAndForward(int listenPort, List<Pair<String, Integer>> hostnamesAndPorts) {
        db = Activator.getDefaultInstance().getDB();
        this.listenPort = listenPort;
        this.transceiver = SwissTimingFactory.INSTANCE.createSailMasterTransceiver();
        this.hostnamesAndPorts = hostnamesAndPorts;
        lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        DBObject lastMessageCountRecord = lastMessageCountCollection.findOne();
        lastMessageCount = (Long) lastMessageCountRecord.get(FieldNames.LAST_MESSAGE_COUNT.name());
    }

    public static void main(String[] args) {
        int listenPort = Integer.valueOf(args[0]);
        List<Pair<String, Integer>> hostnamesAndPorts = new ArrayList<Pair<String,Integer>>();
        for (int i=1; i<args.length-1; i+=2) {
            Pair<String, Integer> hostnameAndPort = new Pair<String, Integer>(args[i], Integer.valueOf(args[i+1]));
            hostnamesAndPorts.add(hostnameAndPort);
        }
        StoreAndForward storeAndForward = new StoreAndForward(listenPort, hostnamesAndPorts);
        storeAndForward.run();
    }

    public void run() {
        try {
            ServerSocket ss = new ServerSocket(listenPort);
            while (true) {
                Socket socket = ss.accept();
                try {
                    List<OutputStream> streamsToForwardTo = new ArrayList<OutputStream>();
                    List<Socket> socketsToForwardTo = new ArrayList<Socket>();
                    for (Pair<String, Integer> hostnameAndPort : hostnamesAndPorts) {
                        try {
                            Socket socketToForwardTo = new Socket(hostnameAndPort.getA(), hostnameAndPort.getB());
                            socketsToForwardTo.add(socketToForwardTo);
                            streamsToForwardTo.add(socketToForwardTo.getOutputStream());
                        } catch (Exception e) {
                            logger.throwing(StoreAndForward.class.getName(), "While trying to open a socket to forward to "+
                                    hostnameAndPort, e);
                        }
                    }
                    InputStream is = socket.getInputStream();
                    Pair<String, Integer> messageAndOptionalSequenceNumber = transceiver.receiveMessage(is);
                    // ignore any sequence number contained in the message; we'll create our own
                    DBObject emptyQuery = new BasicDBObject();
                    DBObject incrementLastMessageCountQuery = new BasicDBObject().
                            append("$inc", new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 1));
                    while (messageAndOptionalSequenceNumber != null) {
                        DBObject newCountRecord = lastMessageCountCollection.findAndModify(emptyQuery, incrementLastMessageCountQuery);
                        lastMessageCount = (Long) newCountRecord.get(FieldNames.LAST_MESSAGE_COUNT.name());
                        for (OutputStream os : streamsToForwardTo) {
                            // write the sequence number of the message into the stream before actually writing the SwissTiming message
                            os.write((""+lastMessageCount).getBytes());
                            transceiver.sendMessage(messageAndOptionalSequenceNumber.getA(), os);
                        }
                        messageAndOptionalSequenceNumber = transceiver.receiveMessage(is);
                    }
                    for (OutputStream os : streamsToForwardTo) {
                        os.close();
                    }
                    for (Socket socketToForwardTo : socketsToForwardTo) {
                        socketToForwardTo.close();
                    }
                } catch (Exception e) {
                    
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
