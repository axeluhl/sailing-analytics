package com.sap.sse.gwt.client.mvp;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Wraps an activity, delegates all methods to it except for the {@link Activity#start(AcceptsOneWidget, EventBus)}
 * which is wrapped by a {@link RunAsyncCallback} to allow for code splitting.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public abstract class AbstractActivityProxy extends AbstractActivity {
    private Activity myActivity;
    private boolean isCancelled;
    private AcceptsOneWidget panel;
    private EventBus eventBus;

    protected abstract void startAsync();

    public abstract class AbstractRunAsyncCallback implements RunAsyncCallback {
        public void onSuccess(Activity activity) {
            if (myActivity == null) {
                myActivity = activity;
            }
            if (!isCancelled) {
                myActivity.start(panel, eventBus);
            }
        }

        @Override
        public void onFailure(Throwable reason) {
            Window.alert(reason.getMessage());
        }
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        this.panel = panel;
        this.eventBus = eventBus;
        startAsync();
    }

    @Override
    public String mayStop() {
        final String result;
        if (myActivity != null) {
            result = myActivity.mayStop();
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public void onCancel() {
        isCancelled = true;
        if (myActivity != null) {
            myActivity.onCancel();
        }
    }

    @Override
    public void onStop() {
        if (myActivity != null) {
            myActivity.onStop();
        }
    }

}
