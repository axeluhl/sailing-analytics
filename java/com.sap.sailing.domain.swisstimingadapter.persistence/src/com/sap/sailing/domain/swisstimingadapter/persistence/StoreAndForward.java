package com.sap.sailing.domain.swisstimingadapter.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
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

    private final Set<ReceivingThread> receivingThreads;
    
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
        this.receivingThreads = new HashSet<>();
        this.db = mongoDBService.getDB();
        this.listenPort = -1;
        this.transceiver = swissTimingFactory.createSailMasterTransceiver();
        this.portForClients = portForClients;
        this.sailMasterHostname = sailMasterHostname;
        this.sailMasterPort = sailMasterPort;
        this.streamsToForwardTo = Collections.synchronizedList(new ArrayList<OutputStream>());
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
        this.receivingThreads = new HashSet<>();
        this.db = mongoDBService.getDB();
        this.listenPort = listenPort;
        this.transceiver = swissTimingFactory.createSailMasterTransceiver();
        this.portForClients = portForClients;
        this.streamsToForwardTo = Collections.synchronizedList(new ArrayList<OutputStream>());
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
            lastMessageCountCollection.insert(new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 0l), WriteConcern.SAFE);
        }
        return lastMessageCountRecord == null ? 0 : ((Number) lastMessageCountRecord.get(FieldNames.LAST_MESSAGE_COUNT.name())).longValue();
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
     * Depending on the mode of operation, accepts an inbound connect from a SailMaster server / bridge or actively
     * initiates a TCP connection to the SailMaster address/port configured. When a connection has been established, a
     * new {@link ReceivingThread} is spawned for the new socket. If in listening mode, the
     * {@link ServerSocket#accept()} call is repeated until the instance is {@link #stop()}ed.
     * 
     * @throws IOException
     */
    private ReceivingThread establishConnection() throws IOException {
        Socket socket;
        ReceivingThread receivingThread = null;
        if (isInSailMasterListeningMode()) {
            while (!stopped) {
                synchronized (this) {
                    receivingFromSailMaster = true;
                    logger.info("StoreAndForward waiting for inbound SailMaster connections on port " + listenPort);
                    notifyAll();
                }
                socket = serverSocketListeningForSailMasterBridge.accept();
                logger.info("StoreAndForward received SailMaster connect on port " + listenPort);
                receivingThread = new ReceivingThread("SwissTiming SailMaster ReceivingThread for "+socket, socket);
                receivingThreads.add(receivingThread);
                receivingThread.start();
            }
        } else {
            synchronized (this) {
                receivingFromSailMaster = true;
                logger.info("StoreAndForward issuing SailMaster connection to "+sailMasterHostname+":"+sailMasterPort);
                notifyAll();
            }
           socket = new Socket(sailMasterHostname, sailMasterPort);
           logger.info("StoreAndForward connections to SailMaster "+sailMasterHostname+":"+sailMasterPort+" established");
           receivingThread = new ReceivingThread("SwissTiming SailMaster ReceivingThread for "+socket, socket);
           receivingThreads.add(receivingThread);
           receivingThread.start();
        }
        return receivingThread;
    }

    /**
     * Does the actual receiving. Is initialized with the socket on which to receive.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private class ReceivingThread extends Thread {
        private final Socket socket;
        private boolean stopped;
        
        public ReceivingThread(String threadName, Socket socket) {
            super(threadName);
            this.socket = socket;
        }

        @Override
        public void run() {
            logger.entering(getClass().getName(), "run");
            while (!stopped) {
                try {
                    InputStream is = socket.getInputStream();
                    try {
                        Pair<String, Long> messageAndOptionalSequenceNumber = transceiver.receiveMessage(is);
                        if (messageAndOptionalSequenceNumber == null) {
                            // found EOF; stopping
                            stopped = true;
                        } else {
                            // ignore any sequence number contained in the message; we'll create our own
                            DBObject emptyQuery = new BasicDBObject();
                            DBObject incrementLastMessageCountQuery = new BasicDBObject().append("$inc",
                                    new BasicDBObject().append(FieldNames.LAST_MESSAGE_COUNT.name(), 1));
                            while (!stopped && messageAndOptionalSequenceNumber != null) {
                                logger.fine("Thread " + this + " received message: "
                                        + messageAndOptionalSequenceNumber.getA());
                                DBObject newCountRecord = lastMessageCountCollection.findAndModify(emptyQuery,
                                        incrementLastMessageCountQuery);
                                lastMessageCount = ((newCountRecord == null) ? 0l : ((Number) newCountRecord
                                        .get(FieldNames.LAST_MESSAGE_COUNT.name())).longValue());
                                SailMasterMessage message = swissTimingFactory.createMessage(
                                        messageAndOptionalSequenceNumber.getA(), lastMessageCount);
                                swissTimingAdapterPersistence.storeSailMasterMessage(message);
                                synchronized (StoreAndForward.this) {
                                    for (OutputStream os : new ArrayList<OutputStream>(streamsToForwardTo)) {
                                        // write the sequence number of the message into the stream before actually
                                        // writing
                                        // the
                                        // SwissTiming message
                                        try {
                                            transceiver.sendMessage(message, os);
                                        } catch (Exception e) {
                                            logger.log(Level.SEVERE, "Error sending message to " + os, e);
                                            int i = streamsToForwardTo.indexOf(os);
                                            try {
                                                os.close();
                                            } catch (Exception exc) {
                                                logger.log(Level.SEVERE, "Exception closing socket output stream " + os
                                                        + " after being unable to forward message " + message, exc);
                                            }
                                            streamsToForwardTo.remove(os);
                                            Socket s = socketsToForwardTo.remove(i);
                                            logger.info("Unable to send to socket " + s
                                                    + ". Trying to close. Removing from sockets to forward to.");
                                            try {
                                                s.close();
                                            } catch (Exception exc) {
                                                logger.log(Level.WARNING, "Exception trying to close socket " + s
                                                        + " after being unable to forward message " + message, exc);
                                            }
                                        }
                                    }
                                }
                                if (!stopped) {
                                    messageAndOptionalSequenceNumber = transceiver.receiveMessage(is);
                                    if (messageAndOptionalSequenceNumber == null) {
                                        // received EOF; stopping received
                                        stopped = true;
                                    }
                                }
                            }
                        }
                    } catch (SocketException e) {
                        logger.log(
                                Level.INFO,
                                "Error during receiving message. Terminating this receiver and waiting for another inbound connection.",
                                e);
                        stopped = true;
                    }
                    // note that we're not changing anything with the sockets to which we forward messages; that will only happen
                    // if forwarding to any of those sockets fails
                } catch (Exception e) {
                    if (!stopped) {
                        logger.log(Level.INFO, "Error during forwarding message. Continuing...", e);
                        try {
                            Thread.sleep(1000l); // wait a little bit before trying to re-establish a connection
                        } catch (InterruptedException e1) {
                            logger.log(Level.INFO, "Can't find any sleep...", e1);
                        } 
                    } else {
                        logger.info("StoreAndForward socket was closed.");
                    }
                }
            }
            logger.info("Terminating StoreAndForward ReceivingThread for socket "+socket);
            receivingThreads.remove(this);
        }
        
        /**
         * Stops execution after having received the next message
         */
        public void stopReceiver() throws UnknownHostException, IOException, InterruptedException {
            logger.entering(getClass().getName(), "stop");
            stopped = true;
            logger.info("closing socket");
            socket.close(); // will let a read terminate abnormally
        }
    }
    
    /**
     * Stops execution after having received the next message
     */
    public void stop() throws UnknownHostException, IOException, InterruptedException {
        logger.entering(getClass().getName(), "stop");
        stopped = true;
        Socket closer = new Socket("localhost", portForClients); // this is to stop the client listener thread
        closer.close();
        logger.info("joining clientListener thread "+clientListener);
        clientListener.join();
        for (ReceivingThread receivingThread : receivingThreads) {
            receivingThread.stopReceiver();
            logger.info("joining storeAndForwardThread "+storeAndForwardThread);
            receivingThread.join();
        }
        logger.info("StoreAndForward is closing receiving server socket");
        if (serverSocketListeningForSailMasterBridge != null && !serverSocketListeningForSailMasterBridge.isClosed()) {
            serverSocketListeningForSailMasterBridge.close();
        }
        logger.info("Stopping StoreAndForward server.");
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        String hostname = null;
        int i=0;
        if (args[i].equals("-h")) {
            hostname = args[++i];
            i++;
        }
        int sailMasterPort = Integer.valueOf(args[i++]);
        int clientPort = Integer.valueOf(args[i++]);
        
        MongoDBService mongoDBService = MongoDBConfiguration.getDefaultConfiguration().getService();
        SwissTimingAdapterPersistence swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        if (hostname == null) {
            new StoreAndForward(sailMasterPort, clientPort, SwissTimingFactory.INSTANCE, swissTimingAdapterPersistence, mongoDBService);
        } else {
            new StoreAndForward(hostname, sailMasterPort, clientPort, SwissTimingFactory.INSTANCE, swissTimingAdapterPersistence, mongoDBService);
        }
    }

    public void run() {
        logger.entering(getClass().getName(), "run");
        try {
            while (!stopped) {
                try {
                    ReceivingThread receivingThread = establishConnection();
                    if (!isInSailMasterListeningMode()) {
                        receivingThread.join(); // we're in active connecting mode; wait for thread to die, then try again
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception in StoreAndForward", e);
                    try {
                        Thread.sleep(100); // if the exception persists, wait a little, hoping something may recover
                    } catch (InterruptedException e1) {
                        logger.log(Level.INFO, "StoreAndForward.run interrupted; setting stopped=true", e1);
                        stopped = true;
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
