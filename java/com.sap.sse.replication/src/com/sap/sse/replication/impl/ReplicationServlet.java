package com.sap.sse.replication.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.util.tracker.ServiceTracker;

import com.rabbitmq.client.Channel;
import com.sap.sse.gateway.AbstractHttpServlet;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.ReplicaDescriptor;
import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicablesProvider;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.replication.ReplicationStatus;
import com.sap.sse.util.impl.CountingOutputStream;

import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

/**
 * The servlet supports registering and de-registering a replica from a master and can send the serialized initial load
 * data of the {@link Replicable}s registered as replicable OSGi service from the master to a requesting replica. The
 * initial load is transmitted through a {@link RabbitOutputStream}, and the response to the servlet request is only the
 * name of the queue the replica can use to obtain the initial load from the message queuing system. This guarantees
 * a more robust transmission of the initial load, even in the face of a flaky connection between master and replica.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class ReplicationServlet extends AbstractHttpServlet {
    private static final Logger logger = Logger.getLogger(ReplicationServlet.class.getName());
    
    private static final long serialVersionUID = 4835516998934433846L;
    
    public enum Action { REGISTER, INITIAL_LOAD, DEREGISTER, STATUS }
    
    /**
     * The parameter value found in the parameter with this name must have a value that matches any of the
     * {@link Action} names.
     */
    public static final String ACTION = "action";
    public static final String SERVER_UUID = "uuid";
    public static final String ADDITIONAL_INFORMATION = "additional";

    /**
     * The size of the packages into which the initial load is split
     */
    private static final int INITIAL_LOAD_PACKAGE_SIZE = 1024*1024;

    public static final String REPLICABLES_IDS_AS_STRINGS_COMMA_SEPARATED = "replicaIdsAsStringsCommaSeparated";

    public static final String REPLICABLE_ID_AS_STRING = "replicaIdAsString";

    private final ServiceTracker<ReplicationService, ReplicationService> replicationServiceTracker;
    
    private final ReplicablesProvider replicablesProvider;
    
    public ReplicationServlet() throws Exception {
        this(new OSGiReplicableTracker(Activator.getDefaultContext()),
                new ServiceTracker<ReplicationService, ReplicationService>(Activator.getDefaultContext(),
                        ReplicationService.class.getName(), /* tracker customizer */ null));
    }

    public ReplicationServlet(ReplicablesProvider replicablesProvider, ServiceTracker<ReplicationService, ReplicationService> replicationServiceTracker) {
        this.replicablesProvider = replicablesProvider;
        this.replicationServiceTracker = replicationServiceTracker;
        if (replicationServiceTracker != null) {
            replicationServiceTracker.open();
        }
    }
    
    protected ReplicationService getReplicationService() {
        return replicationServiceTracker == null ? null : replicationServiceTracker.getService();
    }

    /**
     * The client identifies itself in the request. Two servlet operations are supported currently: registering the
     * client with the replication service (if not already created, the message exchange will be created by this
     * registration); and triggering sending an initial load stream through RabbitMQ that the replica can use to
     * initialize its {@link Replicable} s. The IDs of the replicables of which the initial load is to be sent is
     * expected as the {@link #REPLICABLES_IDS_AS_STRINGS_COMMA_SEPARATED} parameter value. The servlet response
     * consists of the name of the RabbitMQ queue name that the client can connect to and through which to receive
     * the LZ4-compressed initial load stream per replicable, using a {@link RabbitInputStreamProvider} and
     * an {@link LZ4BlockInputStream}..
     * <p>
     * 
     * The operation performed is selected by passing one of the {@link Action} enumeration values for the URL parameter
     * named {@link #ACTION}.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter(ACTION);
        logger.fine("Received replication request, action is "+action);
        String[] replicableIdsAsStrings;
        switch (Action.valueOf(action)) {
        case REGISTER:
            logger.info("Received replica registration request");
            registerClientWithReplicationService(req, resp);
            break;
        case DEREGISTER:
            logger.info("Received replica deregistration request");
            deregisterClientWithReplicationService(req, resp);
            break;
        case INITIAL_LOAD:
            logger.info("Received replication initial load request");
            replicableIdsAsStrings = req.getParameter(REPLICABLES_IDS_AS_STRINGS_COMMA_SEPARATED).split(",");
            Channel channel = getReplicationService().createMasterChannel();
            try {
                RabbitOutputStream ros = new RabbitOutputStream(INITIAL_LOAD_PACKAGE_SIZE, channel,
                        /* queueName */ "initialLoad-for-"+req.getRemoteHost()+"@"+new Date()+"-"+UUID.randomUUID(),
                        /* syncAfterTimeout */ false);
                PrintWriter br = new PrintWriter(new OutputStreamWriter(resp.getOutputStream()));
                br.println(ros.getQueueName());
                br.flush();
                final CountingOutputStream countingOutputStream = new CountingOutputStream(
                        ros, /* log every megabyte */1024l * 1024l, Level.INFO,
                        "HTTP output for initial load for " + req.getRemoteHost());
                final LZ4BlockOutputStream compressingOutputStream = new LZ4BlockOutputStream(countingOutputStream);
                for (String replicableIdAsString : replicableIdsAsStrings) {
                    Replicable<?, ?> replicable = replicablesProvider.getReplicable(replicableIdAsString, /* wait */ false);
                    if (replicable == null) {
                        final String msg = "Couldn't find replicable with ID "+replicableIdAsString+". Aborting serialization of initial load.";
                        logger.severe(msg);
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, StringEscapeUtils.escapeHtml(msg));
                        break; // causing an error on the replica which is expecting the replica's initial load
                    }
                    try {
                        replicable.serializeForInitialReplication(compressingOutputStream);
                    } catch (Exception e) {
                        logger.info("Error trying to serialize initial load for replication: " + e.getMessage());
                        logger.log(Level.SEVERE, "doGet", e);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        e.printStackTrace(resp.getWriter());
                    }
                }
                compressingOutputStream.finish();
                countingOutputStream.close();
                break;
            } finally {
                channel.getConnection().close();
            }
        case STATUS:
            try {
                reportStatus(resp);
            } catch (IllegalAccessException e) {
                logger.info("Error obtaining replication status: " + e.getMessage());
                logger.log(Level.SEVERE, "doGet", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                e.printStackTrace(resp.getWriter());
            }
            break;
        default:
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action " + StringEscapeUtils.escapeHtml(action) + " not understood. Must be one of "
                    + Arrays.toString(Action.values()));
        }
    }

    /**
     * The status is reported as a JSON document. For each replicable it describes the status which tells
     * whether the replicable is still fetching its initial load, as well as the length of the queue of
     * inbound operations not yet processed. The JSON document is printed to the response object's writer.
     */
    private void reportStatus(HttpServletResponse resp) throws IllegalAccessException, IOException {
        final JSONObject result = new JSONObject();
        final JSONArray replicablesJSON = new JSONArray();
        final ReplicationStatus status = getReplicationService().getStatus();
        result.put("replica", status.isReplica());
        result.put("replicationstarting", status.isReplicationStarting());
        result.put("suspended", status.isSuspended());
        result.put("stopped", status.isStopped());
        result.put("messagequeuelength", status.getMessageQueueLength());
        final JSONArray operationQueueLengths = new JSONArray();
        result.put("operationqueuelengths", operationQueueLengths);
        for (final String replicableIdAsString : status.getReplicableIdsAsStrings()) {
            Integer queueLength = status.getOperationQueueLengthsByReplicableIdAsString(replicableIdAsString);
            if (queueLength != null) {
                final JSONObject queueLengthJSON = new JSONObject();
                queueLengthJSON.put("id", replicableIdAsString);
                queueLengthJSON.put("length", queueLength);
                operationQueueLengths.add(queueLengthJSON);
            }
        }
        result.put("totaloperationqueuelength", status.getTotalOperationQueueLength());
        for (final String replicableIdAsString : status.getReplicableIdsAsStrings()) {
            Boolean initialLoadRunning = status.isInitialLoadRunning(replicableIdAsString);
            if (initialLoadRunning != null) {
                final JSONObject replicableJSON = new JSONObject();
                replicableJSON.put("id", replicableIdAsString);
                replicableJSON.put("initialloadrunning", initialLoadRunning);
                replicablesJSON.add(replicableJSON);
            }
        }
        result.put("replicables", replicablesJSON);
        result.put("available", status.isAvailable());
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().print(result.toJSONString());
    }

    /**
     * Made public for test support
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        InputStream is = req.getInputStream();
        DataInputStream dis = new DataInputStream(is);
        String replicableIdAsString = dis.readUTF();
        try {
            Replicable<?, ?> replicable = replicablesProvider.getReplicable(replicableIdAsString, /* wait */ false);
            if (replicable != null) {
                logger.info("Received request to apply and replicate an operation from a replica for replicable "+replicable);
                applyOperationToReplicable(replicable, is);
            } else {
                logger.warning("Received operation for replicable "+replicableIdAsString+
                        ", but a replicable with that ID couldn't be found. Ignoring the operation.");
            }
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE,
                    "Exception occurred while trying to receive and apply operation initiated on replica", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Exception " + e
                    + " occurred while trying to receive and apply operation initiated on replica");
        }
    }

    /**
     * Applies <code>operation</code> to the replicable identified by <code>replicableIdAsString</code>. If such a
     * replicable cannot be found on this server instance, a warning is logged and the operation is ignored.
     */
    private <S, R> void applyOperationToReplicable(Replicable<S, ?> replicable, InputStream is)
            throws ClassNotFoundException, IOException {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(replicable.getClass().getClassLoader());
        OperationWithResult<S, ?> operation = replicable.readOperation(is);
        Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        logger.info("Applying operation of type " + operation.getClass().getName()
                + " received from replica to replicable " + replicable.toString());
        replicable.apply(operation);
    }

    private void deregisterClientWithReplicationService(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final UUID replicaUuid = UUID.fromString(req.getParameter(SERVER_UUID));
        final ReplicaDescriptor replica = getReplicationService().unregisterReplica(replicaUuid);
        if (replica != null) {
            logger.info("Deregistered replication client with this server " + replica.getIpAddress());
            resp.setContentType("text/plain");
            resp.getWriter().print(replica.getUuid());
        } else {
            logger.warning("Couldn't find replica to de-register with ID "+replicaUuid);
        }
    }

    private void registerClientWithReplicationService(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final ReplicaDescriptor replica = getReplicaDescriptor(req);
        getReplicationService().registerReplica(replica);
        logger.info("Registered new replica " + replica);
        resp.setContentType("text/plain");
        resp.getWriter().print(replica.getUuid());
    }

    private ReplicaDescriptor getReplicaDescriptor(HttpServletRequest req) throws UnknownHostException {
        InetAddress ipAddress = InetAddress.getByName(req.getRemoteAddr());
        UUID uuid = UUID.fromString(req.getParameter(SERVER_UUID));
        String additional = req.getParameter(ADDITIONAL_INFORMATION);
        final String[] replicableIdsAsStrings = req.getParameter(REPLICABLES_IDS_AS_STRINGS_COMMA_SEPARATED).split(",");
        return new ReplicaDescriptorImpl(ipAddress, uuid, additional, replicableIdsAsStrings);
    }
}
