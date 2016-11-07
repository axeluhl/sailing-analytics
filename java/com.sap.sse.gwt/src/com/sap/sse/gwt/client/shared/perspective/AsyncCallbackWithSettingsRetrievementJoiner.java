package com.sap.sse.gwt.client.shared.perspective;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;

public class AsyncCallbackWithSettingsRetrievementJoiner<T, PS extends Settings> extends CallbacksJoinerHelper<T, PerspectiveCompositeSettings<PS>> implements AsyncCallback<T> {
    
    private final AsyncCallback<T> wrappedCallback;
    private final AbstractComponentContextWithSettingsStorage<?, PS> context;
    
    public AsyncCallbackWithSettingsRetrievementJoiner(AbstractComponentContextWithSettingsStorage<?, PS> context, AsyncCallback<T> callbackToWrap) {
        this.wrappedCallback = callbackToWrap;
        this.context = context;
    }

    @Override
    public void onFailure(Throwable caught) {
        receiveFirstCallbackResult(null);
        receiveError(caught);
    }

    @Override
    public void onSuccess(T result) {
        receiveFirstCallbackResult(result);
        processIfFinished();
    }
    
    private void processIfFinished() {
        if(hasAllCallbacksReceived()) {
            if(isErrorOccurred()) {
                wrappedCallback.onFailure(getCaught());
            } else {
                wrappedCallback.onSuccess(getFirstCallbackResult());
            }
        }
    }

    public void startSettingsRetrievementAndJoinAsyncCallback() {
        
        context.initDefaultSettings(new DefaultSettingsLoadedCallback<PS>() {

            @Override
            public void onError(Throwable caught, PerspectiveCompositeSettings<PS> fallbackDefaultSettings) {
                receiveSecondCallbackResult(fallbackDefaultSettings);
                processIfFinished();
            }

            @Override
            public void onSuccess(PerspectiveCompositeSettings<PS> defaultSettings) {
                receiveSecondCallbackResult(defaultSettings);
                processIfFinished();
            }
        });
    }
    

}
