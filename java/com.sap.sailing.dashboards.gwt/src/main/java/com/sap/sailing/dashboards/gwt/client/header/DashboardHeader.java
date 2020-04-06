package com.sap.sailing.dashboards.gwt.client.header;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.shared.Branding;

public class DashboardHeader extends Composite implements HasWidgets {

    private static DashboardHeaderUiBinder uiBinder = GWT.create(DashboardHeaderUiBinder.class);

    interface DashboardHeaderUiBinder extends UiBinder<Widget, DashboardHeader> {
    }

    interface SettingsButtonWithSelectionIndicationLabelStyle extends CssResource {
    }
    
    @UiField
    Image sapLogo;

    @UiField
    DivElement event;
    
    @UiField
    DivElement race;
    
    private DashboardHeaderResources dashboardHeaderResources = DashboardHeaderResources.INSTANCE;
    
    public DashboardHeader() {
        dashboardHeaderResources.gss().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        if (Branding.getInstance().isActive()) {
            sapLogo.setResource(DashboardHeaderResources.INSTANCE.sapLogo());
        }
        event.setInnerHTML(StringMessages.INSTANCE.dashboardHeader());
    }
    
    public void setEventName(String eventName) {
        event.setInnerHTML(eventName);
    }

    public void setRaceName(String raceName) {
        this.event.addClassName(dashboardHeaderResources.gss().event_shared_space());
        race.setInnerHTML(raceName);      
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
