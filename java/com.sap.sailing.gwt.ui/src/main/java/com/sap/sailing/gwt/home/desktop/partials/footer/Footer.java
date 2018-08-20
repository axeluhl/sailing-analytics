package com.sap.sailing.gwt.home.desktop.partials.footer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.home.shared.SwitchingEntryPoint;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;

public class Footer extends Composite {
    private static FooterPanelUiBinder uiBinder = GWT.create(FooterPanelUiBinder.class);

    interface FooterPanelUiBinder extends UiBinder<Widget, Footer> {
    }

    @UiField AnchorElement imprintAnchorLink;
    @UiField AnchorElement mobileUi;
    @UiField(provided = true)
    final PlaceNavigation<WhatsNewPlace> releaseNotesNavigation;

    public Footer(final DesktopPlacesNavigator navigator, EventBus eventBus) {
        FooterResources.INSTANCE.css().ensureInjected();
        releaseNotesNavigation = navigator.getWhatsNewNavigation(WhatsNewNavigationTabs.SailingAnalytics);

        initWidget(uiBinder.createAndBindUi(this));
        navigator.getImprintNavigation().configureAnchorElement(imprintAnchorLink);
        
        DOM.sinkEvents(mobileUi, Event.ONCLICK);
        DOM.setEventListener(mobileUi, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (event.getTypeInt() == Event.ONCLICK) {
                    event.preventDefault();
                    SwitchingEntryPoint.switchToMobile();
                }
            }
        });
    }
    
}
