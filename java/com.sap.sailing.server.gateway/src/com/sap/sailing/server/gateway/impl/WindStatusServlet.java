package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.LiveDataConnection;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.expeditionconnector.ExpeditionListener;
import com.sap.sailing.expeditionconnector.ExpeditionMessage;
import com.sap.sailing.expeditionconnector.ExpeditionWindTrackerFactory;
import com.sap.sailing.expeditionconnector.UDPExpeditionReceiver;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;

/**
 * Shows the state of wind receivers regardless of them being attached to a race. Currently Expedition and Igtimi are supported.
 * 
 * @author Simon Marcel Pamies
 *
 */
public class WindStatusServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = -6791613843435003810L;
    
    private static List<ExpeditionMessageInfo> lastExpeditionMessages;
    private static List<IgtimiMessageInfo> lastIgtimiMessages;

    final private Map<Integer, ExpeditionMessageInfo> lastMessageInfosPerBoat;

    private static boolean isExpeditionListenerRegistered;
    private static boolean isIgtimiListenerRegistered;
    
    private static long MIN_TIME_SINCE_LAST_MESSAGE = 5 * 1000; // 5s; 
    private static long MAX_TIME_SINCE_LAST_MESSAGE = 2 * 60 * 60 * 1000; // 2 hour
    private static long MAX_TIME_TO_KEEP_OLD_MESSAGES = 5 * 60 * 1000; // 5 minutes worth of data

    public WindStatusServlet() {
        super();
        lastMessageInfosPerBoat = new HashMap<Integer, ExpeditionMessageInfo>();
        isExpeditionListenerRegistered = false;
        isIgtimiListenerRegistered = false;
    }
    
    private void initializeWindReceiver() {
        if(!isExpeditionListenerRegistered) {
            isExpeditionListenerRegistered = registerExpeditionListener();
            lastExpeditionMessages = new ArrayList<WindStatusServlet.ExpeditionMessageInfo>();
        }
        
        if (!isIgtimiListenerRegistered) {
            isIgtimiListenerRegistered = registerIgtimiListener();
            lastIgtimiMessages = new ArrayList<WindStatusServlet.IgtimiMessageInfo>();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        initializeWindReceiver();
        resp.setContentType("text/html");

        PrintWriter out = resp.getWriter();
        
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Wind Status</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<input type='button' value='Refresh' onClick='window.location.reload()'><br/>");
        out.println("<h3>Expedition Wind Status</h3>");
        if (lastExpeditionMessages.size()>0) {
            for (ExpeditionMessageInfo message : lastExpeditionMessages) {
                out.println(message);
            }
        } else {
            out.println("<div>No Expedition messages received so far!</div>");
        }
        out.println("<h3>Igtimi Wind Status</h3><br/>");
        if (lastIgtimiMessages.size()>0) {
            out.println("<table><tr><td><b>Last General</b></td><td><b>Last Wind</b></td></tr><tr><td valign=top>");
            for (ListIterator<IgtimiMessageInfo> iterator = WindStatusServlet.lastIgtimiMessages.listIterator(WindStatusServlet.lastIgtimiMessages.size()); iterator.hasPrevious();) {
                IgtimiMessageInfo message = iterator.previous();
                if (message.receivedFix.getType() != Type.AWA && message.receivedFix.getType() != Type.AWS) {
                    out.println(message);
                    out.println("<br/>");
                }
            }
            out.println("</td><td valign=top>");
            for (ListIterator<IgtimiMessageInfo> iterator = WindStatusServlet.lastIgtimiMessages.listIterator(WindStatusServlet.lastIgtimiMessages.size()); iterator.hasPrevious();) {
                IgtimiMessageInfo message = iterator.previous();
                if (message.receivedFix.getType() == Type.AWA || message.receivedFix.getType() == Type.AWS) {
                    out.println(message);
                    out.println("<br/>");
                }
            }
            out.println("</td><tr></table>");
        } else {
            out.println("<div>No Igtimi messages received so far!</div>");
        }
        out.println("</body>");
        out.println("</html>");
        out.close();
    }

    private PrintWriter dummy(HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Expedition Wind Status</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h3>Expedition Wind Status</h3>");
       
        if(lastMessageInfosPerBoat.size() > 0) {
            Date now = new Date(); 
            List<Integer> messagesToDrop = new ArrayList<Integer>();
            for(ExpeditionMessageInfo info: lastMessageInfosPerBoat.values()) {
                out.println("Boat-No:" + "&nbsp;" + info.boatID);
                out.println("<br/>");
                long timeSinceLastMessageInMs = now.getTime() - info.messageReceivedAt.getTime();
                if(timeSinceLastMessageInMs > MAX_TIME_SINCE_LAST_MESSAGE) {
                    messagesToDrop.add(info.boatID);
                } else if(timeSinceLastMessageInMs > MIN_TIME_SINCE_LAST_MESSAGE) {
                    long hours = TimeUnit.MILLISECONDS.toHours(timeSinceLastMessageInMs);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(timeSinceLastMessageInMs) - TimeUnit.HOURS.toMinutes(hours);
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(timeSinceLastMessageInMs) - (TimeUnit.MINUTES.toSeconds(minutes) + TimeUnit.HOURS.toSeconds(hours));
                    out.println("Time since last message:" + "&nbsp;" + String.format("%02d:%02d:%02d", hours, minutes, seconds));
                    out.println("<br/>");
                }
                out.println("Last message received:" + "&nbsp;" + info.messageReceivedAt.toString());
                out.println("<br/>");
                out.println("Last message:" + "&nbsp;" + info.message.getOriginalMessage());
                out.println("<br/>");
                GPSFix gpsFix = info.message.getGPSFix();
                out.println("Has GPS-Fix:" + "&nbsp;" + (gpsFix != null ? gpsFix.toString() : "no"));
                out.println("<br/>");
                SpeedWithBearing trueWind = info.message.getTrueWind();
                out.println("Has TrueWind:" + "&nbsp;" + (trueWind != null ? trueWind.toString() : "no"));
                out.println("<br/><br/>");
            }
            for(Integer boatID: messagesToDrop) {
                lastMessageInfosPerBoat.remove(boatID);
            }
        } else {
            out.println("No expedition wind sources available.");
        }
        return out;
    }

    private boolean registerIgtimiListener() {
        boolean result = false;
        ServiceTracker<IgtimiConnectionFactory, IgtimiConnectionFactory> igtimiServiceTracker = new ServiceTracker<IgtimiConnectionFactory, IgtimiConnectionFactory>(getContext(), IgtimiConnectionFactory.class, null);
        igtimiServiceTracker.open();
        IgtimiConnectionFactory igtimiConnectionFactory = igtimiServiceTracker.getService();
        for (Account account : igtimiConnectionFactory.getAllAccounts()) {
            if (account.getUser() != null) {
                IgtimiConnection igtimiConnection = igtimiConnectionFactory.connect(account);
                try {
                    LiveDataConnection liveDataConnection = igtimiConnection.getOrCreateLiveConnection(igtimiConnection.getWindDevices());
                    result = true;
                    liveDataConnection.addListener(new BulkFixReceiver() {
                        @Override
                        public void received(Iterable<Fix> fixes) {
                            for (Fix fix : fixes) {
                                TimePoint messagesReceivedAt = MillisecondsTimePoint.now();
                                if (fix != null) {
                                    IgtimiMessageInfo message = new IgtimiMessageInfo();
                                    message.receivedFix = fix;
                                    message.messageReceivedAt = messagesReceivedAt;
                                    // remove last message if it is old enough
                                    if (WindStatusServlet.lastIgtimiMessages != null) {
                                        if (WindStatusServlet.lastIgtimiMessages.size() > 100) {
                                            WindStatusServlet.lastIgtimiMessages.remove(0);
                                        }
                                        lastIgtimiMessages.add(message);
                                    }
                                }
                            }
                        }
                    });
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
                    if(message != null && message.getBoatID() >= 0) {
                        ExpeditionMessageInfo info = new ExpeditionMessageInfo();
                        info.boatID = message.getBoatID();
                        info.message = message;
                        info.messageReceivedAt = new Date();
                        lastMessageInfosPerBoat.put(info.boatID, info);
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
            return "";
        }
    }
    
    private class IgtimiMessageInfo {
        Fix receivedFix;
        TimePoint messageReceivedAt;
        
        public String toString() {
            return receivedFix.toString();
        }
    }
}
