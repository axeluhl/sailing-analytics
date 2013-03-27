package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.UUID;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;

public class RegattaOverviewEntryPoint extends AbstractEntryPoint  {
    
    @Override
    public void doOnModuleLoad() {
        super.doOnModuleLoad();

        RootPanel rootPanel = RootPanel.get();
        boolean embedded = Window.Location.getParameter("embedded") != null
                && Window.Location.getParameter("embedded").equalsIgnoreCase("true");
        if (!embedded) {
            LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel("Regatta Overview", stringMessages, this);
            logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
            rootPanel.add(logoAndTitlePanel);
        } else {
            RootPanel.getBodyElement().getStyle().setPadding(0, Unit.PX);
            RootPanel.getBodyElement().getStyle().setPaddingTop(20, Unit.PX);
        }
        String eventIdAsString = Window.Location.getParameter("event");
        UUID eventId = convertIdentifierStringToUuid(eventIdAsString);
        if (eventId == null) {
            createErrorPage("This page requires a valid event id.");
            return;
        }
        RegattaOverviewTableComposite regattaOverviewPanel = new RegattaOverviewTableComposite(sailingService, this, stringMessages, eventIdAsString);
        rootPanel.add(regattaOverviewPanel);
    }
    
    private UUID convertIdentifierStringToUuid(String identifierToConvert) {
        UUID convertedUuid = null;
        if (identifierToConvert != null) {
            try {
                convertedUuid = UUID.fromString(identifierToConvert);
            } catch (IllegalArgumentException iae) {
            }
        }
        return convertedUuid;
    }
    
}
