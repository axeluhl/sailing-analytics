package com.sap.sailing.expeditionconnector;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.expeditionconnector.impl.ExpeditionMessageParser;
import com.sap.sailing.udpconnector.UDPReceiver;

/**
 * When run, starts receiving UDP packets expected to be in the format Expedition writes and notifies registered
 * listeners about all contents received. To stop receiving, call {@link #stop}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class UDPExpeditionReceiver extends UDPReceiver<ExpeditionMessage, ExpeditionListener> {
    /**
     * Remembers, per boat ID, the milliseconds difference between the time the message was received
     * and the GPS time stamp provided by the message.
     */
    private final Map<Integer, Long> timeStampOfLastMessageReceived;
    
    private final ExpeditionMessageParser parser;

    /**
     * An optional lookup facility for device identifiers for a {@link ExpeditionMessage#getBoatID() boat ID} as
     * received in an Expedition UDP stream; if device identifiers are returned by a non-{@code null} registry, GPS /
     * sensor fixes will be assembled upon receiving them, and they will be submitted to the {@link SensorFixStore}.
     * Otherwise, only wind data will be forwarded.
     */
    private final DeviceRegistry deviceRegistry;

    /**
     * Launches a listener and dumps messages received to the console
     * @param args 0: port to listen on
     *  
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        UDPExpeditionReceiver receiver = new UDPExpeditionReceiver(Integer.valueOf(args[0]));
        receiver.addListener(new ExpeditionListener() {
            @Override
            public void received(ExpeditionMessage message) {
                System.out.println(message);
            }
        }, /* validMessagesOnly */ false);
        receiver.run();
    }

    /**
     * You need call {@link #run} to actually start receiving events. To do this asynchronously,
     * start this object in a new thread.
     */
    public UDPExpeditionReceiver(int listeningOnPort) throws SocketException {
        this(listeningOnPort, /* DeviceRegistry */ null);
    }
    
    /**
     * You need call {@link #run} to actually start receiving events. To do this asynchronously, start this object in a
     * new thread.
     * 
     * @param deviceRegistry
     *            if not {@code null}, a look-up for each message's {@link ExpeditionMessage#getBoatID() boat ID} will
     *            be performed; if valid device identifiers for GPS and sensor tracks are returned, this receiver will
     *            produce GPS and sensor fixes, respectively, with the device identifiers produced by the
     *            {@link DeviceRegistry} and submit them to the {@link SensorFixStore}.
     */
    public UDPExpeditionReceiver(int listeningOnPort, DeviceRegistry deviceRegistry) throws SocketException {
        super(listeningOnPort);
        this.deviceRegistry = deviceRegistry;
        this.timeStampOfLastMessageReceived = new HashMap<Integer, Long>();
        parser = new ExpeditionMessageParser(this);
        addListener(msg->produceAndStoreOptionalFixes(msg), /* validMessagesOnly */ true);
    }
    
    private void produceAndStoreOptionalFixes(ExpeditionMessage msg) {
        final ExpeditionGpsDeviceIdentifier gpsDeviceIdentifier = deviceRegistry.getGpsDeviceIdentifier(msg.getBoatID());
        if (gpsDeviceIdentifier != null) {
            tryToProduceAndStoreGpsFix(msg, gpsDeviceIdentifier);
        }
        final ExpeditionSensorDeviceIdentifier sensorDeviceIdentifier = deviceRegistry.getSensorDeviceIdentifier(msg.getBoatID());
        if (sensorDeviceIdentifier != null) {
            tryToProduceAndStoreSensorFix(msg, sensorDeviceIdentifier);
        }
    }

    /**
     * If this message completes the set of data required to produce a sensor fix, do so and
     * store in the {@link DeviceRegistry#getSensorFixStore() sensor fix store}.
     */
    private void tryToProduceAndStoreSensorFix(ExpeditionMessage msg, ExpeditionSensorDeviceIdentifier sensorDeviceIdentifier) {
        final DoubleVectorFix fix = null; // TODO try to produce a new fix from what we have if we have enough new evidence
        if (fix != null) {
            deviceRegistry.getSensorFixStore().storeFix(sensorDeviceIdentifier, fix);
        }
    }

    /**
     * If this message completes the set of data required to produce a GPS fix, do so and
     * store in the {@link DeviceRegistry#getSensorFixStore() sensor fix store}.
     */
    private void tryToProduceAndStoreGpsFix(ExpeditionMessage msg, ExpeditionGpsDeviceIdentifier gpsDeviceIdentifier) {
        final GPSFixMoving fix = null; // TODO assemble GPS fix from what we have if we have enough new evidence
        if (fix != null) {
            deviceRegistry.getSensorFixStore().storeFix(gpsDeviceIdentifier, fix);
        }
    }

    public Map<Integer, Long> getTimeStampOfLastMessageReceived() {
        return timeStampOfLastMessageReceived;
    }
    
    protected ExpeditionMessageParser getParser() {
        return parser;
    }

}
