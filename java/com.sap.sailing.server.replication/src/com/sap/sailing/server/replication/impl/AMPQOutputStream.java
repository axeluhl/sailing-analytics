package com.sap.sailing.server.replication.impl;

import java.io.IOException;
import java.io.OutputStream;

import com.rabbitmq.client.Channel;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

/**
 * Output stream that can split its contents into messages for RabbitMQ. This
 * stream will create a RabbitMQ message for a number of bytes written to that
 * stream.
 * 
 * @author Simon Marcel Pamies
 */
public class AMPQOutputStream extends OutputStream {
    
    private static final byte TERMINATION_COMMAND[] = new byte[]{2,6,0,4,1,9,8,2,0,1,4,2};

    private static final long DURATION_AFTER_TO_SYNC_DATA_TO_CHANNEL_AS_MILLIS = 5000;
    private static final long DURATION_TO_PAUSE_SYNCER_THREAD_AS_MILLIS = 1000;

    private final int messageSizeInBytes;
    private final Channel channel;
    private final String exchangeName;

    private boolean closed;

    private TimePoint timeLastDataHasBeenReceived;
    private int count;
    private byte streamBuffer[];
    
    private final Runnable syncer;
    
    /**
     * Lock to ensure that the syncer thread does not interfere with write
     * operations. It will initialized unfair because the sequence in which
     * threads will be granted access is irrelevant. The {@link #write(int)}
     * method will always request a write lock so that it will always be
     * prioritized.
     */
    private final NamedReentrantReadWriteLock readWritelock;
    
    public AMPQOutputStream(int messageSizeInBytes, Channel channel, String exchangeName, boolean syncAfterTimeout) {
        super();
        readWritelock = new NamedReentrantReadWriteLock("Lock for " + exchangeName, /*fair*/false);
        this.messageSizeInBytes = messageSizeInBytes;
        this.channel = channel;
        this.exchangeName = exchangeName;
        this.closed = false;
        resetBuffer();
        
        if (syncAfterTimeout) {
            syncer = new Runnable() {
                @Override
                public void run() {
                    while(!closed) {
                        LockUtil.lockForRead(readWritelock); // unfair read lock - could not be granted if write operation in progress
                        try {
                            if (timeLastDataHasBeenReceived != null) {
                                TimePoint now = MillisecondsTimePoint.now();
                                if ((now.asMillis()-timeLastDataHasBeenReceived.asMillis()) >= DURATION_AFTER_TO_SYNC_DATA_TO_CHANNEL_AS_MILLIS) {
                                    try {
                                        possiblySplitAndSend(/*forceDataWrite*/ true);
                                        timeLastDataHasBeenReceived = MillisecondsTimePoint.now(); // reset time to avoid unnecessary write try
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        break;
                                    }
                                }
                                try {
                                    Thread.sleep(DURATION_TO_PAUSE_SYNCER_THREAD_AS_MILLIS);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } finally {
                            LockUtil.unlockAfterRead(readWritelock);
                        }
                    }
                }
            };
            new Thread(syncer, "Syncer for " + AMPQOutputStream.class.getName() + " on exchange " + exchangeName).run();
        } else {
            syncer = null;
        }
    }

    @Override
    public void write(int b) throws IOException {
        LockUtil.lockForWrite(readWritelock); // locking so that syncer thread waits until op has finished
        try {
            if (this.closed) {
                throw new IOException("This stream has been closed by an earlier operation.");
            }
            if ( count >= messageSizeInBytes ) {
                possiblySplitAndSend(/*forceDataWrite*/ false);
            }
            streamBuffer[count++] = (byte)b;
            timeLastDataHasBeenReceived = MillisecondsTimePoint.now();
        } finally {
            LockUtil.unlockAfterWrite(readWritelock);
        }
    }
    
    @Override
    public void close() throws IOException {
        try {
            finish();
        } finally {
            // make sure to always set this stream to closed
            this.closed = true;
        }
    }
    
    @Override
    public void flush() throws IOException {
        possiblySplitAndSend(/*forceDataWrite*/ true);
    }
    
    private void finish() throws IOException {
        possiblySplitAndSend(/*forceDataWrite*/ false);
        this.channel.basicPublish(exchangeName, /*routingKey*/"", /*properties*/null, TERMINATION_COMMAND);
    }
    
    private void resetBuffer() {
        this.streamBuffer = new byte[messageSizeInBytes+1];
        count = 0;
    }
    
    private synchronized void possiblySplitAndSend(boolean forceDataWrite) throws IOException {
        if (forceDataWrite || (!this.closed & count >= this.messageSizeInBytes)) {
            if (this.channel != null && this.channel.isOpen()) {
                this.channel.basicPublish(exchangeName, /*routingKey*/"", /*properties*/null, streamBuffer);
                resetBuffer();
            } else {
                this.closed = true;
                throw new IOException("AMPQ Channel seems to be closed!");
            }
        }
    }
}