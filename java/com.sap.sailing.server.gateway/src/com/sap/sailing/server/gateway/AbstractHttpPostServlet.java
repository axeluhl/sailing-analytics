package com.sap.sailing.server.gateway;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.server.interfaces.RacingEventService;

/**
 * Subclasses can be used to retrieve messages through an HTTP connection. The connection remains open until the client
 * closes the request stream. Of course, network errors can occur, so the connection may be closed for other reasons,
 * too. The client may not necessarily become aware of the connection breaking. Therefore, clients can send the string
 * "<ping>" terminated with a newline character through the request stream, and this servlet will respond with the
 * message "<pong>" on the output stream. The ping/pong messages interleave the message stream but never cut a single
 * message into several parts.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public abstract class AbstractHttpPostServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = -4275373434350250003L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        createHandler(resp).handleRequest();
    }
    
    @Override
    public RacingEventService getService() {
        return super.getService();
    }

    protected abstract HttpMessageSenderServletRequestHandler createHandler(HttpServletResponse resp) throws IOException;
}
