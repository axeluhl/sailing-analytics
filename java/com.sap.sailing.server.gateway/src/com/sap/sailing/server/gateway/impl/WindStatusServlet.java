package com.sap.sailing.server.gateway.impl;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.sap.sailing.expeditionconnector.ExpeditionTrackerFactory;
import com.sap.sailing.expeditionconnector.UDPExpeditionReceiver;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;
import com.sap.sse.common.Util;

/**
 * Shows the state of wind receivers regardless of them being attached to a race. Currently Expedition and Igtimi are supported.
 * 
 * @author Simon Marcel Pamies
 *
 */
public abstract class WindStatusServlet extends SailingServerHttpServlet implements IgtimiWindListener, BulkFixReceiver {
    private static final Logger logger = Logger.getLogger(WindStatusServlet.class.getName());
    private static final long serialVersionUID = -6791613843435003810L;
    
    protected static final String PARAM_RELOAD_WIND_RECEIVER="reloadWindReceiver";

    protected final int NUMBER_OF_MESSAGES_TO_SHOW=100;
    protected final int NUMBER_OF_MESSAGES_PER_DEVICE_TO_SHOW=10;

    protected static final DecimalFormat decimalFormatter2Digits = new DecimalFormat("#.##");
    protected static final DecimalFormat decimalFormatter1Digit = new DecimalFormat("#.#");
    protected static final DecimalFormat latLngDecimalFormatter = new DecimalFormat("#.######");
    protected static final DateFormat dateTimeFormatter = DateFormat.getTimeInstance(DateFormat.LONG);
            
    private static List<ExpeditionMessageInfo> lastExpeditionMessages;
    
    private static Object lock = new Object();
    private static int igtimiRawMessageCount;
    private static Map<String, Deque<IgtimiMessageInfo>> lastIgtimiMessages;
    private static IgtimiWindReceiver igtimiWindReceiver;
    private static Map<LiveDataConnection, IgtimiConnectionInfo> igtimiConnections;
    
    private static boolean isExpeditionListenerRegistered;
    private static boolean isIgtimiListenerRegistered;
    
    public WindStatusServlet() {
        super();
        isExpeditionListenerRegistered = false;
        isIgtimiListenerRegistered = false;
        igtimiConnections = new LinkedHashMap<>();
    }
    
    protected int getIgtimiMessagesRawCount() {
        return igtimiRawMessageCount;
    }
    
    protected Map<String, Deque<IgtimiMessageInfo>> getLastIgtimiMessages() {
        return lastIgtimiMessages;
    }
    
    protected List<ExpeditionMessageInfo> getLastExpeditionMessages() {
        return lastExpeditionMessages;
    }
    
    protected void initializeWindReceiver(boolean reinitialize) {
        synchronized (lock) {
            if (!isExpeditionListenerRegistered || reinitialize) {
                isExpeditionListenerRegistered = registerExpeditionListener();
                lastExpeditionMessages = new ArrayList<WindStatusServlet.ExpeditionMessageInfo>();
            }
        }
        synchronized (lock) {
            if (!isIgtimiListenerRegistered || reinitialize) {
                if (reinitialize) {
                    if (!igtimiConnections.isEmpty()) {
                        for (Map.Entry<LiveDataConnection, IgtimiConnectionInfo> entry: igtimiConnections.entrySet()) {
                            LiveDataConnection igtimiConnection = entry.getKey();
                            try {
                                if (igtimiConnection != null) {
                                    igtimiConnection.stop();
                                    igtimiConnection.removeListener(igtimiWindReceiver);
                                    igtimiConnection.removeListener(this);
                                    
                                }
                            } catch (Exception e) {
                                logger.log(Level.WARNING, "Exception trying to stop Igtimi connection "+igtimiConnection, e);
                            }
                        }
                        igtimiConnections.clear();
                    }
                }
                lastIgtimiMessages = new HashMap<String, Deque<IgtimiMessageInfo>>();
                isIgtimiListenerRegistered = registerIgtimiListener();
                igtimiRawMessageCount = 0;
            }
        }
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
                    LiveDataConnection newIgtimiConnection = igtimiConnection.getOrCreateLiveConnection(igtimiConnection.getWindDevices());
                    newIgtimiConnection.addListener(igtimiWindReceiver);
                    newIgtimiConnection.addListener(this);
                    IgtimiConnectionInfo newIgtimiConnectionInfo = new IgtimiConnectionInfo(
                            newIgtimiConnection, account.getUser().getEmail(),
                            igtimiConnection.getWindDevices());
                    igtimiConnections.put(newIgtimiConnection, newIgtimiConnectionInfo);
                    result = true;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Exception trying to stop Igtimi connection "+igtimiConnection, e);
                }
            }
        }
        return result;
    }

    private boolean registerExpeditionListener() {
        boolean result = false;
        try {
            ServiceTracker<ExpeditionTrackerFactory, ExpeditionTrackerFactory> expeditionServiceTracker = new ServiceTracker<ExpeditionTrackerFactory, ExpeditionTrackerFactory>(
                    getContext(), ExpeditionTrackerFactory.class.getName(), null);
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

    protected class IgtimiConnectionInfo {
        private final LiveDataConnection igtimiLiveConnection;
        private final String accountName;
        private final Iterable<String> deviceIDs;
        
        public IgtimiConnectionInfo(LiveDataConnection newIgtimiConnection, String accountName, Iterable<String> deviceIDs) {
            super();
            this.igtimiLiveConnection = newIgtimiConnection;
            this.accountName = accountName;
            final List<String> deviceIDsList = new ArrayList<>();
            this.deviceIDs = deviceIDsList;
            Util.addAll(deviceIDs, deviceIDsList);
        }

        public InetSocketAddress getRemoteAddress() {
            return igtimiLiveConnection.getRemoteAddress();
        }

        public String getAccountName() {
            return accountName;
        }

        public Iterable<String> getDeviceIDs() {
            return deviceIDs;
        }
    }

    protected class ExpeditionMessageInfo {
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
    
    protected class IgtimiMessageInfo {
        private Wind wind;
        
        public IgtimiMessageInfo(Wind wind) {
            this.wind = wind;
        }
        
        public Wind getWind() {
            return wind;
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
        if (messagesPerDevice == null) {
            messagesPerDevice = new ArrayDeque<IgtimiMessageInfo>(NUMBER_OF_MESSAGES_PER_DEVICE_TO_SHOW);
            lastIgtimiMessages.put(deviceSerialNumber, messagesPerDevice);
        }
        synchronized (messagesPerDevice) {
            messagesPerDevice.addFirst(new IgtimiMessageInfo(wind));
            if (messagesPerDevice.size() > NUMBER_OF_MESSAGES_PER_DEVICE_TO_SHOW) {
                messagesPerDevice.pollLast();
            }
        }
    }
    
    @Override
    public void destroy() {
        for (Map.Entry<LiveDataConnection, IgtimiConnectionInfo> entry: igtimiConnections.entrySet()) {
            LiveDataConnection igtimiConnection = entry.getKey();
            try {
                igtimiConnection.stop();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception trying to stop Igtimi connection "+igtimiConnection, e);
            }
        }
        igtimiConnections.clear();
        isIgtimiListenerRegistered = false;
    }

    @Override
    public void received(Iterable<Fix> fixes) {
        igtimiRawMessageCount += 1;
    }

    public static Map<LiveDataConnection, IgtimiConnectionInfo> getIgtimiConnections() {
        return igtimiConnections;
    }
}
