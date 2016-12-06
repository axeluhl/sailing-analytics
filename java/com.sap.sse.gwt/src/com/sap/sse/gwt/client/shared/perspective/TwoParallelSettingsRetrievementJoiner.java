package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

public abstract class TwoParallelSettingsRetrievementJoiner<S1 extends Settings, S2 extends Settings> extends CallbacksJoinerHelper<S1, S2> {
    
    private final AbstractComponentContextWithSettingsStorage<?, S1> context1;
    private final AbstractComponentContextWithSettingsStorage<?, S2> context2;
    
    public TwoParallelSettingsRetrievementJoiner(AbstractComponentContextWithSettingsStorage<?, S1> context1, AbstractComponentContextWithSettingsStorage<?, S2> context2) {
        this.context1 = context1;
        this.context2 = context2;
    }

    private void processIfFinished() {
        if(hasAllCallbacksReceived()) {
            onAllDefaultSettingsLoaded();
        }
    }

    public void startSettingsRetrievementAndJoinAsyncCallback() {
        
        context1.initDefaultSettings(new DefaultSettingsLoadedCallback<S1>() {

            @Override
            public void onError(Throwable caught, S1 fallbackDefaultSettings) {
                receiveFirstCallbackResult(fallbackDefaultSettings);
                processIfFinished();
            }

            @Override
            public void onSuccess(S1 defaultSettings) {
                receiveFirstCallbackResult(defaultSettings);
                processIfFinished();
            }
        });
        
        context2.initDefaultSettings(new DefaultSettingsLoadedCallback<S2>() {

            @Override
            public void onError(Throwable caught, S2 fallbackDefaultSettings) {
                receiveSecondCallbackResult(fallbackDefaultSettings);
                processIfFinished();
            }

            @Override
            public void onSuccess(S2 defaultSettings) {
                receiveSecondCallbackResult(defaultSettings);
                processIfFinished();
            }
        });
    }
    
    public abstract void onAllDefaultSettingsLoaded();
    

}
