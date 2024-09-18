package com.sap.sailing.domain.queclinkadapter.tracker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import com.sap.sailing.domain.queclinkadapter.ByteStreamToMessageStreamConverter;
import com.sap.sailing.domain.queclinkadapter.FRIReport;
import com.sap.sailing.domain.queclinkadapter.Message;
import com.sap.sailing.domain.queclinkadapter.MessageVisitor;
import com.sap.sailing.domain.queclinkadapter.impl.AbstractMessageVisitor;
import com.sap.sailing.domain.queclinkadapter.impl.PositionRelatedReportToGPSFixConverter;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogtracking.SmartphoneImeiIdentifier;
import com.sap.sailing.udpconnector.UDPMessage;
import com.sap.sailing.udpconnector.UDPMessageListener;
import com.sap.sailing.udpconnector.UDPMessageParser;
import com.sap.sailing.udpconnector.UDPReceiver;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class QueclinkUDPTracker implements UDPMessageListener<QueclinkUDPTracker.MessageAsUDPMessage> {
    private static final Logger logger = Logger.getLogger(QueclinkUDPTracker.class.getName());

    private final static PositionRelatedReportToGPSFixConverter gpsFixFactory = new PositionRelatedReportToGPSFixConverter();
    private final UDPReceiver<QueclinkUDPTracker.MessageAsUDPMessage, QueclinkUDPTracker> udpReceiver;
    private final Charset charset;
    private ByteStreamToMessageStreamConverter converter;
    private final SensorFixStore sensorFixStore;
    private final ConcurrentMap<String, Pair<DatagramSocket, SocketAddress>> socketAddressesByImei;
    
    static class MessageAsUDPMessage implements UDPMessage {
        private final Iterable<Message> message;
        private final DatagramSocket datagramSocket;
        private final SocketAddress sender;
        
        public MessageAsUDPMessage(Iterable<Message> iterable, DatagramSocket datagramSocket, SocketAddress sender) {
            this.message = iterable;
            this.datagramSocket = datagramSocket;
            this.sender = sender;
        }

        Iterable<Message> getMessages() {
            return message;
        }
        
        SocketAddress getSender() {
            return sender;
        }
        
        DatagramSocket getSocket() {
            return datagramSocket;
        }
        
        @Override
        public boolean isValid() {
            return true;
        }
    }

    /**
     * @param port if {@code 0}, the system will pick a port; to find out which port this tracker listens
     * on, use {@link #getPort}. It uses the default charset {@code ISO-8859-1} for decoding the byte stream
     * into characters.
     */
    public QueclinkUDPTracker(int port, SensorFixStore sensorFixStore) throws SocketException {
        this(port, Charset.forName("ISO-8859-1"), sensorFixStore);
    }
    
    /**
     * @param port if {@code 0}, the system will pick a port; to find out which port this tracker listens
     * on, use {@link #getPort}.
     */
    public QueclinkUDPTracker(int port, Charset charset, SensorFixStore sensorFixStore) throws SocketException {
        this.charset = charset;
        this.sensorFixStore = sensorFixStore;
        socketAddressesByImei = new ConcurrentHashMap<>();
        converter = ByteStreamToMessageStreamConverter.create();
        udpReceiver = new UDPReceiver<QueclinkUDPTracker.MessageAsUDPMessage, QueclinkUDPTracker>(port) {
            @Override
            protected UDPMessageParser<QueclinkUDPTracker.MessageAsUDPMessage> getParser() {
                return new UDPMessageParser<QueclinkUDPTracker.MessageAsUDPMessage>() {
                    @Override
                    public QueclinkUDPTracker.MessageAsUDPMessage parse(DatagramPacket p) throws IOException {
                        logger.fine(()->"Received a packet of length "+p.getLength()+" from "+p.getAddress());
                        try {
                            return new QueclinkUDPTracker.MessageAsUDPMessage(converter.convert(charset.decode(ByteBuffer.wrap(p.getData(), 0, p.getLength()))), getSocket(), p.getSocketAddress());
                        } catch (ParseException e) {
                            throw new IOException(e);
                        }
                    }
                };
            }
        };
        udpReceiver.addListener(this, /* validMessagesOnly */ true);
        final Thread t = new Thread(udpReceiver, "Queclink UDP receiver on port "+getPort());
        t.setDaemon(true);
        logger.info("Starting Queclink UDP listener thread for port "+getPort());
        t.start();
    }

    /**
     * Sends the {@code message} to the device identified by {@code deviceIdentifier}; this only works if messages
     * have already been received from that device by this tracker, and the socket connection is still open. Otherwise,
     * an {@link IllegalStateException} will be thrown.
     */
    public void sendToDevice(SmartphoneImeiIdentifier deviceIdentifier, Message message) throws IOException {
        final String imei = deviceIdentifier.getImei();
        final Pair<DatagramSocket, SocketAddress> socketAddress = socketAddressesByImei.get(imei);
        if (socketAddress == null) {
            throw new IllegalStateException("No connection exists to the device with IMEI "+imei+"; cannot send message "+message.getMessageString());
        }
        final ByteBuffer bytesToSend = charset.encode(message.getMessageString());
        final byte[] bytes = new byte[bytesToSend.limit()];
        bytesToSend.get(bytes);
        final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, socketAddress.getB());
        socketAddress.getA().send(packet);
    }
    
    @Override
    public void received(MessageAsUDPMessage message) {
        logger.fine(()->"Received a message with "+Util.size(message.getMessages())+" Queclink messages from "+message.getSender());
        final MessageVisitor<Void> storeFixVisitor = new AbstractMessageVisitor<Void>() {
            @Override
            public Void visit(FRIReport friReport) {
                socketAddressesByImei.putIfAbsent(friReport.getImei(), new Pair<>(message.getSocket(), message.getSender()));
                gpsFixFactory.ingestFixesToStore(sensorFixStore, friReport);
                return null;
            }
        };
        message.getMessages().forEach(m->m.accept(storeFixVisitor));
    }

    /**
     * Tells the port on which this UDP listener is listening. This is particularly useful when {@code 0}
     * was passed to the {@link #QueclinkUDPTracker(int) constructor} to let the system pick any free port.
     */
    public int getPort() {
        return udpReceiver.getPort();
    }

    public void stop() throws SocketException, IOException {
        udpReceiver.stop();
    }
}
