package com.sap.sse.gwt.client.mvp;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

/**
 * Manages GWT code splitting by moving an activity and everything that activity exclusively uses to a separate code
 * fragment that is loaded lazily. This class delegates all methods to the actual activity except for the
 * {@link Activity#start(AcceptsOneWidget, EventBus)} which delegates to the abstract {@link #startAsync()} methods that
 * subclasses have to implement.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public abstract class AbstractActivityProxy extends AbstractActivity {
    private Activity myActivity;
    private boolean isCancelled;
    private AcceptsOneWidget panel;
    private EventBus eventBus;

    /**
     * Subclasses should implement this by calling {@link GWT#runAsync(RunAsyncCallback)} with
     * an instance whose class extends {@link AbstractRunAsyncCallback} with the {@link RunAsyncCallback#onSuccess()}
     * method calling {@link AbstractRunAsyncCallback#onSuccess(Activity)} with the new
     * activity. Example:
     * <pre>
     *   GWT.runAsync(new AbstractRunAsyncCallback() {
     *       &#64;Override
     *       public void onSuccess() {
     *           super.onSuccess(new MyActivity(place, clientFactory));
     *       }
     *   });
     * </pre>
     * This will cause the activity and everything it controls exclusively to become part of the code split
     * introduced by that very call to {@link GWT#runAsync(RunAsyncCallback)}.
     */
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
            Notification.notify(reason.getMessage(), NotificationType.ERROR);
        }
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        if (!isCancelled) {
            if (myActivity == null) {
                this.panel = panel;
                this.eventBus = eventBus;
                startAsync();
            } else {
                myActivity.start(panel, eventBus);
            }
        }
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
