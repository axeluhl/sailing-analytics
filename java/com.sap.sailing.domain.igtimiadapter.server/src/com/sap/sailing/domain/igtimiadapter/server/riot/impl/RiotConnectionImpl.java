package com.sap.sailing.domain.igtimiadapter.server.riot.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.igtimi.IgtimiAPI.Token;
import com.igtimi.IgtimiData.ApparentWindAngle;
import com.igtimi.IgtimiData.ApparentWindSpeed;
import com.igtimi.IgtimiData.CourseOverGround;
import com.igtimi.IgtimiData.Data;
import com.igtimi.IgtimiData.DataMsg;
import com.igtimi.IgtimiData.DataPoint;
import com.igtimi.IgtimiData.GNSS_Position;
import com.igtimi.IgtimiData.GNSS_Quality;
import com.igtimi.IgtimiData.GNSS_Sat_Count;
import com.igtimi.IgtimiData.Heading;
import com.igtimi.IgtimiData.HeadingMagnetic;
import com.igtimi.IgtimiData.SpeedOverGround;
import com.igtimi.IgtimiDevice.DeviceCommand;
import com.igtimi.IgtimiDevice.DeviceManagement;
import com.igtimi.IgtimiDevice.DeviceManagementRequest;
import com.igtimi.IgtimiStream.AckResponse;
import com.igtimi.IgtimiStream.Authentication;
import com.igtimi.IgtimiStream.Authentication.AuthResponse;
import com.igtimi.IgtimiStream.ChannelManagement;
import com.igtimi.IgtimiStream.Msg;
import com.igtimi.IgtimiStream.ServerDisconnecting;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.igtimiadapter.ChannelManagementVisitor;
import com.sap.sailing.domain.igtimiadapter.DataPointVisitor;
import com.sap.sailing.domain.igtimiadapter.MsgVisitor;
import com.sap.sailing.domain.igtimiadapter.Sensor;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWA;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWS;
import com.sap.sailing.domain.igtimiadapter.datatypes.BatteryLevel;
import com.sap.sailing.domain.igtimiadapter.datatypes.COG;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsAltitude;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsLatLong;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsQualityHdop;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsQualitySatCount;
import com.sap.sailing.domain.igtimiadapter.datatypes.HDG;
import com.sap.sailing.domain.igtimiadapter.datatypes.HDGM;
import com.sap.sailing.domain.igtimiadapter.datatypes.SOG;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotConnection;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.util.ThreadPoolUtil;

public class RiotConnectionImpl implements RiotConnection {
    private static final Logger logger = Logger.getLogger(RiotConnectionImpl.class.getName());

    private final static ExtensionRegistry protobufExtensionRegistry = ExtensionRegistry.newInstance();
    private String serialNumber;

    /**
     * The {@code varint32} indicating the length of the next protobuf message in bytes is read into
     * this buffer. The buffer is sized to five bytes which is the maximum length a {@code varint32}
     * can consume.<p>
     * 
     * Once a complete {@code varint32} that a {@link CodedInputStream#readRawVarint32()} call can
     * read without exception, the value read is stored in {@link #nextMessageLength}, this buffer
     * is {@link ByteBuffer#clear() cleared}, and a new {@link #messageBuffer} with the exact size
     * of the next message is created. 
     */
    private final ByteBuffer messageLengthBuffer;
    
    /**
     * When a non-zero message length has been read and stored in {@link #nextMessageLength}, this
     * field holds a byte buffer of size {@link #nextMessageLength} into which {@link #dataReceived(ByteBuffer)}
     * updates the bytes it has read. When the buffer has been filled completely by {@link #dataReceived(ByteBuffer)},
     * the message is parsed using {@link Msg#parseFrom(ByteBuffer)} and {@link #processMessage(Msg) processed}.
     * The {@link #nextMessageLength} field is set to 0, putting this connection into message length reading-mode.
     * If more data is available in the buffer passed to {@link #dataReceived(ByteBuffer)}, reading continues.
     */
    private ByteBuffer messageBuffer;
    
    /**
     * If 0 (the initial value), we're waiting for the next <tt>varint32</tt> to be {@link #dataReceived(ByteBuffer)
     * read} into the {@link #messageLengthBuffer}. Otherwise, we're expecting to receive these many bytes
     * through {@link #dataReceived(ByteBuffer)} into {@link #messageBuffer}
     */
    private int nextMessageLength;
    
