package com.sap.sailing.gwt.autoplay.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.autoplay.client.dataloader.AutoPlayDataLoader;

/**
 * Sample custom event class for copy & paste
 */
public class DataLoadFailure extends GwtEvent<DataLoadFailure.Handler> {

    private final AutoPlayDataLoader<?> source;
    public static final Type<Handler> TYPE = new Type<Handler>();

    /**
     * Event handler interface
     */
    interface Handler extends EventHandler {
        void onLoadFailure(DataLoadFailure e);
    }

    public DataLoadFailure(AutoPlayDataLoader<?> source) {
        this.source = source;
    }

    public AutoPlayDataLoader<?> getSource() {
        return source;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onLoadFailure(this);
    }
}
