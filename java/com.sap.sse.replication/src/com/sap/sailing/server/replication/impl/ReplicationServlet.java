package com.sap.sailing.server.replication.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.rabbitmq.client.Channel;
import com.sap.sailing.server.replication.Replicable;
import com.sap.sailing.server.replication.ReplicationService;
import com.sap.sse.gateway.AbstractHttpServlet;
import com.sap.sse.util.impl.CountingOutputStream;

/**
 * As the response to any type of <code>GET</code> request, sends a serialized copy of the {@link RacingEventService} to
 * the response's output stream.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class ReplicationServlet extends AbstractHttpServlet {
    private static final Logger logger = Logger.getLogger(ReplicationServlet.class.getName());
    
    private static final long serialVersionUID = 4835516998934433846L;
    
    public enum Action { REGISTER, INITIAL_LOAD, DEREGISTER }
    
    public static final String ACTION = "action";
    public static final String SERVER_UUID = "uuid";
    public static final String ADDITIONAL_INFORMATION = "additional";

    /**
     * The size of the packages into which the initial load is split
     */
    private static final int INITIAL_LOAD_PACKAGE_SIZE = 1024*1024;

    private ServiceTracker<ReplicationService<?>, ReplicationService<?>> replicationServiceTracker;
    
    private ServiceTracker<Replicable<?, ?>, Replicable<?, ?>> replicableServiceTracker;
    
    public ReplicationServlet() throws Exception {
        BundleContext context = Activator.getDefaultContext();
        replicationServiceTracker = new ServiceTracker<ReplicationService<?>, ReplicationService<?>>(context, ReplicationService.class.getName(), null);
        replicationServiceTracker.open();
        replicableServiceTracker = new ServiceTracker<Replicable<?, ?>, Replicable<?, ?>>(context, Replicable.class.getName(), null);
        replicableServiceTracker.open();
    }

    protected ReplicationService<?> getReplicationService() {
        return replicationServiceTracker.getService();
    }

    /**
     * The client identifies itself in the request. Two servlet operations are supported currently: registering the
     * client with the replication service (if not already created, the JMS replication topic will be created by this
     * registration); and obtaining an initial load stream that the replica can use to initialize its
     * {@link RacingEventService}. The operation performed is selected by passing one of the {@link Action} enumeration
     * values for the URL parameter named {@link #ACTION}.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter(ACTION);
        logger.info("Received replication request, action is "+action);
        switch (Action.valueOf(action)) {
        case REGISTER:
            registerClientWithReplicationService(req, resp);
            break;
        case DEREGISTER:
            deregisterClientWithReplicationService(req, resp);
            break;
        case INITIAL_LOAD:
            Channel channel = getReplicationService().createMasterChannel();
            RabbitOutputStream ros = new RabbitOutputStream(INITIAL_LOAD_PACKAGE_SIZE, channel,
                    /* queueName */ "initialLoad-for-"+req.getRemoteHost()+"@"+new Date()+"-"+UUID.randomUUID(),
                    /* syncAfterTimeout */ false);
            PrintWriter br = new PrintWriter(new OutputStreamWriter(resp.getOutputStream()));
            br.println(ros.getQueueName());
            br.flush();
            final CountingOutputStream countingOutputStream = new CountingOutputStream(
                    ros, /* log every megabyte */1024l * 1024l, Level.INFO,
                    "HTTP output for initial load for " + req.getRemoteHost());
            final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(countingOutputStream);
            ObjectOutputStream oos = new ObjectOutputStream(gzipOutputStream);
            try {
                getReplicable().serializeForInitialReplication(oos);
                gzipOutputStream.finish();
            } catch (Exception e) {
                logger.info("Error trying to serialize initial load for replication: "+e.getMessage());
                logger.log(Level.SEVERE, "doGet", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                e.printStackTrace(resp.getWriter());
            }
            countingOutputStream.close();
            break;
        default:
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action " + action + " not understood. Must be one of "
                    + Arrays.toString(Action.values()));
        }
    }

    private Replicable<?, ?> getReplicable() {
        return replicableServiceTracker.getService();
    }

    private void deregisterClientWithReplicationService(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ReplicaDescriptor replica = getReplicaDescriptor(req);
        getReplicationService().unregisterReplica(replica);
        logger.info("Deregistered replication client with this server " + replica.getIpAddress());
        resp.setContentType("text/plain");
        resp.getWriter().print(replica.getUuid());
    }

    private void registerClientWithReplicationService(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        ReplicaDescriptor replica = getReplicaDescriptor(req);
        getReplicationService().registerReplica(replica);
        resp.setContentType("text/plain");
        resp.getWriter().print(replica.getUuid());
    }

    private ReplicaDescriptor getReplicaDescriptor(HttpServletRequest req) throws UnknownHostException {
        InetAddress ipAddress = InetAddress.getByName(req.getRemoteAddr());
        UUID uuid = UUID.fromString(req.getParameter(SERVER_UUID));
        String additional = req.getParameter(ADDITIONAL_INFORMATION);
        logger.info("Registered new replica " + ipAddress + " " + uuid.toString() + " " + additional);
        return new ReplicaDescriptor(ipAddress, uuid, additional);
    }
}