    private final SocketChannel socketChannel;
    
    private final RiotServerImpl riotServer;
    
    private String deviceGroupToken;

    private final ScheduledFuture<?> heartbeatSendingTask;
    
    RiotConnectionImpl(SocketChannel socketChannel, RiotServerImpl riotServer) {
        this.socketChannel = socketChannel;
        this.riotServer = riotServer;
        this.messageLengthBuffer = ByteBuffer.allocate(5);
        heartbeatSendingTask = scheduleHeartbeat();
    }
    
    private ScheduledFuture<?> scheduleHeartbeat() {
        return ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor().scheduleAtFixedRate(this::sendHeartbeat, 15, 15, TimeUnit.SECONDS);
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public String getDeviceGroupToken() {
        return deviceGroupToken;
    }

    @Override
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    @Override
    public void close() throws IOException {
        try {
            send(Msg.newBuilder().setChannelManagement(ChannelManagement.newBuilder().setDisconnect(ServerDisconnecting.newBuilder().setCode(500).setReason("Connection closed by server"))).build());
        } finally {
            heartbeatSendingTask.cancel(/* mayInterruptIfRunning */ false);
            socketChannel.close();
        }
    }
    
    @Override
    public void sendCommand(String command) throws IOException {
        send(Msg.newBuilder().setDeviceManagement(DeviceManagement.newBuilder().setRequest(DeviceManagementRequest.newBuilder()
                .setCommand(DeviceCommand.newBuilder().setText(command)))).build());
    }

    @Override
    public void dataReceived(ByteBuffer data) {
        data.flip();
        while (data.hasRemaining()) { // consume all data received
            if (nextMessageLength == 0) { // we're in message length reading mode
                final byte b = data.get();
                messageLengthBuffer.put(b);
                final int oldPosition = messageLengthBuffer.position();
                messageLengthBuffer.flip();
                try {
                    nextMessageLength = CodedInputStream.newInstance(messageLengthBuffer).readRawVarint32();
                    messageLengthBuffer.clear();
                    messageBuffer = ByteBuffer.allocate(nextMessageLength);
                } catch (IOException ioe) {
                    // varint32 still incomplete; wait for more bytes
                    messageLengthBuffer.limit(messageLengthBuffer.capacity());
                    messageLengthBuffer.position(oldPosition);
                }
            } else { // we're in message reading mode, and the messageBuffer is not full yet
                final byte[] copyBuffer = new byte[Math.min(data.remaining(), messageBuffer.remaining())];
                data.get(copyBuffer);
                messageBuffer.put(copyBuffer);
                if (!messageBuffer.hasRemaining()) { // data for message read completely
                    nextMessageLength = 0;
                    try {
                        messageBuffer.flip();
                        final Msg message = Msg.parseFrom(messageBuffer, protobufExtensionRegistry);       
                        processMessage(message);
                    } catch (InvalidProtocolBufferException e) {
                        logger.log(Level.SEVERE, "Error parsing message from device "+serialNumber, e);
                    }
                }
            }
        }
    }
    

    private void sendPositiveAuthResponse() throws IOException {
        final AuthResponse response = AuthResponse.newBuilder()
            .setTimestamp(System.currentTimeMillis())
            .setToken(Token.newBuilder().setDeviceGroupToken(getDeviceGroupToken()))
            .setAck(true)
            .setCode(200)
            .setReason("Authenticated")
            .build();
        send(Msg.newBuilder().setChannelManagement(ChannelManagement.newBuilder().setAuth(Authentication.newBuilder().setAuthResponse(response))).build());
    }
    
    /**
     * Serializes the {@code message} using protobuf, determines the length of the resulting
     * byte sequence, then writes this length to the {@link #socketChannel} as a {@code varint32},
     * followed by the bytes representing the actual message.
     */
    private void send(final AbstractMessage message) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        message.writeDelimitedTo(bos);
        final ByteBuffer buf = ByteBuffer.allocate(bos.size());
        buf.put(bos.toByteArray());
        buf.flip();
        socketChannel.write(buf);
    }
    
