package com.sap.sse.datamining;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;

public interface DataMiningService {

    public FunctionRegistry getFunctionRegistry();

    public FunctionProvider getFunctionProvider();
    
    public static final class Util {

        private static final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
        private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 60,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        public static ThreadPoolExecutor getExecutor() {
            return executor;
        }
        
    }

}
