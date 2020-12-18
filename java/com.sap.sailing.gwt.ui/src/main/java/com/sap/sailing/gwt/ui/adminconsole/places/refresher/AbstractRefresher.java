package com.sap.sailing.gwt.ui.adminconsole.places.refresher;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.Displayer;
import com.sap.sailing.gwt.ui.client.Refresher;
import com.sap.sse.gwt.client.ErrorReporter;

public abstract class AbstractRefresher<T> implements Refresher<T> {

    private final Set<Displayer> displayers = new HashSet<Displayer>();
    private Iterable<T> dtos;
    private final ErrorReporter errorReporter;

    public AbstractRefresher(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    @Override
    public void addDisplayerAndCallFillOnInit(Displayer displayer) {
        displayers.add(displayer);
        if (dtos == null) {
            reloadAndCallFillAll();
        } else {
            fill(dtos, displayer);
        }
    }

    @Override
    public void reloadAndCallFillOnly(Displayer fillOnlyDisplayer) {
        AsyncCallback<Iterable<T>> callback = new AsyncCallback<Iterable<T>>() {

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to obtain list from server " + caught.getMessage());
            }

            @Override
            public void onSuccess(Iterable<T> result) {
                if (fillOnlyDisplayer != null) {
                    fill(result, fillOnlyDisplayer);
                } else {
                    callAllFill(result, null);
                }
            }
        };
        reload(callback);
    }
    
    @Override
    public void reloadAndCallFillAll() {
        reloadAndCallFillOnly(null);
    }

    @Override
    public void updateAndCallFillForAll(Iterable<T> dtos, Displayer origin) {
        callAllFill(dtos, origin);
    }
    

    @Override
    public void callFillAndReloadInitially(Displayer displayer) {
        if (dtos == null) {
            reloadAndCallFillAll();
        } else {
            fill(dtos, displayer);
        }
    }

    private void callAllFill(final Iterable<T> dtos, final Displayer origin) {
        displayers.stream().filter(displayer -> !Objects.equals(displayer, origin))
                .forEach(displayer -> fill(dtos, displayer));
    }

    public abstract void reload(AsyncCallback<Iterable<T>> callback);
    public abstract void fill(Iterable<T> dtos, Displayer displayer);

}
