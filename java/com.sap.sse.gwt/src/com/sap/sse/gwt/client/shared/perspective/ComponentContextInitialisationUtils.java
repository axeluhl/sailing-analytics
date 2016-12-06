package com.sap.sse.gwt.client.shared.perspective;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class ComponentContextInitialisationUtils {
    
    private ComponentContextInitialisationUtils() {}
    
    public static<T, S extends Settings> AsyncCallbackWithSettingsRetrievementJoiner<T, S> wrapAsyncCallbak(
            AbstractComponentContextWithSettingsStorage<? extends ComponentLifecycle<S, ?>, S> componentContext, AsyncCallback<T> callbackToWrap) {
        return new AsyncCallbackWithSettingsRetrievementJoiner<>(componentContext, callbackToWrap);
    }
    
    public static<S1 extends Settings, S2 extends Settings> void initTwoComponentContexts(AbstractComponentContextWithSettingsStorage<?, S1> context1, AbstractComponentContextWithSettingsStorage<?, S2> context2, final IOnDefaultSettingsLoaded onDefaultSettingsLoaded) {
        TwoParallelSettingsRetrievementJoiner<S1, S2> joiner = new TwoParallelSettingsRetrievementJoiner<S1, S2>(context1, context2) {
            @Override
            public void onAllDefaultSettingsLoaded() {
                onDefaultSettingsLoaded.onLoaded();
            }
        };
        joiner.startSettingsRetrievementAndJoinAsyncCallback();
    }

}
