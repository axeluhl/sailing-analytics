package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.IgtimiWindListener;
import com.sap.sailing.domain.igtimiadapter.LiveDataConnection;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.shared.IgtimiWindReceiver;
import com.sap.sailing.expeditionconnector.ExpeditionListener;
import com.sap.sailing.expeditionconnector.ExpeditionMessage;
import com.sap.sailing.expeditionconnector.ExpeditionWindTrackerFactory;
import com.sap.sailing.expeditionconnector.UDPExpeditionReceiver;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;
import com.sap.sse.common.TimePoint;

/**
 * Shows the state of wind receivers regardless of them being attached to a race. Currently Expedition and Igtimi are supported.
 * 
 * @author Simon Marcel Pamies
 *
 */
public class WindStatusServlet extends SailingServerHttpServlet implements IgtimiWindListener, BulkFixReceiver {
    private static final long serialVersionUID = -6791613843435003810L;
    
    private static final String PARAM_RELOAD_WIND_RECEIVER="reloadWindReceiver";

    private final int NUMBER_OF_MESSAGES_TO_SHOW=20;
    private final int NUMBER_OF_MESSAGES_PER_DEVICE_TO_SHOW=5;

    private static final DecimalFormat decimalFormatter2Digits = new DecimalFormat("#.##");
    private static final DecimalFormat decimalFormatter1Digit = new DecimalFormat("#.#");
    private static final DecimalFormat latLngDecimalFormatter = new DecimalFormat("#.######");
    private static final DateFormat dateTimeFormatter = DateFormat.getTimeInstance(DateFormat.LONG);
            
    private static List<ExpeditionMessageInfo> lastExpeditionMessages;
    
    private static Object lock = new Object();
    private static int igtimiRawMessageCount;
    private static Map<String, Deque<IgtimiMessageInfo>> lastIgtimiMessages;
    private static IgtimiWindReceiver igtimiWindReceiver;
    private static LiveDataConnection liveDataConnection;

    private static boolean isExpeditionListenerRegistered;
    private static boolean isIgtimiListenerRegistered;
    
    public WindStatusServlet() {
        super();
        isExpeditionListenerRegistered = false;
        isIgtimiListenerRegistered = false;
    }
    
