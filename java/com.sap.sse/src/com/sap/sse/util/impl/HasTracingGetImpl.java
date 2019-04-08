package com.sap.sse.util.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;

/**
 * Can be used with a {@link Future} task and outputs trace messages to the log after {@link #MILLIS_AFTER_WHICH_TO_TRACE_NON_RETURNING_GET} milliseconds
 * of a {@link #get()} call not returning. The {@link #getAdditionalTraceInfo()} method must be implemented by subclasses to provide some meaningful
 * hint as to which object is currently timing out.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <V>
 */
public abstract class HasTracingGetImpl<V> implements HasTracingGet<V> {
    private static final Logger logger = Logger.getLogger(HasTracingGetImpl.class.getName());
    private final long MILLIS_AFTER_WHICH_TO_TRACE_NON_RETURNING_GET = Duration.ONE_SECOND.times(5).asMillis();
    
    public V callGetAndTraceAfterEachTimeout(Future<V> future) throws InterruptedException, ExecutionException {
        while (true) {
            try {
                return future.get(MILLIS_AFTER_WHICH_TO_TRACE_NON_RETURNING_GET, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, ""+Thread.currentThread()+": Timeout waiting for future task "+future+
                            " (retrying); "+ getAdditionalTraceInfo(), e);
                } else {
                    logger.info(""+Thread.currentThread()+": Timeout waiting for future task "+future+
                            " (retrying); "+ getAdditionalTraceInfo());
                }
            }
        }
    }

    protected abstract String getAdditionalTraceInfo();
}
