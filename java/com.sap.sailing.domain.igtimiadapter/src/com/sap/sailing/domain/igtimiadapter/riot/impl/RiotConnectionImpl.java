package com.sap.sailing.domain.igtimiadapter.riot.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.igtimi.IgtimiAPI.APIData;
import com.igtimi.IgtimiData.Data;
import com.igtimi.IgtimiData.DataMsg;
import com.igtimi.IgtimiData.DataPoint;
import com.igtimi.IgtimiDevice.DeviceManagement;
import com.igtimi.IgtimiStream.AckResponse;
import com.igtimi.IgtimiStream.ChannelManagement;
import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.riot.DataPointVisitor;
import com.sap.sailing.domain.igtimiadapter.riot.MsgVisitor;
import com.sap.sailing.domain.igtimiadapter.riot.RiotConnection;

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
    
    private final CodedInputStream messageLengthDecoder;
    
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
    
    RiotConnectionImpl(SocketChannel socketChannel, RiotServerImpl riotServer) {
        this.socketChannel = socketChannel;
        this.riotServer = riotServer;
        this.messageLengthBuffer = ByteBuffer.allocate(5);
        this.messageLengthDecoder = CodedInputStream.newInstance(messageLengthBuffer);
    }
    
    @Override
    public String getSerialNumber() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDeviceGroupToken() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void sendCommand(String command) {
        // TODO sendCommand
    }

    @Override
    public void dataReceived(ByteBuffer data) {
        data.flip();
        while (data.hasRemaining()) { // consume all data received
            if (nextMessageLength == 0) { // we're in message length reading mode
                final byte b = data.get();
                messageLengthBuffer.put(b);
                messageLengthBuffer.mark();
                try {
                    nextMessageLength = messageLengthDecoder.readRawVarint32();
                    messageBuffer = ByteBuffer.allocate(nextMessageLength);
                } catch (IOException ioe) {
                    // varint32 still incomplete; wait for more bytes
                    messageLengthBuffer.reset();
                }
            } else { // we're in message reading mode, and the messageBuffer is not full yet
                final byte[] copyBuffer = new byte[Math.min(data.remaining(), messageBuffer.remaining())];
                data.get(copyBuffer);
                messageBuffer.put(copyBuffer);
                if (!messageBuffer.hasRemaining()) { // data for message read completely
                    nextMessageLength = 0;
                    try {
                        final Msg message = Msg.parseFrom(messageBuffer, protobufExtensionRegistry);       
                        processMessage(message);
                    } catch (InvalidProtocolBufferException e) {
                        logger.log(Level.SEVERE, "Error parsing message from device "+serialNumber, e);
                    }
                }
            }
        }
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
                    for (final DataPoint dataPoint : dataMsg.getDataList()) {
                        DataPointVisitor.accept(dataPoint, new DataPointVisitor() {
                            // TODO handle DataPoint
                        });
                    }
                }
            }
            
            @Override
            public void handleChannelManagement(ChannelManagement channelManagement) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void handleApiData(APIData apiData) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void handleAckResponse(AckResponse ackResponse) {
                // TODO Auto-generated method stub
                
            }
        });
        riotServer.notifyListeners(fixes);
    }
}
