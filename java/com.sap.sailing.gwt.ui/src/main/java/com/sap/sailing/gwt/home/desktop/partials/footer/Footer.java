package com.sap.sailing.gwt.home.desktop.partials.footer;

import static com.sap.sse.gwt.shared.DebugConstants.DEBUG_ID_ATTRIBUTE;

import java.util.Optional;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.shared.SwitchingEntryPoint;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.controls.languageselect.LanguageSelector;
import com.sap.sse.gwt.shared.ClientConfiguration;
import com.sap.sse.gwt.shared.DebugConstants;

public class Footer extends Composite {
    private static FooterPanelUiBinder uiBinder = GWT.create(FooterPanelUiBinder.class);

    interface FooterPanelUiBinder extends UiBinder<Widget, Footer> {
    }
    
    @UiField AnchorElement whatsNewAnchor;
    @UiField AnchorElement supportAnchor;
    @UiField LanguageSelector languageSelector;
    @UiField DivElement copyrightDiv;
    @UiField AnchorElement imprintAnchorLink;
    @UiField AnchorElement privacyAnchorLink;
    @UiField AnchorElement mobileUi;
    @UiField AnchorElement sapJobsAnchor;

    public Footer(EventBus eventBus) {
        FooterResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));
        
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
        ClientConfiguration cfg = ClientConfiguration.getInstance();
        if (!cfg.isBrandingActive()) {
            copyrightDiv.getStyle().setDisplay(Display.NONE);
            languageSelector.setLabelText(StringMessages.INSTANCE.whitelabelFooterLanguage());
            supportAnchor.getStyle().setDisplay(Display.NONE);
            whatsNewAnchor.getStyle().setDisplay(Display.NONE);
            imprintAnchorLink.getStyle().setDisplay(Display.NONE);
            privacyAnchorLink.getStyle().setDisplay(Display.NONE);
            sapJobsAnchor.getStyle().setDisplay(Display.NONE);
        } else {
            hideIfBlank(copyrightDiv, cfg.getFooterCopyright());
            setHrefOrHide(privacyAnchorLink, cfg.getFooterPrivacyLink());
            setHrefOrHide(sapJobsAnchor, cfg.getFooterJobsLink());
            setHrefOrHide(supportAnchor, cfg.getFooterSupportLink());
            setHrefOrHide(whatsNewAnchor, cfg.getFooterWhatsNewLink());
            setHrefOrHide(imprintAnchorLink, cfg.getFooterLegalLink());
            languageSelector.setLabelText(cfg.getBrandTitle(Optional.empty()) + " " + StringMessages.INSTANCE.whitelabelFooterLanguage());
            if (!hideIfBlank(copyrightDiv, cfg.getFooterCopyright())) {
                copyrightDiv.setInnerText(cfg.getFooterCopyright());
            }
        }
        copyrightDiv.setAttribute(DebugConstants.DEBUG_ID_ATTRIBUTE, "copyrightDiv");
        supportAnchor.setAttribute(DEBUG_ID_ATTRIBUTE, "supportAnchor");
        whatsNewAnchor.setAttribute(DEBUG_ID_ATTRIBUTE, "whatsNewAnchor");
        imprintAnchorLink.setAttribute(DEBUG_ID_ATTRIBUTE, "imprintAnchorLink");
        privacyAnchorLink.setAttribute(DEBUG_ID_ATTRIBUTE, "privacyAnchorLink");
        languageSelector.getElement().setAttribute(DEBUG_ID_ATTRIBUTE, "languageSelector");
        sapJobsAnchor.setAttribute(DEBUG_ID_ATTRIBUTE, "sapJobsAnchor");
    }
    private static boolean hideIfBlank(DivElement el, String text) {
        if (!Util.hasLength(text)) {
            el.getStyle().setDisplay(Display.NONE);
            return true;
        }
        return false;
    }
    private static void setHrefOrHide(AnchorElement el, String url) {
        if (!Util.hasLength(url)) {
          el.getStyle().setDisplay(Display.NONE);
        } else {
          el.setHref(url);
        }
    }
}
