package com.sap.sse.gwt.client.shared.perspective;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;

public class ComponentContextInitialisationUtils {
    
    private ComponentContextInitialisationUtils() {}
    
    public static<T, PS extends Settings> AsyncCallbackWithSettingsRetrievementJoiner<T, PS> createSettingsRetrievementWithAsyncCallbackJoiner(
            AbstractComponentContextWithSettingsStorage<? extends PerspectiveLifecycle<PS>, PS> componentContext, AsyncCallback<T> callbackToWrap) {
        return new AsyncCallbackWithSettingsRetrievementJoiner<>(componentContext, callbackToWrap);
    }
    
    public static<PS1 extends Settings, PS2 extends Settings> void initTwoComponentContexts(AbstractComponentContextWithSettingsStorage<?, PS1> context1, AbstractComponentContextWithSettingsStorage<?, PS2> context2, final IOnDefaultSettingsLoaded onDefaultSettingsLoaded) {
        TwoParallelSettingsRetrievementJoiner<PS1, PS2> joiner = new TwoParallelSettingsRetrievementJoiner<PS1, PS2>(context1, context2) {
            @Override
            public void onAllDefaultSettingsLoaded() {
                onDefaultSettingsLoaded.onLoaded();
            }
        };
        joiner.startSettingsRetrievementAndJoinAsyncCallback();
    }

}
