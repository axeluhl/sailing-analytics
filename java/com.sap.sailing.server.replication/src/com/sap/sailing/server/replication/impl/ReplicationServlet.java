package com.sap.sailing.server.replication.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.Servlet;

/**
 * As the response to any type of <code>GET</code> request, sends a serialized copy of the {@link RacingEventService} to
 * the response's output stream.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class ReplicationServlet extends Servlet {
    private static final long serialVersionUID = 4835516998934433846L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectOutputStream oos = new ObjectOutputStream(resp.getOutputStream());
        getService().serializeForInitialReplication(oos);
    }
}
