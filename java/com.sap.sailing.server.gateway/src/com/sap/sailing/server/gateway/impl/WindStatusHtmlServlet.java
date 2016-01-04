package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.IgtimiWindListener;
import com.sap.sse.common.TimePoint;

/**
 * Shows the state of wind receivers regardless of them being attached to a race. 
 * Currently Expedition and Igtimi are supported.
 * 
 * @author Simon Marcel Pamies
 *
 */
public class WindStatusHtmlServlet extends WindStatusServlet implements IgtimiWindListener, BulkFixReceiver {

    private static final long serialVersionUID = 6091476602985063675L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String reinitializeWindReceiverParameter = req.getParameter(PARAM_RELOAD_WIND_RECEIVER);
        initializeWindReceiver(reinitializeWindReceiverParameter != null && reinitializeWindReceiverParameter.equalsIgnoreCase("true"));
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Wind Status</title>");
        out.println("<meta http-equiv=refresh content='10; url="+req.getRequestURI()+"'>");
        out.println("</head>");
        out.println("<body>");
        out.println("<p>Reload wind connectors with parameter <a href=\"/sailingserver/windStatus?reloadWindReceiver=true\">reloadWindReceiver=true</a>. This will force a connection reset and a reloading of the wind receivers.</p>");
        out.println("<h3>Igtimi Wind Status ("+getIgtimiMessagesRawCount()+" raw messages received)</h3>");
        if (getLastIgtimiMessages() != null && !getLastIgtimiMessages().isEmpty()) {
            for(Entry<String, Deque<IgtimiMessageInfo>> deviceAndMessagesList: getLastIgtimiMessages().entrySet()) {
                final Deque<IgtimiMessageInfo> copyOfLastIgtimiMessages;
                synchronized (deviceAndMessagesList.getValue()) {
                    copyOfLastIgtimiMessages = new ArrayDeque<>(deviceAndMessagesList.getValue());
                }
                out.println("Windbot: <b>" + deviceAndMessagesList.getKey() + "</b>");
                if(copyOfLastIgtimiMessages.size() > 0) {
                    TimePoint latestTimePoint = copyOfLastIgtimiMessages.peek().getWind().getTimePoint(); 
                    long lastFixDiffInMs = System.currentTimeMillis() - latestTimePoint.asMillis();
                    out.println("&nbsp;&nbsp;&nbsp;&nbsp;Last fix:");
                    if(lastFixDiffInMs / 1000 < 60) {
                        out.println(lastFixDiffInMs / 1000 +"s ago");
                    } else {
                        out.println("<span style=\"color:red;\">" + lastFixDiffInMs / 1000 +"min ago</span>");
                    }
                }
                out.println("<br/>");
                Iterator<IgtimiMessageInfo> messageIt = copyOfLastIgtimiMessages.iterator();
                while (messageIt.hasNext()){
                    IgtimiMessageInfo message = messageIt.next();
                    out.println(message);
                    out.println("<br/>");
                }
                out.println("<br/>");
            }
        } else {
            if (getIgtimiMessagesRawCount() == 0) {
                out.println("<i>No Igtimi messages received so far!</i>");
            } else {
                out.println("<i>"+getIgtimiMessagesRawCount()+" Igtimi message bunch has been received but not enough messages to generate wind information.</i>");
            }
        }
        out.println("<h3>Expedition Wind Status</h3>");
        if (getLastExpeditionMessages() != null && !getLastExpeditionMessages().isEmpty()) {
            final List<ExpeditionMessageInfo> copyOfLastExpeditionMessages;
            synchronized (getLastExpeditionMessages()) {
                copyOfLastExpeditionMessages = new ArrayList<>(getLastExpeditionMessages());
            }
            int expeditionMsgCounter = 0;
            for (ListIterator<ExpeditionMessageInfo> iterator = copyOfLastExpeditionMessages.listIterator(copyOfLastExpeditionMessages.size()); iterator.hasPrevious();) {
                expeditionMsgCounter++;
                ExpeditionMessageInfo message = iterator.previous();
                out.println(message);
                out.println("<br/>");
                if (expeditionMsgCounter >= NUMBER_OF_MESSAGES_TO_SHOW) {
                    break;
                }
            }
        } else {
            out.println("<i>No Expedition messages received so far!</i>");
        }
        out.println("</body>");
        out.println("</html>");
        out.close();
    }

}
