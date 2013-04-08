package com.sap.sailing.server.replication.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;
import com.sap.sailing.server.replication.ReplicaDescriptor;
import com.sap.sailing.server.replication.ReplicationService;

/**
 * As the response to any type of <code>GET</code> request, sends a serialized copy of the {@link RacingEventService} to
 * the response's output stream.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class ReplicationServlet extends SailingServerHttpServlet {
    private static final Logger logger = Logger.getLogger(ReplicationServlet.class.getName());
    
    private static final long serialVersionUID = 4835516998934433846L;
    
    public enum Action { REGISTER, INITIAL_LOAD }
    
    public static final String ACTION = "action";

    private ServiceTracker<ReplicationService, ReplicationService> replicationServiceTracker;
    
    public ReplicationServlet() throws Exception {
        BundleContext context = Activator.getDefaultContext();
        replicationServiceTracker = new ServiceTracker<ReplicationService, ReplicationService>(context, ReplicationService.class.getName(), null);
        replicationServiceTracker.open();
    }

    protected ReplicationService getReplicationService() {
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
        switch (Action.valueOf(action)) {
        case REGISTER:
            registerClientWithReplicationService(req, resp);
            break;
        case INITIAL_LOAD:
            ObjectOutputStream oos = new ObjectOutputStream(resp.getOutputStream());
            try {
                getService().serializeForInitialReplication(oos);
            } catch (Exception e) {
                logger.info("Error trying to serialize initial load for replication: "+e.getMessage());
                logger.log(Level.SEVERE, "doGet", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                e.printStackTrace(resp.getWriter());
            }
            break;
        default:
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action " + action + " not understood. Must be one of "
                    + Arrays.toString(Action.values()));
        }
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
        return new ReplicaDescriptor(ipAddress);
    }
}
