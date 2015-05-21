package com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.ActionProvider.DefaultActionProvider;
import com.sap.sailing.gwt.home.client.shared.dispatch.DispatchAsync;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class RefreshManager {

    private static final long PAUSE_ON_ERROR = 5000;
    private List<RefreshHolder<DTO, Action<ResultWithTTL<DTO>>>> refreshables = new ArrayList<>();

    private final Timer timer = new Timer() {
        @Override
        public void run() {
            update();
        }
    };

    private final DispatchAsync actionExecutor;

    private Widget container;

    public RefreshManager(Widget container, DispatchAsync actionExecutor) {
        this.container = container;
        this.actionExecutor = actionExecutor;
        container.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    reschedule();
                } else {
                    timer.cancel();
                }
            }
        });
    }

    private int updateNo;

    private void update() {
        updateNo++;
        for (final RefreshHolder<DTO, Action<ResultWithTTL<DTO>>> refreshable : refreshables) {
            // Everything that needs refresh within the next 5000ms will be refreshed now.
            // This makes it possible to use batching resulting in less requests.
            if (!refreshable.callRunning && refreshable.timeout < System.currentTimeMillis() + 5000) {
                refreshable.callRunning = true;
                actionExecutor.execute(refreshable.provider.getAction(), new AsyncCallback<ResultWithTTL<DTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        refreshable.callRunning = false;
                        refreshable.timeout = System.currentTimeMillis() + PAUSE_ON_ERROR;
                        reschedule();
                    }

                    @Override
                    public void onSuccess(ResultWithTTL<DTO> result) {
                        refreshable.callRunning = false;
                        refreshable.timeout = System.currentTimeMillis() + result.getTtl();
                        refreshable.widget.setData(result.getDto(), refreshable.timeout, updateNo);
                        reschedule();
                    }
                });
            }
        }
    }

    boolean scheduled;

    private void reschedule() {
        if (scheduled) {
            return;
        }
        scheduled = true;
        Scheduler.get().scheduleFinally(new ScheduledCommand() {
            @Override
            public void execute() {
                scheduled = false;

                if (refreshables.isEmpty() || !container.isAttached()) {
                    return;
                }

                long nextUpdate = 0;
                for (final RefreshHolder<DTO, Action<ResultWithTTL<DTO>>> refreshable : refreshables) {
                    if (refreshable.callRunning) {
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
                    timer.schedule(delayMillis);
                }
            }
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <D extends DTO, A extends Action<ResultWithTTL<D>>> void add(RefreshableWidget<D> widget,
            ActionProvider<A> provider) {
        refreshables.add(new RefreshHolder(widget, provider));
        reschedule();
    }

    public <D extends DTO, A extends Action<ResultWithTTL<D>>> void add(RefreshableWidget<D> widget, A action) {
        add(widget, new DefaultActionProvider<>(action));
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
