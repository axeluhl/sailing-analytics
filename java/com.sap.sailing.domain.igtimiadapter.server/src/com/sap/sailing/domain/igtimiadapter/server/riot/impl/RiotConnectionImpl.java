package com.sap.sailing.domain.igtimiadapter.server.riot.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.igtimi.IgtimiAPI.Token;
import com.igtimi.IgtimiData.Data;
import com.igtimi.IgtimiData.DataMsg;
import com.igtimi.IgtimiDevice.DeviceCommand;
import com.igtimi.IgtimiDevice.DeviceManagement;
import com.igtimi.IgtimiDevice.DeviceManagementRequest;
import com.igtimi.IgtimiStream.AckResponse;
import com.igtimi.IgtimiStream.Authentication;
import com.igtimi.IgtimiStream.Authentication.AuthResponse;
import com.igtimi.IgtimiStream.ChannelManagement;
import com.igtimi.IgtimiStream.Msg;
import com.igtimi.IgtimiStream.ServerDisconnecting;
import com.sap.sailing.domain.igtimiadapter.ChannelManagementVisitor;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.MsgVisitor;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotConnection;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.util.ThreadPoolUtil;

public class RiotConnectionImpl implements RiotConnection {
    private static final Logger logger = Logger.getLogger(RiotConnectionImpl.class.getName());

    private final static ExtensionRegistry protobufExtensionRegistry = ExtensionRegistry.newInstance();
    private String serialNumber;

    private String deviceGroupToken;
    
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
    
    private final ScheduledFuture<?> heartbeatSendingTask;
    
    private TimePoint lastHeartbeatReceivedAt;
    
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
    public TimePoint getLastHeartbeatReceivedAt() {
        return lastHeartbeatReceivedAt;
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
                        processMessage(message); // extract device serial number and device group token and send auth response for auth request
                        riotServer.notifyListeners(message, serialNumber);
                    } catch (InvalidProtocolBufferException e) {
                        logger.log(Level.SEVERE, "Error parsing message from device "+serialNumber, e);
                    }
                }
            }
        }
    }

    /**
     * Here is the code in the firmware handling the authentication response:
     * 
     * <pre>
     * 
    UT_PL_Statusv2Payload* status = (UT_PL_Statusv2Payload*) payload;
      if (status->payloadType == UT_PL_STATUS_AUTH)
      {
        if (status->data.auth.response)
        {
          if (status->data.auth.success)
          {
            LOG(LINF, LG_RIOT, "Authentication successful (%s)", status->data.auth.authType == UT_PL_STATUS_AUTH_USER ? "user" : "device");
            
            utConnectionManager_SendDeviceData();
            
            if (status->data.auth.authType == UT_PL_STATUS_AUTH_DEVICE)
            {
              if (ut_StringsEqualIgnoreCase(status->data.auth.token, DEFAULT_DGT))
              {
                uTrackConfig.usingDefaultDgt = true;
                forward = false;
              }
              else if (deviceAuthState == DeviceAuthState_Authenticating)
              {
                uTrackConfig.usingDefaultDgt = false;
                forward = false;
                if (!ut_StringsEqualIgnoreCase(status->data.auth.token, uTrackConfig.core.body.deviceGroupToken))
                {
                  bool success = false;
                  strcpy(uTrackConfig.core.body.deviceGroupToken, status->data.auth.token);
                  success = utEEPROM_setNewDeviceGroupToken(uTrackConfig.core.body.deviceGroupToken);
                  LOG(LINF, LG_RIOT, "Loaded new device group token: %s", success ? "YES" : "NO");
                }
              }
              else
              {
                uTrackConfig.usingDefaultDgt = false;
              }
            }
            deviceAuthState = DeviceAuthState_Authenticated;
          } 
          else 
          {
            LOG(LWRN, LG_RIOT, "Authentication failed with code: %d (%s)", status->data.auth.code, status->data.auth.authType == UT_PL_STATUS_AUTH_USER ? "user" : "device");
            deviceAuthState = DeviceAuthState_Error;
          }
        }
        checkDeviceAuth();
      }
     * </pre>
     * 
     * This suggests that sending a device group token back in the response may instruct the device to update its device
     * group token in its EEPROM.<p>
     * 
     * We received additional instructions that say that when {@code uTrackConfig.usingDefaultDgt} is set to {@code false} then this
     * makes the device stop sending its serial number in messages.
     */
    private void sendPositiveAuthResponse() throws IOException {
        final AuthResponse response = AuthResponse.newBuilder()
            .setTimestamp(System.currentTimeMillis())
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
    
    /**
     * Extracts the {@link #serialNumber} and the {@link #deviceGroupToken} from any message received. Furthermore, if
     * an authentication request with a device group token is found, a positive authentication response is sent back to
     * the client.
     */
    private void processMessage(Msg message) {
        MsgVisitor.accept(message, new MsgVisitor() {
            @Override
            public void handleDeviceManagement(DeviceManagement deviceManagement) {
                updateSerialNumber(deviceManagement.getSerialNumber());
            }
            
            @Override
            public void handleData(Data data) {
                for (final DataMsg dataMsg : data.getDataList()) {
                    updateSerialNumber(dataMsg.getSerialNumber());
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

                    @Override
                    public void handleHeartbeat(long heartbeat) {
                        lastHeartbeatReceivedAt = TimePoint.now();
                        updateDeviceHeartbeatIfSerialNumberKnown();
                    }
                });
            }
            
            @Override
            public void handleAckResponse(AckResponse ackResponse) {
                logger.info("Received AckResponse from device "+getSerialNumber()+": "+ackResponse);
            }

            private void updateSerialNumber(String serialNumber) {
                if (Util.hasLength(serialNumber)) {
                    final boolean serialNumberWasUnknown = RiotConnectionImpl.this.serialNumber == null;
                    RiotConnectionImpl.this.serialNumber = serialNumber;
                    if (serialNumberWasUnknown && lastHeartbeatReceivedAt != null) {
                        // we see the serial number for the first time although we already have received a heartbeat; update
                        updateDeviceHeartbeatIfSerialNumberKnown();
                    }
                }
            }
        });
    }


    private void updateDeviceHeartbeatIfSerialNumberKnown() {
        if (serialNumber != null) {
            final Device device = riotServer.getDeviceBySerialNumber(serialNumber);
            if (device != null) {
                try {
                    riotServer.updateDeviceLastHeartbeat(device.getId(), lastHeartbeatReceivedAt, getSocketChannel().getRemoteAddress().toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void sendHeartbeat() {
        try {
            send(Msg.newBuilder().setChannelManagement(ChannelManagement.newBuilder().setHeartbeat(1)).build());
        } catch (ClosedChannelException cce) {
            logger.warning("Channel "+socketChannel+" closed. Forwarding exception to stop sending heartbeat to closed connection");
            throw new RuntimeException(cce);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Problem while trying to send heartbeat message to device "+getSerialNumber(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "RiotConnectionImpl [serialNumber=" + serialNumber + ", socketChannel=" + socketChannel + "]";
    }
}
