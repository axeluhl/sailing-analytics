package com.sap.sse.replication.impl;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.OperationsToMasterSender;
import com.sap.sse.replication.OperationsToMasterSendingQueue;
import com.sap.sse.util.ThreadPoolUtil;

/**
 * Manages a queue of operations that failed to get delivered to this replica's master server.
 * When an operation is enqueued, it is added to the tail of the queue. Resend attempts start
 * at the head of the queue, ensuring messages are delivered to the master in the order they
 * were received by this job.<p>
 * 
 * If an operation is delivered successfully, delivering the next operation will be attempted
 * immediately afterwards. If operation delivery fails, the job will re-schedule itself using the
 * {@link ThreadPoolUtil#getDefaultBackgroundTaskThreadPoolExecutor() background thread pool executor}
 * for a later time. The duration the job waits is increased gradually as more delivery attempts
 * fail, up to a maximum wait time between attempts.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class UnsentOperationsSenderJob implements OperationsToMasterSendingQueue, Runnable {
    private static final Logger logger = Logger.getLogger(UnsentOperationsSenderJob.class.getName());
    private final static Duration MAX_WAIT_TIME_BETWEEN_ATTEMPTS = Duration.ONE_MINUTE;
    
    private final Deque<Pair<OperationWithResult<?, ?>, OperationsToMasterSender<?, ?>>> queue;
    
    private boolean scheduled;
    
    /**
     * Something between {@link Duration#NULL} and {@link #MAX_WAIT_TIME_BETWEEN_ATTEMPTS}, indicating
     * the time to wait before trying the next re-send.
     */
    private Duration nextWaitDuration;

    public UnsentOperationsSenderJob() {
        queue = new ConcurrentLinkedDeque<>();
        resetWaitDuration();
    }

    private void resetWaitDuration() {
        nextWaitDuration = Duration.ONE_MILLISECOND;
    }

    @Override
    public synchronized <S, O extends OperationWithResult<S, ?>, T> void scheduleForSending(
            OperationWithResult<S, T> operationWithResult,
            OperationsToMasterSender<S, O> sender) {
        queue.addLast(new Pair<>(operationWithResult, sender));
        ensureScheduled();
    }

    /**
     * Ensures that this job is scheduled with an executor.
     */
    private synchronized void ensureScheduled() {
        if (!scheduled) {
            logger.info("Scheduling replication operation re-send to master in "+nextWaitDuration);
            ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor().schedule(this,
                    /* delay */ nextWaitDuration.asMillis(), TimeUnit.MILLISECONDS);
            scheduled = true;
        }
    }

    @Override
    public void run() {
        boolean empty = false;
        Pair<OperationWithResult<?, ?>, OperationsToMasterSender<?, ?>> first = null;
        boolean sendOk = true;
        while (!empty && sendOk) {
            first = queue.peekFirst();
            if (first != null) {
                sendOk = tryToSend(first);
            } else {
                sendOk = false;
            }
            synchronized (this) { // FIXME synchronization too coarse-grained
                if (sendOk) {
                    resetWaitDuration();
                    queue.removeFirst();
                } else {
                    incrementWaitDuration();
                    // stop this loop (because sendOk==false) and re-schedule
                    scheduled = false;
                    ensureScheduled();
                }
                empty = queue.isEmpty();
                assert empty || !sendOk;
                assert empty || scheduled;
            }
        }
    }

    private <S, O extends OperationWithResult<S, ?>, R> boolean tryToSend(Pair<OperationWithResult<?, ?>, OperationsToMasterSender<?, ?>> unsentOperationAndSender) {
        @SuppressWarnings("unchecked")
        OperationWithResult<S, R> operation = (OperationWithResult<S, R>) unsentOperationAndSender.getA();
        @SuppressWarnings("unchecked")
        OperationsToMasterSender<S, O> sender = (OperationsToMasterSender<S, O>) unsentOperationAndSender.getB();
        return tryToSend(operation, sender);
    }

    /**
     * @return {@code true} if sending succeeded, {@code false} otherwise
     */
    private <S, O extends OperationWithResult<S, ?>, R> boolean tryToSend(OperationWithResult<S, R> operation, OperationsToMasterSender<S, O> sender) {
        boolean result;
        try {
            sender.sendReplicaInitiatedOperationToMaster(operation);
            result = true;
        } catch (IOException e) {
            result = false;
            // remove the operation that failed to arrive on the master server from those marked as sent to master for now:
            sender.hasSentOperationToMaster(operation);
            logger.log(Level.INFO, "Error (re-)sending operation "+operation+" to master "+sender.getMasterDescriptor()+
                    ". Will try again in "+nextWaitDuration);
        }
        return result;
    }

    /**
     * Doubles the wait duration, capping at {@link #MAX_WAIT_TIME_BETWEEN_ATTEMPTS}.
     */
    private void incrementWaitDuration() {
        if (nextWaitDuration.compareTo(MAX_WAIT_TIME_BETWEEN_ATTEMPTS) < 0) {
            nextWaitDuration = nextWaitDuration.times(2);
            if (nextWaitDuration.compareTo(MAX_WAIT_TIME_BETWEEN_ATTEMPTS) > 0) {
                nextWaitDuration = MAX_WAIT_TIME_BETWEEN_ATTEMPTS;
            }
        }
    }
}
