package com.sap.sse.pairinglist.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.PairingListTemplateFactory;
import com.sap.sse.util.ThreadPoolUtil;
import com.sap.sse.util.impl.ThreadPoolUtilImpl;

public class PairingListTemplateFactoryImpl implements PairingListTemplateFactory {
    private final Map<PairingFrameProvider, PairingListTemplate> pairingListTemplates;

    public PairingListTemplateFactoryImpl() {
        this(new HashMap<>());
    }

    public PairingListTemplateFactoryImpl(Map<PairingFrameProvider, PairingListTemplate> existingPairingListTemplates) {
        this.pairingListTemplates = existingPairingListTemplates;
    }

    @Override
    public PairingListTemplate getOrCreatePairingListTemplate(PairingFrameProvider pairingFrameProvider) {
        PairingListTemplate result = pairingListTemplates.get(pairingFrameProvider);
        if (result == null) {
            result = generatePairingList(pairingFrameProvider);
            pairingListTemplates.put(pairingFrameProvider, result);
        }
        return result;
    }

    protected PairingListTemplate generatePairingList(PairingFrameProvider pairingFrameProvider) {
        ExecutorService executorService = ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor();
        int threadPoolSize = ThreadPoolUtilImpl.INSTANCE.getReasonableThreadPoolSize();

        Callable<PairingListTemplate> callable = new Callable<PairingListTemplate>() {
            @Override
            public PairingListTemplate call() throws Exception {
                return new PairingListTemplateImpl_OLD(pairingFrameProvider, (100000/threadPoolSize));
            }
        };

        List<Future<PairingListTemplate>> futures = new ArrayList<Future<PairingListTemplate>>();

        IntStream.range(0, threadPoolSize).forEach($ -> {
            Future<PairingListTemplate> future = executorService.submit(callable);
            futures.add(future);
        });


        List<PairingListTemplate> templates = new ArrayList<PairingListTemplate>();
        futures.forEach(future -> {
            try {
                templates.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        
        executorService.shutdown();
        
        PairingListTemplate currentBest = templates.get(0);
        for(int i = 1; i<templates.size(); i++) {
            PairingListTemplate current = templates.get(i);
            if(current.getQuality()<currentBest.getQuality()) {
                currentBest = current;
            }
        }
        return currentBest;
    }
}
