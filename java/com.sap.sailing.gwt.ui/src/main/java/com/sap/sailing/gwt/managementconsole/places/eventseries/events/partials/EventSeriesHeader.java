package com.sap.sailing.gwt.managementconsole.places.eventseries.events.partials;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.mvp.View;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewView;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewView.Presenter;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

/**
 * TODO: This is currently copied over from regatta representing an event. Need to be changed to series.
 */
public class EventSeriesHeader extends Composite implements View<RegattaOverviewView.Presenter> {

    interface EventHeaderUiBinder extends UiBinder<Widget, EventSeriesHeader> {
    }

    private static EventHeaderUiBinder uiBinder = GWT.create(EventHeaderUiBinder.class);

    @UiField
    ManagementConsoleResources app_res;

    @UiField
    Anchor back, settings;

    @UiField
    Element eventName;

    @UiField
    Button regattaSection, invitationSection, webPageSection;

    private Presenter presenter;

    public EventSeriesHeader() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    public void setEventName(final String name) {
        eventName.setInnerText(name);
    }

    public void setRegattaSectionSelected(final boolean selected) {
        regattaSection.setStyleName(app_res.style().menuSelected(), selected);
    }

    public void setInvitationSectionSelected(final boolean selected) {
        regattaSection.setStyleName(app_res.style().menuSelected(), selected);
    }

    public void setWebPageSectionSelected(final boolean selected) {
        regattaSection.setStyleName(app_res.style().menuSelected(), selected);
    }

    @UiHandler("back")
    void onBackNavigationClicked(final ClickEvent event) {
        presenter.navigateToEvents();
    }

}
