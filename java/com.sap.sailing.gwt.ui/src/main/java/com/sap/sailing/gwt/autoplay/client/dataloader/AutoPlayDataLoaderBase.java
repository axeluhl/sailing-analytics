package com.sap.sailing.gwt.autoplay.client.dataloader;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.ResettableEventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailure;

public abstract class AutoPlayDataLoaderBase
        implements AutoPlayDataLoader {

    private final Timer loadTrigger;
    private int loadingIntervallInMs = 5000;
    private ResettableEventBus eventBus;
    private AutoPlayClientFactorySixtyInch clientFactory;

    public AutoPlayDataLoaderBase() {
        loadTrigger = new Timer() {
            @Override
            public void run() {
                try {
                    onLoadData();
                } catch (Exception e) {
                    fireEvent(new DataLoadFailure(AutoPlayDataLoaderBase.this, e));
                }
            }
        };
    }

    @Override
    public final void startLoading(EventBus eventBus, AutoPlayClientFactorySixtyInch clientFactory) {
        this.eventBus = new ResettableEventBus(eventBus);
        this.clientFactory = clientFactory;
        loadTrigger.scheduleRepeating(loadingIntervallInMs);
        onStartedLoading();
    }

    @Override
    public final void stopLoading() {
        loadTrigger.cancel();
        onStoppedLoading();
        eventBus.removeHandlers();
    }

    protected abstract void onLoadData();

    protected abstract void onStoppedLoading();

    protected abstract void onStartedLoading();

    /**
     * Helper method to fire event upon data changes.
     * 
     * @param eventToFire
     */
    protected void fireEvent(GwtEvent<?> eventToFire) {
        eventBus.fireEvent(eventToFire);
    }

    protected void setLoadingIntervallInMs(int loadingIntervallInMs) {
        this.loadingIntervallInMs = loadingIntervallInMs;
        if (loadTrigger.isRunning()) {
            loadTrigger.scheduleRepeating(loadingIntervallInMs);
        }
    }

    protected AutoPlayClientFactorySixtyInch getClientFactory() {
        return clientFactory;
    }

    protected EventBus getEventBus() {
        return eventBus;
    }
}