    private void initializeWindReceiver(boolean reinitialize) {
        synchronized (lock) {
            if (!isExpeditionListenerRegistered || reinitialize) {
                isExpeditionListenerRegistered = registerExpeditionListener();
                lastExpeditionMessages = new ArrayList<WindStatusServlet.ExpeditionMessageInfo>();
            }
        }
        synchronized (lock) {
            if (!isIgtimiListenerRegistered || reinitialize) {
                if (reinitialize) {
                    try {
                        if (liveDataConnection != null) {
                            liveDataConnection.stop();
                            liveDataConnection.removeListener(igtimiWindReceiver);
                            liveDataConnection.removeListener(this);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isIgtimiListenerRegistered = registerIgtimiListener();
                lastIgtimiMessages = new HashMap<String, Deque<IgtimiMessageInfo>>();
                igtimiRawMessageCount = 0;
            }
        }
    }

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
        out.println("<h3>Igtimi Wind Status ("+igtimiRawMessageCount+" raw messages received)</h3>");
        if (lastIgtimiMessages != null && !lastIgtimiMessages.isEmpty()) {
            for(Entry<String, Deque<IgtimiMessageInfo>> deviceAndMessagesList: lastIgtimiMessages.entrySet()) {
                final Deque<IgtimiMessageInfo> copyOfLastIgtimiMessages;
                synchronized (deviceAndMessagesList.getValue()) {
                    copyOfLastIgtimiMessages = new ArrayDeque<>(deviceAndMessagesList.getValue());
                }
                out.println("Windbot: <b>" + deviceAndMessagesList.getKey() + "</b>");
                if(copyOfLastIgtimiMessages.size() > 0) {
                    TimePoint latestTimePoint = copyOfLastIgtimiMessages.peek().wind.getTimePoint(); 
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
            if (igtimiRawMessageCount == 0) {
                out.println("<i>No Igtimi messages received so far!</i>");
            } else {
                out.println("<i>"+igtimiRawMessageCount+" Igtimi message bunch has been received but not enough messages to generate wind information.</i>");
            }
        }
        out.println("<h3>Expedition Wind Status</h3>");
        if (lastExpeditionMessages != null && !lastExpeditionMessages.isEmpty()) {
            final List<ExpeditionMessageInfo> copyOfLastExpeditionMessages;
            synchronized (lastExpeditionMessages) {
                copyOfLastExpeditionMessages = new ArrayList<>(WindStatusServlet.lastExpeditionMessages);
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

    private boolean registerIgtimiListener() {
        boolean result = false;
        ServiceTracker<IgtimiConnectionFactory, IgtimiConnectionFactory> igtimiServiceTracker = new ServiceTracker<IgtimiConnectionFactory, IgtimiConnectionFactory>(getContext(), IgtimiConnectionFactory.class, null);
        igtimiServiceTracker.open();
        IgtimiConnectionFactory igtimiConnectionFactory = igtimiServiceTracker.getService();
        igtimiWindReceiver = new IgtimiWindReceiver(DeclinationService.INSTANCE);
        igtimiWindReceiver.addListener(this);
        for (Account account : igtimiConnectionFactory.getAllAccounts()) {
            if (account.getUser() != null) {
                IgtimiConnection igtimiConnection = igtimiConnectionFactory.connect(account);
                try {
                    liveDataConnection = igtimiConnection.getOrCreateLiveConnection(igtimiConnection.getWindDevices());
                    liveDataConnection.addListener(igtimiWindReceiver);
                    liveDataConnection.addListener(this);
                    result = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private boolean registerExpeditionListener() {
        boolean result = false;
        try {
            ServiceTracker<ExpeditionWindTrackerFactory, ExpeditionWindTrackerFactory> expeditionServiceTracker = new ServiceTracker<ExpeditionWindTrackerFactory, ExpeditionWindTrackerFactory>(
                    getContext(), ExpeditionWindTrackerFactory.class.getName(), null);
            expeditionServiceTracker.open();
            UDPExpeditionReceiver receiver = expeditionServiceTracker.getService().getOrCreateWindReceiverOnDefaultPort();
            receiver.addListener(new ExpeditionListener() {
                @Override
                public void received(ExpeditionMessage message) {
                    if (message != null && message.getBoatID() >= 0) {
                        ExpeditionMessageInfo info = new ExpeditionMessageInfo();
                        info.boatID = message.getBoatID();
                        info.message = message;
                        info.messageReceivedAt = new Date();
                        synchronized (lastExpeditionMessages) {
                            lastExpeditionMessages.add(info);
                        }
                    }
                }
            } 
            , /*validMessagesOnly*/ false);            
            result = true;
        } catch (SocketException e) {
            result = false;
        }
        return result;
    }
    
    private class ExpeditionMessageInfo {
        Integer boatID;
        ExpeditionMessage message;
        Date messageReceivedAt;
        
        public String toString() {
            if (message.getTrueWind() != null) {
                return messageReceivedAt.toString() + ": [" + boatID + "] Knots: " + message.getTrueWind().getKnots() + " From: " + getFromAsDegrees();
            }
            return messageReceivedAt.toString() + ": [" + boatID + "] " + message.getOriginalMessage();
        }

        private double getFromAsDegrees() {
            return message.getTrueWindBearing().reverse().getDegrees();
        }
    }
    
    private class IgtimiMessageInfo {
        private Wind wind;
        
        public IgtimiMessageInfo(Wind wind) {
            this.wind = wind;
        }
        
        public String toString() {
            String formatedInfo = "";
            if(wind.getTimePoint() != null) {
                formatedInfo += "Time: " + dateTimeFormatter.format(wind.getTimePoint().asDate());
            }
            if(wind.getPosition() != null) {
                formatedInfo += ", Pos: " + latLngDecimalFormatter.format(wind.getPosition().getLatDeg()) + " " + latLngDecimalFormatter.format(wind.getPosition().getLngDeg()); 
            }
            formatedInfo += ", Wind: " + decimalFormatter2Digits.format(wind.getKnots()) +"kn";
            if(wind.getFrom() != null) {
                formatedInfo += " from "+ decimalFormatter1Digit.format(wind.getFrom().getDegrees()) + "&deg;";
            }
            return formatedInfo;
        }
    }

    @Override
    public void windDataReceived(Wind wind, String deviceSerialNumber) {
        Deque<IgtimiMessageInfo> messagesPerDevice = lastIgtimiMessages.get(deviceSerialNumber);
        if(messagesPerDevice == null) {
            messagesPerDevice = new ArrayDeque<IgtimiMessageInfo>(NUMBER_OF_MESSAGES_PER_DEVICE_TO_SHOW);
            lastIgtimiMessages.put(deviceSerialNumber, messagesPerDevice);
        }
        messagesPerDevice.addFirst(new IgtimiMessageInfo(wind));
        if(messagesPerDevice.size() > NUMBER_OF_MESSAGES_PER_DEVICE_TO_SHOW) {
            messagesPerDevice.pollLast();
        }
    }
    
    @Override
    public void destroy() {
        if (liveDataConnection != null) {
            try {
                liveDataConnection.stop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                liveDataConnection = null;
                isIgtimiListenerRegistered = false;
            }
        }
    }

    @Override
    public void received(Iterable<Fix> fixes) {
        igtimiRawMessageCount += 1;
    }
}
