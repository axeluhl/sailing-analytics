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
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;
import com.sap.sailing.domain.swisstimingadapter.SailMasterTransceiver;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.CollectionNames;
import com.sap.sailing.domain.swisstimingadapter.persistence.impl.FieldNames;
import com.sap.sailing.mongodb.MongoDBConfiguration;
import com.sap.sailing.mongodb.MongoDBService;

/**
 * Receives events from a SwissTiming SailMaster server, stores valid messages received persistently and forward them
 * to a port specified. The messages forwarded are augmented by sending a counter in ASCII encoding before the
 * message's <code>STX</code> start byte. This allows a receiver to optionally record the counter value after
 * having processed the message. When messages have to be retrieved from the database at a later point, a
 * client can request only those message starting at a specific counter value.<p>
 * 
 * The connectivity to the SailMaster system can operate in one of two modes. Either a TCP connection to the
 * SailMaster is initiated by this client. When the connection is lost or a connect is unsuccessful, the client
 * will try again periodically. In the other mode of operation, this client will act as a TCP server and accepts
 * inbound requests from the SailMaster system (typically a bridge that forwards messages from multiple
 * SailMaster systems). In this mode of operation it's up to the SailMaster environment to re-initiate
 * connects after a connection loss.
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
    private boolean receivingFromSailMaster;

    private final DBCollection lastMessageCountCollection;
  
    private final SwissTimingAdapterPersistence swissTimingAdapterPersistence;
    
    private final SwissTimingFactory swissTimingFactory;

    private final Thread storeAndForwardThread;

    private Socket socket;
    
    /**
     * Use of this server socket is optional and happens if and only if this object is operated in
     * "listening" mode. This means that the SailMaster system / bridge is expected to initiate TCP
     * connections to this object.
     */
    private ServerSocket serverSocketListeningForSailMasterBridge;

    private final int sailMasterPort;

    private final String sailMasterHostname;
    
    /**
     * When initialized using this constructor, the resulting object proactively connects and re-connects to the
     * SailMaster server specified by <code>sailMasterHostname</code>/<code>sailMasterPort</code>. The
     * {@link #serverSocketListeningForSailMasterBridge} remains <code>null</code> and {@link #listenPort} is set
     * to <code>-1</code>, indicating that this object is not listening on any port for incoming
     * SailMaster connections.
     */
    public StoreAndForward(String sailMasterHostname, int sailMasterPort, int portForClients, SwissTimingFactory swissTimingFactory, 
            SwissTimingAdapterPersistence swissTimingAdapterPersistence, MongoDBService mongoDBService) throws InterruptedException, IOException {
        this.db = mongoDBService.getDB();
        this.listenPort = -1;
        this.transceiver = swissTimingFactory.createSailMasterTransceiver();
        this.portForClients = portForClients;
        this.sailMasterHostname = sailMasterHostname;
        this.sailMasterPort = sailMasterPort;
        this.streamsToForwardTo = new ArrayList<OutputStream>();
        this.socketsToForwardTo = new ArrayList<Socket>();
        this.swissTimingAdapterPersistence = swissTimingAdapterPersistence;
        this.swissTimingFactory = swissTimingFactory; 
        lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        lastMessageCount = getLastMessageCount();
        clientListener = createClientListenerThread(portForClients);
        clientListener.start();
        storeAndForwardThread = new Thread(this, "StoreAndForward");
        storeAndForwardThread.start();
        synchronized (this) {
            while (!listeningForClients) {
                wait();
            }
            while (!receivingFromSailMaster) {
                wait();
            }
        }
    }

    /**
     * Creates a storing message forwarder in listening mode. In this mode, this object won't actively try to open
     * TCP connections to a SailMaster system / bridge but instead listen for inbound TCP connections on port
     * <code>listenPort</code>.
     * 
     * @param listenPort
     *            listens on this port for messages coming in from a real SwissTiming SailMaster
     * @param portForClients
     *            clients can connect to this port and will receive forwarded and sequence-numbered messages over those
     *            sockets
     * @throws IOException 
     */
    public StoreAndForward(final int listenPort, final int portForClients, SwissTimingFactory swissTimingFactory, 
            SwissTimingAdapterPersistence swissTimingAdapterPersistence, MongoDBService mongoDBService) throws InterruptedException, IOException {
        this.db = mongoDBService.getDB();
        this.listenPort = listenPort;
        this.transceiver = swissTimingFactory.createSailMasterTransceiver();
        this.portForClients = portForClients;
        this.streamsToForwardTo = new ArrayList<OutputStream>();
        this.socketsToForwardTo = new ArrayList<Socket>();
        this.swissTimingAdapterPersistence = swissTimingAdapterPersistence;
        this.swissTimingFactory = swissTimingFactory; 
        this.sailMasterHostname = null;
        this.sailMasterPort = -1;
        lastMessageCountCollection = db.getCollection(CollectionNames.LAST_MESSAGE_COUNT.name());
        lastMessageCount = getLastMessageCount();
        serverSocketListeningForSailMasterBridge = new ServerSocket(listenPort);
        clientListener = createClientListenerThread(portForClients);
        clientListener.start();
        storeAndForwardThread = new Thread(this, "StoreAndForward");
        storeAndForwardThread.start();
        synchronized (this) {
            while (!listeningForClients) {
                wait();
            }
            while (!receivingFromSailMaster) {
                wait();
            }
        }
    }
    
    private long getLastMessageCount() {
        DBObject lastMessageCountRecord = lastMessageCountCollection.findOne();
        if (lastMessageCountRecord == null) {
            lastMessageCountCollection.insert(new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 0l));
        }
        return lastMessageCountRecord == null ? 0 : (Long) lastMessageCountRecord.get(FieldNames.LAST_MESSAGE_COUNT.name());
    }

    /**
     * Returns <code>true</code> if and only if this object is listening for incoming SailMaster TCP
     * connections instead of actively connecting / reconnecting to a SailMaster server by itself.
     */
    private boolean isInSailMasterListeningMode() {
        return serverSocketListeningForSailMasterBridge != null;
    }

    private Thread createClientListenerThread(final int portForClients) {
        return new Thread(new Runnable() {
            public void run() {
                ServerSocket ss;
                try {
                    synchronized (StoreAndForward.this) {
                        ss = new ServerSocket(portForClients);
                        listeningForClients = true;
                        logger.info("StoreAndForward listening for clients on port "+portForClients);
                        StoreAndForward.this.notifyAll();
                    }
                    while (!stopped) {
                        Socket s = ss.accept();
                        logger.info("StoreAndForward received connector's connect request on port "+portForClients);
                        if (!stopped) {
                            synchronized (StoreAndForward.this) {
                                socketsToForwardTo.add(s);
                                streamsToForwardTo.add(s.getOutputStream());
                            }
                        } else {
                            s.close();
                        }
                    }
                    ss.close();
                    logger.info("StoreAndForward client listener thread stopped.");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "StoreAndForwardClientListener");
    }
    
    /**
     * Depending on the mode of operation, accepts an inbound connect from a SailMaster server / bridge or
     * actively initiates a TCP connection to the SailMaster address/port configured. When this method
     * returns, the {@link #socket} holds an open and connected socket.
     * @throws IOException 
     */
    private void establishConnection() throws IOException {
        if (isInSailMasterListeningMode()) {
            synchronized (this) {
                receivingFromSailMaster = true;
                logger.info("StoreAndForward waiting for inbound SailMaster connections on port "+listenPort);
                notifyAll();
            }
            socket = serverSocketListeningForSailMasterBridge.accept();
            logger.info("StoreAndForward received SailMaster connect on port "+listenPort);
        } else {
            synchronized (this) {
                receivingFromSailMaster = true;
                logger.info("StoreAndForward issuing SailMaster connection to "+sailMasterHostname+":"+sailMasterPort);
                notifyAll();
            }
           socket = new Socket(sailMasterHostname, sailMasterPort);
           logger.info("StoreAndForward connections to SailMaster "+sailMasterHostname+":"+sailMasterPort+" established");
        }
    }
    
    /**
     * Stops execution after having received the next message
     */
    public void stop() throws UnknownHostException, IOException, InterruptedException {
        logger.entering(getClass().getName(), "stop");
        stopped = true;
        new Socket("localhost", portForClients); // this is to stop the client listener thread
        logger.info("joining clientListener thread "+clientListener);
        clientListener.join();
        socket.close(); // will let a read terminate abnormally
        logger.info("joining storeAndForwardThread "+storeAndForwardThread);
        storeAndForwardThread.join();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        int listenPort = Integer.valueOf(args[0]);
        int clientPort = Integer.valueOf(args[1]);
        
        MongoDBService mongoDBService = MongoDBService.INSTANCE;
        mongoDBService.setConfiguration(MongoDBConfiguration.getDefaultConfiguration());
        SwissTimingAdapterPersistence swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        new StoreAndForward(listenPort, clientPort, SwissTimingFactory.INSTANCE, swissTimingAdapterPersistence, mongoDBService);
    }

    public void run() {
        logger.entering(getClass().getName(), "run");
        try {
            while (!stopped) {
                try {
                    establishConnection();
                    InputStream is = socket.getInputStream();
                    Pair<String, Long> messageAndOptionalSequenceNumber = transceiver.receiveMessage(is);
                    // ignore any sequence number contained in the message; we'll create our own
                    DBObject emptyQuery = new BasicDBObject();
                    DBObject incrementLastMessageCountQuery = new BasicDBObject().
                            append("$inc", new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 1));
                    while (!stopped && messageAndOptionalSequenceNumber != null) {
                        logger.fine("Received message: "+messageAndOptionalSequenceNumber.getA());
                        DBObject newCountRecord = lastMessageCountCollection.findAndModify(emptyQuery, incrementLastMessageCountQuery);
                        lastMessageCount = (Long) ((newCountRecord == null) ? 0 : newCountRecord.get(FieldNames.LAST_MESSAGE_COUNT.name()));
                        SailMasterMessage message = swissTimingFactory.createMessage(messageAndOptionalSequenceNumber.getA(), lastMessageCount);
                        swissTimingAdapterPersistence.storeSailMasterMessage(message);
                        synchronized (this) {
                            for (OutputStream os : streamsToForwardTo) {
                                // write the sequence number of the message into the stream before actually writing the
                                // SwissTiming message
                                // TODO if forwarding to os doesn't work, e.g., because the socket was closed or the client died, remove os from streamsToForwardTo and the socket from socketsToForwardTo
                                transceiver.sendMessage(message, os);
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
                } catch (Throwable e) {
                    e.printStackTrace();
                    if (!stopped) {
                        logger.throwing(StoreAndForward.class.getName(), "Error during forwarding message. Continuing...", e);
                        try {
                            Thread.sleep(1000l); // wait a little bit before trying to re-establish a connection
                        } catch (InterruptedException e1) {
                            logger.throwing(StoreAndForward.class.getName(), "Can't find any sleep...", e1);
                        } 
                    } else {
                        logger.info("StoreAndForward socket was closed.");
                    }
                }
            }
            logger.info("StoreAndForward is closing receiving server socket");
            if (serverSocketListeningForSailMasterBridge != null && !serverSocketListeningForSailMasterBridge.isClosed()) {
                serverSocketListeningForSailMasterBridge.close();
            }
            logger.info("Stopping StoreAndForward server.");
        } catch (IOException e) {
            logger.throwing(getClass().getName(), "run", e);
            throw new RuntimeException(e);
        }
        logger.exiting(getClass().getName(), "run");
    }
}
