package com.sap.sailing.gwt.autoplay.client.dataloader;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.ResettableEventBus;
<<<<<<< HEAD
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailure;

public abstract class AutoPlayDataLoaderBase<CF extends AutoPlayClientFactory<?>> implements AutoPlayDataLoader<CF> {
=======
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailure;

public abstract class AutoPlayDataLoaderBase
        implements AutoPlayDataLoader {
>>>>>>> 8bd30717ae79719e3140a0152e35b51a9b46587c

    private final Timer loadTrigger;
    private int loadingIntervallInMs = 5000;
    private ResettableEventBus eventBus;
<<<<<<< HEAD
    private CF clientFactory;
=======
    private AutoPlayClientFactorySixtyInch clientFactory;
>>>>>>> 8bd30717ae79719e3140a0152e35b51a9b46587c

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
<<<<<<< HEAD
    public final void startLoading(EventBus eventBus, CF clientFactory) {
=======
    public final void startLoading(EventBus eventBus, AutoPlayClientFactorySixtyInch clientFactory) {
>>>>>>> 8bd30717ae79719e3140a0152e35b51a9b46587c
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

    protected void onStoppedLoading() {
    }

    protected void onStartedLoading() {
    }

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

<<<<<<< HEAD
    protected CF getClientFactory() {
=======
    protected AutoPlayClientFactorySixtyInch getClientFactory() {
>>>>>>> 8bd30717ae79719e3140a0152e35b51a9b46587c
        return clientFactory;
    }

    protected EventBus getEventBus() {
        return eventBus;
    }
}
