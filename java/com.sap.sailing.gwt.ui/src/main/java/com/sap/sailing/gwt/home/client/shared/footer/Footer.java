package com.sap.sailing.gwt.home.client.shared.footer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;

public class Footer extends Composite {
    private static FooterPanelUiBinder uiBinder = GWT.create(FooterPanelUiBinder.class);

    interface FooterPanelUiBinder extends UiBinder<Widget, Footer> {
    }

    @UiField Anchor changeLanguageLink;
    @UiField Anchor releaseNotesLink;
    @UiField DivElement languageSelectionDiv;

    private final Map<String, String> localeAndLanguages; 

    private final PlaceNavigation<WhatsNewPlace> releaseNotesNavigation;
    private final HomePlacesNavigator navigator;

    /** set-configuration-property name='locale.searchorder' value='queryparam,cookie,useragent' /> */
    private static final HyperlinkImpl HYPERLINK_IMPL = GWT.create(HyperlinkImpl.class);

    public Footer(final HomePlacesNavigator navigator) {
        this.navigator = navigator;
        
        localeAndLanguages = new HashMap<String, String>();
        localeAndLanguages.put("default", "English");
        localeAndLanguages.put("de", "Deutsch");
        
        FooterResources.INSTANCE.css().ensureInjected();
        StyleInjector.injectAtEnd("@media (min-width: 25em) { "+FooterResources.INSTANCE.mediumCss().getText()+"}");

        initWidget(uiBinder.createAndBindUi(this));
        
        releaseNotesNavigation = navigator.getWhatsNewNavigation(WhatsNewNavigationTabs.SailingAnalytics);
        releaseNotesLink.setHref(releaseNotesNavigation.getTargetUrl());
        
        languageSelectionDiv.getStyle().setDisplay(Display.NONE);
        updateUI();
    }
    
    @UiHandler("releaseNotesLink")
    public void goToReleaseNotes(ClickEvent e) {
        handleClickEvent(e, releaseNotesNavigation);
    }
    
    @UiHandler("changeLanguageLink")
    public void changeLanguage(ClickEvent e) {
        String selectedLanguage = changeLanguageLink.getText();
        String selectedLocale = null;
        for (Entry<String, String> entry: localeAndLanguages.entrySet()) {
            if (entry.getValue().equals(selectedLanguage)) {
                selectedLocale = entry.getKey();
            }
        }
        if(selectedLocale != null) {
          UrlBuilder builder = Location.createUrlBuilder().setParameter("locale", selectedLocale);
          Window.Location.replace(builder.buildString());        
        }
    }
    
    private void updateUI() {
        LocaleInfo currentLocale = LocaleInfo.getCurrentLocale();
        HashMap<String, String> languagesToSelectFrom = new HashMap<String, String>(localeAndLanguages);
        languagesToSelectFrom.remove(currentLocale.getLocaleName());

        if(languagesToSelectFrom.size() == 1) {
            changeLanguageLink.setText(languagesToSelectFrom.values().iterator().next());
        }
    }
    
    private void handleClickEvent(ClickEvent e, PlaceNavigation<?> placeNavigation) {
        if (HYPERLINK_IMPL.handleAsClick((Event) e.getNativeEvent())) {
            navigator.goToPlace(placeNavigation);
            e.preventDefault();
         }
    }
}
