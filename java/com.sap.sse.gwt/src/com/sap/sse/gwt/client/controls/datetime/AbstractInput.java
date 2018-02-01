package com.sap.sse.gwt.client.controls.datetime;

import java.util.Date;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstract implementation for classes wrapping date and/or time inputs providing a fallback widget if the respective
 * native input type is not supported.
 */
abstract class AbstractInput extends Composite implements DateTimeInput {

    private final HasValue<Date> delegate;

    /**
     * Creates a new {@link AbstractInput} instance passing method calls to the provided delegate.
     * 
     * @param delegate
     *            the native or fallback input for the respective input type
     */
    protected <T extends HasValue<Date> & IsWidget> AbstractInput(T delegate) {
        DateTimeInputResources.INSTANCE.css().ensureInjected();
        this.delegate = delegate;
        initWidget(delegate.asWidget());
    }

    @Override
    protected final void onEnsureDebugId(String baseId) {
        getWidget().ensureDebugId(baseId);
    }

    @Override
    public final Date getValue() {
        return delegate.getValue();
    }

    @Override
    public final void setValue(Date value) {
        this.delegate.setValue(value);
    }

    @Override
    public void setValue(Date value, boolean fireEvents) {
        this.delegate.setValue(value, fireEvents);
    }

    @Override
    public final HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
        return delegate.addValueChangeHandler(handler);
    }

}
