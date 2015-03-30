package com.sap.sailing.domain.tractracadapter.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.tractracadapter.LoadingQueueDoneCallBack;
import com.sap.sailing.domain.tractracadapter.Receiver;

/**
 * Performs {@link #executeWhenAllReceiversAreDoneLoading()}, when all Receivers have called back.
 * 
 * @author Frederik Petersen
 *
 */
public abstract class AbstractLoadingQueueDoneCallBack implements LoadingQueueDoneCallBack {
    
    /**
     * Set keeping track of all receivers that still need to callback. When it's empty, desired 
     * action should be performed.
     */
    private final Set<Receiver> receiversToCallback;
    
    /**
     * 
     * @param receivers
     *            All of these receivers will be queried to call back when they are done handling currently queued
     *            events
     */
    public AbstractLoadingQueueDoneCallBack(Collection<Receiver> receivers) {
        this.receiversToCallback = new HashSet<>(receivers);
        for (Receiver receiver : receivers) {
            receiver.callBackWhenLoadingQueueIsDone(this);
        }
    }

    @Override
    public void loadingQueueDone(Receiver receiver) {
        synchronized (this.receiversToCallback) {
            receiversToCallback.remove(receiver);
            if (this.receiversToCallback.isEmpty()) {
                executeWhenAllReceiversAreDoneLoading();
            }
        }
    }

    protected abstract void executeWhenAllReceiversAreDoneLoading();

}
