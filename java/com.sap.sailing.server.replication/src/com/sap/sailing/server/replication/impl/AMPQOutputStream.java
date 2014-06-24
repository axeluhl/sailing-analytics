package com.sap.sailing.server.replication.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rabbitmq.client.Channel;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

/**
 * Output stream that can split its contents into messages for RabbitMQ. This stream will create a RabbitMQ message for
 * a number of bytes written to that stream.
 * 
 * @author Simon Marcel Pamies
 * @author Axel Uhl (d043530)
 */
public class AMPQOutputStream extends OutputStream {
    private static final Logger logger = Logger.getLogger(AMPQOutputStream.class.getName());

    // FIXME this seems inherently unsafe as it is not properly escaped if it occurs in the actual stream
    private static final byte TERMINATION_COMMAND[] = new byte[] { 2, 6, 0, 4, 1, 9, 8, 2, 0, 1, 4, 2 };

    private static final long DURATION_AFTER_TO_SYNC_DATA_TO_CHANNEL_AS_MILLIS = 5000;
    private static final long DURATION_TO_PAUSE_SYNCER_THREAD_AS_MILLIS = 1000;

    private final Channel channel;
    private final String exchangeName;

    private boolean closed;

    private TimePoint timeLastDataHasBeenReceived;
    private int count;
    private final byte streamBuffer[];

    /**
     * Lock to ensure that the syncer thread and {@link #write(int)} do not interfere with operations on {@link #streamBuffer}, the
     * {@link #count} index into this buffer and the {@link #timeLastDataHasBeenReceived} time stamp. Both, the {@link #write(int)}
     * method and the syncer thread will request a write lock because both will perform modifications and therefore need mutually
     * exclusive access.
     */
    private final NamedReentrantReadWriteLock streamBufferCountAndTimestampLock;

    public AMPQOutputStream(int messageSizeInBytes, Channel channel, String exchangeName, boolean syncAfterTimeout) {
        super();
        this.streamBufferCountAndTimestampLock = new NamedReentrantReadWriteLock("Lock for " + exchangeName, /* fair */false);
        this.streamBuffer = new byte[messageSizeInBytes];
        this.count = 0;
        this.channel = channel;
        this.exchangeName = exchangeName;
        this.closed = false;

        if (syncAfterTimeout) {
            Runnable syncer = new Runnable() {
                @Override
                public void run() {
                    while (!closed) {
                        LockUtil.lockForRead(streamBufferCountAndTimestampLock); // unfair read lock - could not be granted if write
                                                             // operation in progress
                        try {
                            if (timeLastDataHasBeenReceived != null) {
                                TimePoint now = MillisecondsTimePoint.now();
                                // FIXME the timing should better be managed by a Timer instance with delays based on last send and wake-up / test time point
                                if ((now.asMillis() - timeLastDataHasBeenReceived.asMillis()) >= DURATION_AFTER_TO_SYNC_DATA_TO_CHANNEL_AS_MILLIS) {
                                    try {
                                        sendBuffer();
                                        timeLastDataHasBeenReceived = MillisecondsTimePoint.now(); // reset time to avoid unnecessary write attempt
                                    } catch (IOException e) {
                                        logger.log(Level.INFO, "Exception trying to send message. Aborting.", e);
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
                            LockUtil.unlockAfterRead(streamBufferCountAndTimestampLock);
                        }
                    }
                }
            };
            new Thread(syncer, "Syncer for " + AMPQOutputStream.class.getName() + " on exchange " + exchangeName).run();
        }
    }

    /**
     * The method writes a byte to the buffer. If this fills up the buffer to its limit, the buffer is {@link #sendBuffer() sent}
     * as a message and cleared again. The {@link #timeLastDataHasBeenReceived} timestamp is updated to the current time.
     */
    @Override
    public synchronized void write(int b) throws IOException {
        assert count < streamBuffer.length - 1;
        if (this.closed) {
            throw new IOException("This stream has been closed by an earlier operation.");
        }
        streamBuffer[count++] = (byte) b;
        if (count == streamBuffer.length) {
            sendBuffer();
        }
        timeLastDataHasBeenReceived = MillisecondsTimePoint.now();
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
        sendBuffer();
    }

    private void finish() throws IOException {
        sendBuffer();
        this.channel.basicPublish(exchangeName, /* routingKey */"", /* properties */null, TERMINATION_COMMAND);
    }

    /**
     * Sends the buffer contents from index 0 to index {@link #count}-1 inclusive. {@link #count} is reset to 0 when done.
     * The method is synchronized as it needs exclusive and atomic access to {@link #count} and {@link #streamBuffer}.
     */
    private synchronized void sendBuffer() throws IOException {
        if (this.channel != null && this.channel.isOpen()) {
            byte[] message = new byte[count];
            System.arraycopy(streamBuffer, 0, message, 0, count);
            this.channel.basicPublish(exchangeName, /* routingKey */"", /* properties */null, message);
            count = 0;
        } else {
            this.closed = true;
            throw new IOException("AMPQ Channel seems to be closed!");
        }
    }
}
