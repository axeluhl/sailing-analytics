package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.OperationWithResultWithIdWrapper;
import com.sap.sse.replication.OperationsToMasterSender;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.impl.UnsentOperationsSenderJob;

public class UnsentOperationSenderJobTest implements OperationsToMasterSender<RacingEventService, OperationWithResult<RacingEventService, String>> {
    private static final int MAX_RESEND_COUNT = 10;
    private UnsentOperationsSenderJob job;
    private int resendCount;
    private CountDownLatch latch;
    
    @Rule public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
    
    @Before
    public void setUp() {
        job = new UnsentOperationsSenderJob();
        resendCount = 0;
        latch = new CountDownLatch(1);
    }
    
    @Test
    public void testResendThatFailsAtLeastOnce() throws InterruptedException, BrokenBarrierException {
        final OperationWithResult<RacingEventService, String> operationWithResult = null;
        final OperationsToMasterSender<RacingEventService, OperationWithResult<RacingEventService, String>> sender = this;
        job.scheduleForSending(operationWithResult, sender);
        latch.await();
        assertEquals(MAX_RESEND_COUNT+1, resendCount);
    }

    public <T> void sendReplicaInitiatedOperationToMaster(OperationWithResult<RacingEventService, T> operation) throws IOException {
        logger.info("resending at count "+resendCount);
        if (resendCount++ < MAX_RESEND_COUNT) {
            throw new IOException("First resend failed");
        } else {
            latch.countDown();
        }
    }
    
    @Override
    public <S, O extends OperationWithResult<S, ?>, T> void scheduleForSending(
            OperationWithResult<S, T> operationWithResult, OperationsToMasterSender<S, O> sender) {
        job.scheduleForSending(operationWithResult, sender);
    }

    @Override
    public Serializable getId() {
        return getClass().getName();
    }

    @Override
    public ReplicationMasterDescriptor getMasterDescriptor() {
        return null;
    }

    @Override
    public void writeOperation(OperationWithResult<?, ?> operation, OutputStream outputStream, boolean closeStream) throws IOException {}

    @Override
    public void addOperationSentToMasterForReplication(OperationWithResultWithIdWrapper<RacingEventService, ?> operationWithResultWithIdWrapper) {}

    @Override
    public boolean hasSentOperationToMaster(OperationWithResult<RacingEventService, ?> operation) { return false; }
}
