package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.OperationWithResultWithIdWrapper;
import com.sap.sse.replication.OperationsToMasterSender;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.impl.UnsentOperationsSenderJob;

public class UnsentOperationSenderJobTest implements OperationsToMasterSender<RacingEventService, OperationWithResult<RacingEventService, String>> {
    private UnsentOperationsSenderJob job;
    private int resendCount;
    private CountDownLatch latch;
    
//    @Rule public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
    
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
        job.retrySendingLater(operationWithResult, sender);
        latch.await();
        assertEquals(2, resendCount);
    }

    public <T> void sendReplicaInitiatedOperationToMaster(OperationWithResult<RacingEventService, T> operation) throws IOException {
        logger.info("resending at count "+resendCount);
        if (resendCount++ == 0) {
            throw new IOException("First resend failed");
        } else {
            latch.countDown();
        }
    }
    
    @Override
    public <S, O extends OperationWithResult<S, ?>, T> void retrySendingLater(
            OperationWithResult<S, T> operationWithResult, OperationsToMasterSender<S, O> sender) {
        job.retrySendingLater(operationWithResult, sender);
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
}
