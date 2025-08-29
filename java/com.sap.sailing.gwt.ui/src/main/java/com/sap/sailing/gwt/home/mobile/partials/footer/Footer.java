package com.sap.sailing.gwt.home.mobile.partials.footer;

import static com.google.gwt.dom.client.Style.Display.NONE;

import java.util.Optional;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.SwitchingEntryPoint;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.controls.languageselect.LanguageSelector;
import com.sap.sse.gwt.shared.ClientConfiguration;

/**
 * Mobile page footer with several links and the ability to switch the language.
 */
public class Footer extends Composite {
    private static FooterPanelUiBinder uiBinder = GWT.create(FooterPanelUiBinder.class);
    ClientConfiguration cfg = ClientConfiguration.getInstance();

    interface FooterPanelUiBinder extends UiBinder<Widget, Footer> {
    }
    
    @UiField Anchor whatsNewLinkUi;
    @UiField AnchorElement supportAnchor;
    @UiField LanguageSelector languageSelector;
    @UiField DivElement copyrightDiv;
    @UiField AnchorElement imprintAnchorLink;
    @UiField AnchorElement desktopUi;
    @UiField AnchorElement sapJobsAnchor;
    @UiField AnchorElement privacyAnchorLink;

    private final MobilePlacesNavigator placeNavigator;

    public Footer(MobilePlacesNavigator placeNavigator) {
        this.placeNavigator = placeNavigator;
        FooterResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
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
        if (!cfg.isBrandingActive()) {
            copyrightDiv.getStyle().setDisplay(NONE);
            languageSelector.setLabelText(StringMessages.INSTANCE.whitelabelFooterLanguage());
            supportAnchor.getStyle().setDisplay(Display.NONE);
            whatsNewLinkUi.getElement().getStyle().setDisplay(Display.NONE);
            imprintAnchorLink.getStyle().setDisplay(Display.NONE);
            sapJobsAnchor.getStyle().setDisplay(Display.NONE);
            privacyAnchorLink.getStyle().setDisplay(NONE);
        } else {
            if (!hideIfBlank(copyrightDiv, cfg.getFooterCopyright())) {
                copyrightDiv.setInnerText(cfg.getFooterCopyright());
            }
            languageSelector.setLabelText(cfg.getBrandTitle(Optional.empty()) + " " + StringMessages.INSTANCE.whitelabelFooterLanguage());
            setHrefOrHide(privacyAnchorLink, cfg.getFooterPrivacyLink());
            setHrefOrHide(sapJobsAnchor, cfg.getFooterJobsLink());
            setHrefOrHide(supportAnchor, cfg.getFooterSupportLink());
            setHrefOrHide(imprintAnchorLink, cfg.getFooterLegalLink());
            if (cfg.getFooterLegalLink().equals("nothing")) {
                placeNavigator.getImprintNavigation().configureAnchorElement(imprintAnchorLink);
            }
            if (isBlank(cfg.getFooterWhatsNewLink())) {
                whatsNewLinkUi.getElement().getStyle().setDisplay(Display.NONE);
            }
        }
    }

    @UiHandler("whatsNewLinkUi")
    void onWhatsNew(ClickEvent e) {
        if (!cfg.getFooterWhatsNewLink().equals("nothing")) {
            Window.Location.assign(cfg.getFooterWhatsNewLink());
        } else {
            placeNavigator.getWhatsNewNavigation(WhatsNewPlace.WhatsNewNavigationTabs.SailingAnalytics).goToPlace();
        }
    }
    private static boolean isBlank(String s) {
        return s == null || s.isEmpty();
    }
    private static boolean hideIfBlank(DivElement el, String text) {
        if (isBlank(text)) {
            el.getStyle().setDisplay(Display.NONE);
            return true;
        }
        return false;
    }
    private static void setHrefOrHide(AnchorElement el, String url) {
        if (isBlank(url)) {
          el.getStyle().setDisplay(Display.NONE);
        } else if (!url.equals("nothing")) {
          el.setHref(url);
        }
    }
}
