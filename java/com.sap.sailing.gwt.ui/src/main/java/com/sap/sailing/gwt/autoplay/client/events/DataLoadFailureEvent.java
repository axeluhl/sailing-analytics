package com.sap.sailing.gwt.autoplay.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.autoplay.client.dataloader.AutoPlayDataLoader;

/**
 * Sample custom event class for copy & paste
 */
public class DataLoadFailureEvent extends GwtEvent<DataLoadFailureEvent.Handler> implements FailureEvent {
    public static final Type<Handler> TYPE = new Type<Handler>();

    private final AutoPlayDataLoader<?> source;
    private Throwable caught;
    private String message;

    /**
     * Event handler interface
     */
    public interface Handler extends EventHandler {
        void onLoadFailure(DataLoadFailureEvent e);
    }

    public DataLoadFailureEvent(AutoPlayDataLoader<?> source, Throwable caught) {
        this(source, caught, null);
    }

    public DataLoadFailureEvent(AutoPlayDataLoader<?> source, String message) {
        this(source, null, message);
    }

    public DataLoadFailureEvent(AutoPlayDataLoader<?> source, Throwable caught, String message) {
        this.source = source;
        this.caught = caught;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCaught() {
        return caught;
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
