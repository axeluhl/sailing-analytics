package com.sap.sailing.gwt.home.shared.refresh;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.home.shared.refresh.ActionProvider.DefaultActionProvider;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sse.common.Duration;

public abstract class RefreshManager {
    private static final Logger LOG = Logger.getLogger(RefreshManager.class.getName());

    private static final long PAUSE_ON_ERROR = Duration.ONE_SECOND.times(30).asMillis();
    private List<RefreshHolder<DTO, Action<ResultWithTTL<DTO>>>> refreshables = new ArrayList<>();

    private boolean scheduled;
    private final Timer timer = new Timer() {
        @Override
        public void run() {
            update();
        }
    };

    private final DispatchSystem actionExecutor;
    
    boolean started = false;

    public RefreshManager(DispatchSystem actionExecutor) {
        this.actionExecutor = actionExecutor;
    }
    
    protected void start() {
        LOG.log(Level.FINE, "Starting auto refresh");
        started = true;
        reschedule();
    }
    
    protected void cancel() {
        LOG.log(Level.FINE, "Cancelling auto refresh");
        started = false;
        timer.cancel();
    }
    
    protected abstract boolean canExecute();
    
    protected void onSuccessfulUpdate() {
    }
    
    protected  void onFailedUpdate(Throwable errorCause) {
    }

    private void update() {

        for (final RefreshHolder<DTO, Action<ResultWithTTL<DTO>>> refreshable : refreshables) {
            // Everything that needs refresh within the next 5000ms will be refreshed now.
            // This makes it possible to use batching resulting in less requests.
            if (refreshable.provider.isActive() && !refreshable.callRunning
                    && refreshable.timeout < System.currentTimeMillis() + ResultWithTTL.MAX_TIME_TO_LOAD_EARLIER.asMillis()) {
                refreshable.callRunning = true;
                final Action<ResultWithTTL<DTO>> action = refreshable.provider.getAction();
                actionExecutor.execute(action, new AsyncCallback<ResultWithTTL<DTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        refreshable.callRunning = false;
                        refreshable.timeout = System.currentTimeMillis() + PAUSE_ON_ERROR;
                        reschedule();
                        onFailedUpdate(caught);
                    }

                    @Override
                    public void onSuccess(ResultWithTTL<DTO> result) {
                        refreshable.callRunning = false;
                        refreshable.timeout = System.currentTimeMillis() + result.getTtlMillis();
                        try {
                            refreshable.widget.setData(result.getDto());
                            onSuccessfulUpdate();
                        } catch(Throwable error) {
                            LOG.log(Level.SEVERE, "Error while refreshing content with action " + action.getClass().getName(), error);
                        }
                        reschedule();
                    }
                });
            }
        }
    }

    private void reschedule() {
        if (scheduled) {
            return;
        }
        scheduled = true;
        Scheduler.get().scheduleFinally(new ScheduledCommand() {
            @Override
            public void execute() {
                scheduled = false;

                if (refreshables.isEmpty() || !started || !canExecute()) {
                    return;
                }

                long nextUpdate = 0;
                for (final RefreshHolder<DTO, Action<ResultWithTTL<DTO>>> refreshable : refreshables) {
                    if (refreshable.callRunning || !refreshable.provider.isActive()) {
                        continue;
                    }
                    if (nextUpdate == 0) {
                        nextUpdate = refreshable.timeout;
                    } else {
                        nextUpdate = Math.min(nextUpdate, refreshable.timeout);
                    }
                }
                int delayMillis = Math.max(0, (int) (nextUpdate - System.currentTimeMillis()));
                if (delayMillis == 0) {
                    update();
                } else {
                    LOG.log(Level.FINE, "Scheduling auto refresh in " + delayMillis + "ms");
                    timer.schedule(delayMillis);
                }
            }
        });
    }
    
    public void forceReschedule() {
        cancel();
        reschedule();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <D extends DTO, A extends Action<ResultWithTTL<D>>> void add(RefreshableWidget<? super D> widget,
            ActionProvider<A> provider) {
        refreshables.add(new RefreshHolder(widget, provider));
        reschedule();
    }

    public <D extends DTO, A extends Action<ResultWithTTL<D>>> void add(RefreshableWidget<? super D> widget, A action) {
        add(widget, new DefaultActionProvider<>(action));
    }
    
    public DispatchSystem getDispatchSystem() {
        return actionExecutor;
    }

    private static class RefreshHolder<D extends DTO, A extends Action<ResultWithTTL<D>>> {
        private final RefreshableWidget<D> widget;
        private final ActionProvider<A> provider;

        // initial update is now
        private long timeout = System.currentTimeMillis();
        private boolean callRunning = false;

        public RefreshHolder(RefreshableWidget<D> widget, ActionProvider<A> provider) {
            this.widget = widget;
            this.provider = provider;
        }
    }
}
