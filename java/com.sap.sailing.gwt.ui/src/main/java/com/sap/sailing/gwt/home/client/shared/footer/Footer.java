package com.sap.sailing.gwt.home.client.shared.footer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;

public class Footer extends Composite {
    private static FooterPanelUiBinder uiBinder = GWT.create(FooterPanelUiBinder.class);

    interface FooterPanelUiBinder extends UiBinder<Widget, Footer> {
    }

    @UiField Anchor changeLanguageLink;
    @UiField DivElement languageSelectionDiv;

    private final Map<String, String> localeAndLanguages; 

    @UiField(provided = true)
    final PlaceNavigation<WhatsNewPlace> releaseNotesNavigation;

    public Footer(final HomePlacesNavigator navigator) {
        localeAndLanguages = new HashMap<String, String>();
        localeAndLanguages.put("default", "English");
        localeAndLanguages.put("de", "Deutsch");
        
        FooterResources.INSTANCE.css().ensureInjected();
        releaseNotesNavigation = navigator.getWhatsNewNavigation(WhatsNewNavigationTabs.SailingAnalytics);

        initWidget(uiBinder.createAndBindUi(this));
        
        languageSelectionDiv.getStyle().setDisplay(Display.NONE);
        updateUI();
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
}
