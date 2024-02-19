package com.sap.sailing.gwt.managementconsole.places.eventseries.overview;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.communication.event.EventSeriesMetadataDTO;
import com.sap.sailing.gwt.managementconsole.partials.contextmenu.ContextMenu;
import com.sap.sailing.gwt.managementconsole.places.eventseries.overview.partials.EventSeriesCard;
import com.sap.sailing.gwt.managementconsole.places.eventseries.overview.partials.EventSeriesInfo;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventSeriesOverviewViewImpl extends Composite implements EventSeriesOverviewView {
    
    private Presenter presenter;

    interface EventSeriesOverviewViewUiBinder extends UiBinder<Widget, EventSeriesOverviewViewImpl> {
    }

    private static EventSeriesOverviewViewUiBinder uiBinder = GWT.create(EventSeriesOverviewViewUiBinder.class);

    @UiField
    EventSeriesOverviewResources local_res;

    @UiField
    ManagementConsoleResources app_res;

    @UiField
    StringMessages i18n;

    @UiField
    Element headerContainer;

    @UiField
    ScrollPanel scrollContainer;

    @UiField
    FlowPanel cards;

    @UiField
    Anchor addEventAnchor, filterEventAnchor, searchEventAnchor;

    public EventSeriesOverviewViewImpl() {
        GWT.log("#### init event series");
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
        app_res.icons().ensureInjected();
        addEventAnchor.addClickHandler(e -> presenter.navigateToCreateEventSeries());
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void renderEventSeries(final List<EventSeriesMetadataDTO> eventSeries) {
        cards.clear();
        eventSeries.stream().map(eventSerie -> new EventSeriesCard(eventSerie, presenter)).forEach(cards::add);
    }

    @Override
    public void showContextMenu(EventSeriesMetadataDTO eventSeries) {
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setHeaderWidget(new EventSeriesInfo(eventSeries));
        contextMenu.addItem(i18n.advanced(), app_res.icons().iconSettings(), e -> presenter.advancedSettings(app_res, eventSeries));
        contextMenu.addItem(i18n.delete(), app_res.icons().iconDelete(), e -> presenter.deleteEventSeries(eventSeries));
        contextMenu.show();
    }

    @Override
    public void onResize() {
        final int scrollContainerHeight = getElement().getOffsetHeight() - headerContainer.getOffsetHeight();
        scrollContainer.getElement().getStyle().setHeight(scrollContainerHeight, Unit.PX);
    }

}