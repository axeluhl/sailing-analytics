package com.sap.sailing.gwt.home.mobile.partials.footer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.SwitchingEntryPoint;

/**
 * Mobile page footer with several links and the ability to switch the language.
 */
public class Footer extends Composite {
    private static FooterPanelUiBinder uiBinder = GWT.create(FooterPanelUiBinder.class);

    interface FooterPanelUiBinder extends UiBinder<Widget, Footer> {
    }

    @UiField AnchorElement imprintAnchorLink;
    @UiField AnchorElement desktopUi;

    private final MobilePlacesNavigator placeNavigator;

    public Footer(MobilePlacesNavigator placeNavigator) {
        this.placeNavigator = placeNavigator;
        FooterResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));
        placeNavigator.getImprintNavigation().configureAnchorElement(imprintAnchorLink);
        
        DOM.sinkEvents(desktopUi, Event.ONCLICK);
        DOM.setEventListener(desktopUi, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (event.getTypeInt() == Event.ONCLICK) {
                    event.preventDefault();
                    SwitchingEntryPoint.switchToDesktop();
                }
            }
        });
    }

    @UiHandler("whatsNewLinkUi")
    void onWhatsNew(ClickEvent e) {
        placeNavigator.getWhatsNewNavigation(WhatsNewPlace.WhatsNewNavigationTabs.SailingAnalytics).goToPlace();
    }

}
