package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

public abstract class DoubleSettingsRetrievementJoiner<PS1 extends Settings, PS2 extends Settings> extends CallbacksJoinerHelper<PerspectiveCompositeSettings<PS1>, PerspectiveCompositeSettings<PS2>> {
    
    private final ComponentContext<?, PS1> context1;
    private final ComponentContext<?, PS2> context2;
    
    public DoubleSettingsRetrievementJoiner(ComponentContext<?, PS1> context1, ComponentContext<?, PS2> context2) {
        this.context1 = context1;
        this.context2 = context2;
    }

    private void processIfFinished() {
        if(hasAllCallbacksReceived()) {
            onAllDefaultSettingsLoaded();
        }
    }

    public void startSettingsRetrievementAndJoinAsyncCallback() {
        
        context1.initDefaultSettings(new DefaultSettingsLoadedCallback<PS1>() {

            @Override
            public void onError(Throwable caught, PerspectiveCompositeSettings<PS1> fallbackDefaultSettings) {
                receiveFirstCallbackResult(fallbackDefaultSettings);
                processIfFinished();
            }

            @Override
            public void onSuccess(PerspectiveCompositeSettings<PS1> defaultSettings) {
                receiveFirstCallbackResult(defaultSettings);
                processIfFinished();
            }
        });
        
        context2.initDefaultSettings(new DefaultSettingsLoadedCallback<PS2>() {

            @Override
            public void onError(Throwable caught, PerspectiveCompositeSettings<PS2> fallbackDefaultSettings) {
                receiveSecondCallbackResult(fallbackDefaultSettings);
                processIfFinished();
            }

            @Override
            public void onSuccess(PerspectiveCompositeSettings<PS2> defaultSettings) {
                receiveSecondCallbackResult(defaultSettings);
                processIfFinished();
            }
        });
    }
    
    public abstract void onAllDefaultSettingsLoaded();
    

}
