package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.expeditionconnector.ExpeditionListener;
import com.sap.sailing.expeditionconnector.ExpeditionMessage;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;

public class ExpeditionWindMeasureStatusGetServlet extends SailingServerHttpServlet implements ExpeditionListener {
    private static final long serialVersionUID = -6791613843435009810L;

    private Map<Integer, ExpeditionMessageInfo> lastMessageInfosPerBoat;
    private boolean isExpeditionListenerRegistered;
    
    public ExpeditionWindMeasureStatusGetServlet() {
        super();
        lastMessageInfosPerBoat = new HashMap<Integer, ExpeditionMessageInfo>();
        isExpeditionListenerRegistered = false;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(!isExpeditionListenerRegistered) {
            isExpeditionListenerRegistered = registerExpeditionListener();
        }
        
        resp.setContentType("text/html");

        PrintWriter out = resp.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Expedition Wind Status</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h3>Expedition Wind Status</h3>");
        out.println("<br/>");
        for(ExpeditionMessageInfo info: lastMessageInfosPerBoat.values()) {
            out.println("Boat-No:" + "&nbsp;" + info.boatID);
            out.println("<br/>");
            out.println("Last message received:" + "&nbsp;" + info.messageReceivedAt.toString());
            out.println("<br/>");
            out.println("Last message:" + "&nbsp;" + info.message.getOriginalMessage());
            out.println("<br/>");
        }
        out.println("</body>");
        out.println("</html>");
        
        out.close();
    }

    private boolean registerExpeditionListener() {
        boolean result = false;
        try {
            getService().addExpeditionListener(this, false);
            result = true;
        } catch (SocketException e) {
            result = false;
        }
        return result;
    }
    
    @Override
    public void received(final ExpeditionMessage message) {
        if(message != null && message.getBoatID() >= 0) {
            ExpeditionMessageInfo info = new ExpeditionMessageInfo();
            info.boatID = message.getBoatID();
            info.message = message;
            info.messageReceivedAt = new Date();
            lastMessageInfosPerBoat.put(info.boatID, info);
        }
    }

    private class ExpeditionMessageInfo {
        Integer boatID;
        ExpeditionMessage message;
        Date messageReceivedAt;
    }
}
