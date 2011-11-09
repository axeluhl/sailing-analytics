package com.sap.sailing.domain.swisstimingadapter.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
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
    private final int portForClients;
    private long lastMessageCount;
    private boolean stopped;
    private final Thread clientListener;
    private final List<Socket> socketsToForwardTo;
    private final List<OutputStream> streamsToForwardTo;
    private boolean listeningForClients;

    private final DBCollection lastMessageCountCollection;
    
    private final MongoObjectFactory mongoObjectFactory;
    
    private final SwissTimingFactory swissTimingFactory;
    
    /**
     * @param listenPort
     *            listens on this port for messages coming in from a real SwissTiming SailMaster
     * @param portForClients
     *            clients can connect to this port and will receive forwarded and sequence-numbered messages over those
     *            sockets
     * @param mongoObjectFactory TODO
     * @param swissTimingFactory TODO
     */
    public StoreAndForward(int listenPort, final int portForClients, MongoObjectFactory mongoObjectFactory,
            SwissTimingFactory swissTimingFactory) throws InterruptedException {
        db = Activator.getDefaultInstance().getDB();
        this.listenPort = listenPort;
        this.transceiver = SwissTimingFactory.INSTANCE.createSailMasterTransceiver();
        this.portForClients = portForClients;
        this.streamsToForwardTo = new ArrayList<OutputStream>();
        this.socketsToForwardTo = new ArrayList<Socket>();
        this.mongoObjectFactory = mongoObjectFactory;
        this.swissTimingFactory = swissTimingFactory;
        lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        DBObject lastMessageCountRecord = lastMessageCountCollection.findOne();
        lastMessageCount = lastMessageCountRecord == null ? 0 : (Long) lastMessageCountRecord.get(FieldNames.LAST_MESSAGE_COUNT.name());
        clientListener = new Thread(new Runnable() {
            public void run() {
                ServerSocket ss;
                try {
                    synchronized (StoreAndForward.this) {
                        ss = new ServerSocket(portForClients);
                        listeningForClients = true;
                        StoreAndForward.this.notifyAll();
                    }
                    while (!stopped) {
                        Socket s = ss.accept();
                        if (!stopped) {
                            synchronized (StoreAndForward.this) {
                                socketsToForwardTo.add(s);
                                streamsToForwardTo.add(s.getOutputStream());
                            }
                        } else {
                            s.close();
                        }
                    }
                    logger.info("StoreAndForward client listener thread stopped.");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "StoreAndForwardClientListener");
        clientListener.start();
        synchronized (this) {
            while (!listeningForClients) {
                wait();
            }
        }
    }
    
    /**
     * Stops execution after having received the next message
     */
    public void stop() throws UnknownHostException, IOException, InterruptedException {
        stopped = true;
        new Socket("localhost", portForClients); // this is to stop the client listener thread
        clientListener.join();
    }

    public static void main(String[] args) throws InterruptedException {
        int listenPort = Integer.valueOf(args[0]);
        int clientPort = Integer.valueOf(args[1]);
        StoreAndForward storeAndForward = new StoreAndForward(listenPort, clientPort, MongoObjectFactory.INSTANCE, SwissTimingFactory.INSTANCE);
        storeAndForward.run();
    }

    public void run() {
        try {
            ServerSocket ss = new ServerSocket(listenPort);
            while (!stopped) {
                Socket socket = ss.accept();
                try {
                    InputStream is = socket.getInputStream();
                    Pair<String, Long> messageAndOptionalSequenceNumber = transceiver.receiveMessage(is);
                    // ignore any sequence number contained in the message; we'll create our own
                    DBObject emptyQuery = new BasicDBObject();
                    DBObject incrementLastMessageCountQuery = new BasicDBObject().
                            append("$inc", new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 1));
                    while (messageAndOptionalSequenceNumber != null) {
                        DBObject newCountRecord = lastMessageCountCollection.findAndModify(emptyQuery, incrementLastMessageCountQuery);
                        lastMessageCount = (Long) newCountRecord.get(FieldNames.LAST_MESSAGE_COUNT.name());
                        SailMasterMessage message = swissTimingFactory.createMessage(messageAndOptionalSequenceNumber.getA(), lastMessageCount);
                        mongoObjectFactory.storeRawSailMasterMessage(message);
                        synchronized (this) {
                            for (OutputStream os : streamsToForwardTo) {
                                // write the sequence number of the message into the stream before actually writing the
                                // SwissTiming message
                                os.write(("" + message.getSequenceNumber()).getBytes());
                                transceiver.sendMessage(message.getMessage(), os);
                            }
                        }
                        if (!stopped) {
                            messageAndOptionalSequenceNumber = transceiver.receiveMessage(is);
                        }
                    }
                    for (OutputStream os : streamsToForwardTo) {
                        os.close();
                    }
                    for (Socket socketToForwardTo : socketsToForwardTo) {
                        socketToForwardTo.close();
                    }
                } catch (Exception e) {
                    logger.throwing(StoreAndForward.class.getName(), "Error during forwarding message. Continuing...", e);
                }
            }
            logger.info("Stopping StoreAndForward server.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
