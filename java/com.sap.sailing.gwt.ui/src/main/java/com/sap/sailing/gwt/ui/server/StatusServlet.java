package com.sap.sailing.gwt.ui.server;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.replication.ReplicationStatus;

public class StatusServlet extends HttpServlet {
    private static final long serialVersionUID = -8896724182560416457L;

    protected <T> T getService(Class<T> clazz) {
        BundleContext context = Activator.getDefault();
        ServiceTracker<T, T> tracker = new ServiceTracker<T, T>(context, clazz, null);
        tracker.open();
        T service = tracker.getService();
        tracker.close();
        return service;
    }

    private RacingEventService getService(ServletContext servletContext) {
        return getService(RacingEventService.class);
    }
    
    private ReplicationService getReplicationService(ServletContext servletContext) {
        return getService(ReplicationService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final ServletContext servletContext = req.getServletContext();
        final JSONObject result = new JSONObject();
        final RacingEventService service = getService(servletContext);
        result.put("numberofracestorestore", service.getNumberOfTrackedRacesToRestore());
        result.put("numberofracesrestored", service.getNumberOfTrackedRacesRestored());
        final ReplicationService replicationService = getReplicationService(servletContext);
        final ReplicationStatus replicationStatus = replicationService == null ? null : replicationService.getStatus();
        if (replicationStatus != null) {
            result.put("replication", replicationStatus.toJSONObject());
        }
        final boolean available = service.getNumberOfTrackedRacesRestored() >= service.getNumberOfTrackedRacesToRestore() &&
                (replicationStatus == null || replicationStatus.isAvailable());
        result.put("available", available);
        resp.setStatus(available ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        resp.setContentType(MediaType.APPLICATION_JSON + ";charset=UTF-8");
        OutputStreamWriter out = new OutputStreamWriter(resp.getOutputStream());
        result.writeJSONString(out);
        out.close();
    }
}