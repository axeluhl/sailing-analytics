package com.sap.sse.gwt.client.controls.languageselect;

import java.util.List;
import java.util.Optional;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.GWTLocaleUtil;

/**
 * {@link Widget} class which can be used to change the language of the application, based on the languages configured
 * in the module's <code>*.gwt.xml</code> file. The widget behaves as follows:
 * <ul>
 * <li>{@link Widget#removeFromParent() Removing} itself, if there is only one language configured (maybe default)</li>
 * <li>If two languages are configured, it shows a {@link Anchor link} to switch to the not actually selected one</li>
 * <li>If multiple languages are configured, it shows a {@link ValueListBox selection} to change to the desired one</li>
 * </ul>
 */
public class LanguageSelector extends Composite {

    private static LanguageSelectorUiBinder uiBinder = GWT.create(LanguageSelectorUiBinder.class);

    interface LanguageSelectorUiBinder extends UiBinder<Widget, LanguageSelector> {
    }

    @UiField
    InlineLabel labelUi;
    @UiField
    Anchor languageSwitchLinkUi;
    @UiField(provided = true)
    ValueListBox<String> languageSelectionUi;

    /**
     * Creates a new {@link LanguageSelector} instance.
     */
    @UiConstructor
    public LanguageSelector() {
        this.languageSelectionUi = new ValueListBox<>(new LanguageRenderer());
        this.initWidget(uiBinder.createAndBindUi(this));
        this.initLanguages(LocaleInfo.getCurrentLocale());
        this.labelUi.getElement().getStyle().setDisplay(Display.NONE);
    }

    /**
     * Sets the label text to use for this {@link LanguageSelector} instance.
     * 
     * @param labelText
     *            {@link String text} to show in front of the link / selection
     */
    public void setLabelText(String labelText) {
        this.labelUi.getElement().getStyle().clearDisplay();
        this.labelUi.setText(labelText);
    }

    private void initLanguages(final LocaleInfo currentLocale) {
        final List<String> availableLocales = Util.asList(GWTLocaleUtil.getAvailableLocales());
        if (availableLocales.size() <= 1) { // no other language available => remove widget
            this.removeFromParent();
        } else if (availableLocales.size() == 2) { // only one other language available => show link
            this.initLanguageSwitchLink(availableLocales, currentLocale);
        } else { // multiple languages available => show selection
            this.initLanguageSelection(availableLocales, currentLocale);
        }
    }

    private void initLanguageSwitchLink(final List<String> available, final LocaleInfo current) {
        this.languageSelectionUi.removeFromParent();
        final Optional<String> other = available.stream().filter(l -> !current.getLocaleName().equals(l)).findFirst();
        other.ifPresent(name -> {
            languageSwitchLinkUi.setText(LocaleInfo.getLocaleNativeDisplayName(name));
            languageSwitchLinkUi.addClickHandler(e -> switchLanguage(name));
        });
    }

    private void initLanguageSelection(final List<String> available, final LocaleInfo current) {
        this.languageSwitchLinkUi.removeFromParent();
        this.languageSelectionUi.setValue(current.getLocaleName());
        this.languageSelectionUi.setAcceptableValues(available);
        this.languageSelectionUi.addValueChangeHandler(event -> {
            if (!current.getLocaleName().equals(event.getValue())) {
                this.switchLanguage(event.getValue());
            }
        });
    }

    private void switchLanguage(String newLocaleName) {
        final UrlBuilder builder = Location.createUrlBuilder().setParameter("locale", newLocaleName);
        // Now work around the asymmetry (see also bug 4497) of the UrlBuilder's dealing with the hash/fragment:
        final String hash = Location.getHash();
        builder.setHash(null);
        final String newUrl;
        if (hash == null) {
            newUrl = builder.buildString();
        } else if (hash.startsWith("#")) {
            newUrl = builder.buildString()+hash;
        } else {
            newUrl = builder.buildString()+"#"+hash;
        }
        Window.Location.replace(newUrl);
    }

    private static class LanguageRenderer extends AbstractRenderer<String> {

        @Override
        public String render(String object) {
            final String displayName = LocaleInfo.getLocaleNativeDisplayName(object);
            return displayName == null ? "" : displayName;
        }

    }

}
