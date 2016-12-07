package com.sap.sailing.dashboards.gwt.client.widgets.header;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class DashboardWidgetHeaderAndNoDataMessage extends Composite implements HasWidgets {

    private static DashboardWidgetHeaderAndNoDataMessageUiBinder uiBinder = GWT.create(DashboardWidgetHeaderAndNoDataMessageUiBinder.class);
    private static final int EXTRA_WIDTH_MESSAGE = 10;

    interface DashboardWidgetHeaderAndNoDataMessageUiBinder extends
            UiBinder<Widget, DashboardWidgetHeaderAndNoDataMessage> {
    }

    @UiField
    HTMLPanel header;

    @UiField
    DivElement noDataMessageHeader;

    @UiField
    DivElement noDataMessage;

    public DashboardWidgetHeaderAndNoDataMessage() {
        DashboardWidgetHeaderAndNoDataMessageResources.INSTANCE.gss().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setHeaderText(String headerText) {
        this.header.getElement().setInnerHTML(headerText);
    }

    public void showNoDataMessageWithHeaderAndMessage(final String header, final String message) {
        Scheduler.get().scheduleDeferred(new Command() {
            public void execute() {
                noDataMessageHeader.setInnerText(header);
                noDataMessage.setInnerText(message);
                noDataMessage.getStyle().setWidth(noDataMessageHeader.getClientWidth() + EXTRA_WIDTH_MESSAGE, Unit.PX);
            }
        });

    }

    public void hideNoDataMessage() {
        noDataMessageHeader.setInnerHTML("");
        noDataMessage.setInnerHTML("");
    }

    @Override
    public void add(Widget w) {
        throw new UnsupportedOperationException("The method add(Widget w) is not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("The method clear() is not supported.");
    }

    @Override
    public Iterator<Widget> iterator() {
        return null;
    }

    @Override
    public boolean remove(Widget w) {
        return false;
    }
}
