package com.sap.sailing.gwt.home.desktop.partials.footer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.home.shared.SwitchingEntryPoint;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sse.common.Util.Pair;

public class Footer extends Composite {
    private static FooterPanelUiBinder uiBinder = GWT.create(FooterPanelUiBinder.class);

    interface FooterPanelUiBinder extends UiBinder<Widget, Footer> {
    }

    @UiField Anchor changeLanguageLink;
    @UiField DivElement languageSelectionDiv;
    @UiField(provided = true) ValueListBox<Pair<String, String>> changeLanguageList = new ValueListBox<Pair<String,String>>(new Renderer<Pair<String, String>>() {
        @Override
        public String render(Pair<String, String> object) {
            if(object == null) {
                return "";
            }
            if(object.getB() != null) {
                return object.getB();
            }
            return object.getA();
        }

        @Override
        public void render(Pair<String, String> object, Appendable appendable) throws IOException {
            appendable.append(render(object));
        }
    });
    
    @UiField AnchorElement mobileUi;
    private String otherLanguage;
    private final LocaleInfo currentLocale = LocaleInfo.getCurrentLocale();


    @UiField(provided = true)
    final PlaceNavigation<WhatsNewPlace> releaseNotesNavigation;

    public Footer(final DesktopPlacesNavigator navigator, EventBus eventBus) {
        FooterResources.INSTANCE.css().ensureInjected();
        releaseNotesNavigation = navigator.getWhatsNewNavigation(WhatsNewNavigationTabs.SailingAnalytics);

        initWidget(uiBinder.createAndBindUi(this));
        
        updateUI();
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
    
    
    @UiHandler("changeLanguageLink")
    public void changeLanguage(ClickEvent e) {
        if(otherLanguage != null) {
          UrlBuilder builder = Location.createUrlBuilder().setParameter("locale", otherLanguage);
          Window.Location.replace(builder.buildString());
        }
    }
    
    @UiHandler("changeLanguageList")
    void onListSelection(ValueChangeEvent<Pair<String, String>> event) {
        String selectedLocaleName = event.getValue().getA();
        if(!currentLocale.getLocaleName().equals(selectedLocaleName)) {
            UrlBuilder builder = Location.createUrlBuilder().setParameter("locale", selectedLocaleName);
            Window.Location.replace(builder.buildString());
        }
    }
    
    private void updateUI() {
        Set<String> availableLocales = new HashSet<String>(Arrays.asList(LocaleInfo.getAvailableLocaleNames()));
        availableLocales.remove("default");
        
        if(availableLocales.size() <= 1) {
            // only current language (or removed default) available
            changeLanguageLink.removeFromParent();
            changeLanguageList.removeFromParent();
            languageSelectionDiv.removeFromParent();
            return;
        }
        if(availableLocales.size() == 2) {
            changeLanguageList.removeFromParent();
            // current language + one we can switch to
            for(String localeName : availableLocales) {
                if(!currentLocale.getLocaleName().equals(localeName)) {
                    otherLanguage = localeName;
                    changeLanguageLink.setText(LocaleInfo.getLocaleNativeDisplayName(otherLanguage));
                    return;
                }
            }
            return;
        }
        changeLanguageLink.removeFromParent();
        Set<Pair<String, String>> values = new HashSet<>();
        Pair<String, String> selectedValue = null;
        for(String localeName : availableLocales) {
            Pair<String, String> value = new Pair<String, String>(localeName, LocaleInfo.getLocaleNativeDisplayName(localeName));
            values.add(value);
            if(currentLocale.getLocaleName().equals(localeName)) {
                selectedValue = value;
            }
        }
        changeLanguageList.setValue(selectedValue);
        changeLanguageList.setAcceptableValues(values);
    }
}
