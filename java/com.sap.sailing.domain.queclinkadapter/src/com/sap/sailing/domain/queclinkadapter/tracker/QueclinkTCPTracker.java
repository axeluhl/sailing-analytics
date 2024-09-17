package com.sap.sailing.domain.queclinkadapter.tracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.queclinkadapter.ByteStreamToMessageStreamConverter;
import com.sap.sailing.domain.queclinkadapter.FRIReport;
import com.sap.sailing.domain.queclinkadapter.Message;
import com.sap.sailing.domain.queclinkadapter.MessageVisitor;
import com.sap.sailing.domain.queclinkadapter.PositionRelatedReport;
import com.sap.sailing.domain.queclinkadapter.impl.AbstractMessageVisitor;
import com.sap.sailing.domain.queclinkadapter.impl.PositionRelatedReportToGPSFixConverter;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogtracking.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneImeiIdentifierImpl;

/**
 * Listens on a given port for incoming TCP connections, using an asynchronous server socket. When a connection is
 * established, an asynchronous socket channel is used for reading data from that socket until the socket is closed. The
 * data read is assumed to consist of Queclink GL300 messages. When the device's IMEI is found in any of the messages
 * received, the asynchronous socket channel through which it was received is associated to that IMEI. This way, when
 * clients would like to send messages to the device, they can do so as long as the socket is still connected.<p>
 * 
 * Calling the {@link #stop} method will close all existing socket connections and will stop listening for new
 * incoming connections.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class QueclinkTCPTracker {
    private static final Logger logger = Logger.getLogger(QueclinkTCPTracker.class.getName());

    private final AsynchronousServerSocketChannel serverSocketChannel;
    private final ConcurrentMap<String, AsynchronousSocketChannel> socketChannelsByImei;
    private final SensorFixStore sensorFixStore;
    private final Charset charset;
    private final static PositionRelatedReportToGPSFixConverter gpsFixFactory = new PositionRelatedReportToGPSFixConverter();
    
    public QueclinkTCPTracker(int port, Charset charset, SensorFixStore sensorFixStore) throws IOException {
        this.sensorFixStore = sensorFixStore;
        this.charset = charset;
        socketChannelsByImei = new ConcurrentHashMap<>();
        serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        handleAccept(port);
    }
    
    public void sendToDevice(SmartphoneImeiIdentifier deviceIdentifier, Message message) {
        final String imei = deviceIdentifier.getImei();
        final AsynchronousSocketChannel channel = socketChannelsByImei.get(imei);
        if (channel == null) {
            throw new IllegalStateException("No connection exists to the device with IMEI "+imei+"; cannot send message "+message.getMessageString());
        }
        channel.write(charset.encode(CharBuffer.wrap(message.getMessageString()))); // ignore the Future object returned
    }
    
    private void handleConnection(final AsynchronousSocketChannel socketChannel, final int port) {
        final MessageVisitor<Void> storeFixVisitor = new AbstractMessageVisitor<Void>() {
            @Override
            public Void visit(FRIReport friReport) {
                socketChannelsByImei.putIfAbsent(friReport.getImei(), socketChannel);
                final SmartphoneImeiIdentifier deviceIdentifier = new SmartphoneImeiIdentifierImpl(friReport.getImei());
                for (final PositionRelatedReport prr : friReport.getPositionRelatedReports()) {
                    if (prr.getPosition() != null) {
                        sensorFixStore.storeFix(deviceIdentifier, gpsFixFactory.createGPSFixFromPositionRelatedReport(prr));
                    }
                }
                return null;
            }
        };
        final ByteBuffer buf = ByteBuffer.allocateDirect(8192);
        final ByteStreamToMessageStreamConverter converter = ByteStreamToMessageStreamConverter.create();
        final CompletionHandler<Integer, Integer> connectionHandler = new CompletionHandler<Integer, Integer>() {
            @Override
            public void completed(Integer result, Integer port) {
                if (result != -1) {
                    buf.flip();
                    try {
                        converter.convert(charset.decode(buf)).forEach(message->message.accept(storeFixVisitor));
                    } catch (ParseException e) {
                        logger.log(Level.SEVERE, "Error trying to convert messages receive through TCP on port "+port, e);
                    }
                } else {
                    // EOF reached; close the channel
                    try {
                        socketChannel.close();
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error trying to close TCP connection on port "+port, e);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, Integer port) {
                logger.log(Level.SEVERE, "Error trying to read from TCP connection on port "+port, exc);
            }
        };
        socketChannel.read(buf, /* attachment */ null, connectionHandler);
    }

    private void handleAccept(int port) {
        serverSocketChannel.accept(port, new CompletionHandler<AsynchronousSocketChannel, Integer>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Integer port) {
                handleAccept(port); // try to accept the next connection request asynchronously
                handleConnection(result, port);
            }

            @Override
            public void failed(Throwable exc, Integer port) {
                logger.log(Level.SEVERE, "Error trying to accept TCP connection on port "+port, exc);
            }
        });
    }

    public void stop() throws IOException {
        for (final AsynchronousSocketChannel socketChannel : socketChannelsByImei.values()) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error trying to close socket channel; continuing with other channels if any", e);
            }
        }
        serverSocketChannel.close();
    }
}