    private void processMessage(Msg message) {
        final List<Fix> fixes = new ArrayList<>(); // the fixes extracted from the message
        MsgVisitor.accept(message, new MsgVisitor() {
            @Override
            public void handleDeviceManagement(DeviceManagement deviceManagement) {
                serialNumber = deviceManagement.getSerialNumber();
            }
            
            @Override
            public void handleData(Data data) {
                for (final DataMsg dataMsg : data.getDataList()) {
                    serialNumber = dataMsg.getSerialNumber();
                    for (final DataPoint dataPoint : dataMsg.getDataList()) {
                        DataPointVisitor.accept(dataPoint, new DataPointVisitor() {
                            @Override
                            public void handleAwa(ApparentWindAngle awa) {
                                fixes.add(new AWA(TimePoint.of(awa.getTimestamp()), getSensor(), new DegreeBearingImpl(awa.getValue())));
                            }

                            @Override
                            public void handleAws(ApparentWindSpeed aws) {
                                fixes.add(new AWS(TimePoint.of(aws.getTimestamp()), getSensor(), new KilometersPerHourSpeedImpl(aws.getValue())));
                            }

                            @Override
                            public void handleCog(CourseOverGround cog) {
                                fixes.add(new COG(TimePoint.of(cog.getTimestamp()), getSensor(), new DegreeBearingImpl(cog.getValue())));
                            }

                            @Override
                            public void handleHdg(Heading hdg) {
                                fixes.add(new HDG(TimePoint.of(hdg.getTimestamp()), getSensor(), new DegreeBearingImpl(hdg.getValue())));
                            }

                            @Override
                            public void handleHdgm(HeadingMagnetic hdgm) {
                                fixes.add(new HDGM(TimePoint.of(hdgm.getTimestamp()), getSensor(), new DegreeBearingImpl(hdgm.getValue())));
                            }

                            @Override
                            public void handlePos(GNSS_Position pos) {
                                final Sensor sensor = getSensor();
                                fixes.add(new GpsLatLong(TimePoint.of(pos.getTimestamp()), sensor, new DegreePosition(pos.getLatitude(), pos.getLongitude())));
                                fixes.add(new GpsAltitude(TimePoint.of(pos.getTimestamp()), sensor, new MeterDistance(pos.getAltitude())));
                            }

                            @Override
                            public void handleSatq(GNSS_Quality hdop) {
                                fixes.add(new GpsQualityHdop(TimePoint.of(hdop.getTimestamp()), getSensor(), new MeterDistance(hdop.getValue())));
                            }

                            @Override
                            public void handleSatc(GNSS_Sat_Count satCount) {
                                fixes.add(new GpsQualitySatCount(TimePoint.of(satCount.getTimestamp()), getSensor(), satCount.getValue()));
                            }

                            @Override
                            public void handleNum(com.igtimi.IgtimiData.Number num) {
                                // This is expected to represent the battery state of charge (SOC) in percent
                                fixes.add(new BatteryLevel(TimePoint.of(num.getTimestamp()), getSensor(), num.getValue()));
                            }

                            @Override
                            public void handleSog(SpeedOverGround sog) {
                                fixes.add(new SOG(TimePoint.of(sog.getTimestamp()), getSensor(), new KilometersPerHourSpeedImpl(sog.getValue())));
                            }
                        });
                    }
                }
            }
            
            @Override
            public void handleChannelManagement(ChannelManagement channelManagement) {
                ChannelManagementVisitor.accept(channelManagement, new ChannelManagementVisitor() {
                    @Override
                    public void handleAuth(Authentication auth) {
                        if (auth.hasAuthRequest()) {
                            final Token token = auth.getAuthRequest().getToken();
                            if (token.hasDeviceGroupToken()) {
                                deviceGroupToken = token.getDeviceGroupToken();
                                logger.info("Received auth request from device "+serialNumber+" with device group token "+deviceGroupToken);
                                try {
                                    sendPositiveAuthResponse();
                                } catch (IOException e) {
                                    logger.log(Level.SEVERE, "Couldn't send authentication response to device "+getSerialNumber(), e);
                                }
                            }
                        }
                    }
                });
            }
            
            @Override
            public void handleAckResponse(AckResponse ackResponse) {
                logger.info("Received AckResponse from device "+getSerialNumber()+": "+ackResponse);
            }
        });
        riotServer.notifyListeners(fixes);
    }

    private Sensor getSensor() {
        return Sensor.create(getSerialNumber(), /* sub-device */ 0);
    }

    private void sendHeartbeat() {
        try {
            send(Msg.newBuilder().setChannelManagement(ChannelManagement.newBuilder().setHeartbeat(1)).build());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Problem while trying to send heartbeat message to device "+getSerialNumber(), e);
            throw new RuntimeException(e);
        }
    }
}
