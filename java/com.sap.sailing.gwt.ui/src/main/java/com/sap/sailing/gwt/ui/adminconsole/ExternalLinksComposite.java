package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.GWTLocaleUtil;

public class ExternalLinksComposite extends Composite {

    private Map<String, TextBox> sailorsInfoWebsiteURLEntryFields = new HashMap<>();
    private TextBox officialWebsiteURLEntryField;

    public ExternalLinksComposite(StringMessages stringMessages) {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");

        officialWebsiteURLEntryField = new TextBox();
        officialWebsiteURLEntryField.setVisibleLength(50);
        sailorsInfoWebsiteURLEntryFields = createTextBoxesForLocalesAndDefault(Collections.<String, String> emptyMap());

        int rowIndex = 0;
        Grid formGrid = new Grid(GWTLocaleUtil.getLanguageCountWithDefault() + 3, 2);
        formGrid.setWidget(rowIndex, 0, new Label(stringMessages.eventOfficialWebsiteURL() + ":"));
        formGrid.setWidget(rowIndex++, 1, officialWebsiteURLEntryField);
        formGrid.setWidget(rowIndex++, 0, new Label(stringMessages.eventSailorsInfoWebsiteURL()));
        for (Map.Entry<String, TextBox> sailorsInfoWebsiteUrlEntry : sailorsInfoWebsiteURLEntryFields.entrySet()) {
            String locale = sailorsInfoWebsiteUrlEntry.getKey();
            String suffix = locale == null ? "*" : "[" + locale + "]";
            String text = locale == null ? stringMessages.urlForAllLanguages()
                    : stringMessages.urlForLanguage(LocaleInfo.getLocaleNativeDisplayName(locale));
            Label urlLabel = new Label(text + " " + suffix + ":");
            urlLabel.getElement().getStyle().setMarginLeft(10, Unit.PX);
            formGrid.setWidget(rowIndex, 0, urlLabel);
            formGrid.setWidget(rowIndex, 1, sailorsInfoWebsiteUrlEntry.getValue());
            rowIndex++;
        }

        panel.add(formGrid);
        panel.add(new Label("[*] " + stringMessages.defaultSailorsInfoLinkInfoText()));
        initWidget(panel);
    }

    public String getOfficialWebsiteURLValue() {
        String value = officialWebsiteURLEntryField.getText().trim();
        return value.isEmpty() ? null : value;
    }

    public Map<String, String> getSailorsInfoWebsiteURLs() {
        Map<String, String> sailorsInfoWebsiteURLsMap = new HashMap<>();
        for (Map.Entry<String, TextBox> sailorsInfoWebsiteUrlEntry : sailorsInfoWebsiteURLEntryFields.entrySet()) {
            TextBox sailorsInfoWebsiteURLEntryField = sailorsInfoWebsiteUrlEntry.getValue();
            String sailorsInfoWebsiteURL = sailorsInfoWebsiteURLEntryField.getText().trim();
            sailorsInfoWebsiteURLsMap.put(sailorsInfoWebsiteUrlEntry.getKey(),
                    sailorsInfoWebsiteURL.isEmpty() ? null : sailorsInfoWebsiteURL);
        }
        return sailorsInfoWebsiteURLsMap;
    }

    public void fillExternalLinks(EventDTO event) {
        Map<String, String> initialValues = event.getSailorsInfoWebsiteURLs();
        for (String localeName : GWTLocaleUtil.getAvailableLocalesAndDefault()) {
            sailorsInfoWebsiteURLEntryFields.get(localeName).setValue(initialValues.get(localeName));
        }
        officialWebsiteURLEntryField.setValue(event.getOfficialWebsiteURL());
    }

    private Map<String, TextBox> createTextBoxesForLocalesAndDefault(Map<String, String> initialValues) {
        Map<String, TextBox> result = new LinkedHashMap<>();
        for (String localeName : GWTLocaleUtil.getAvailableLocalesAndDefault()) {
            TextBox sailorsInfoWebsiteURLEntryField = new TextBox();
            sailorsInfoWebsiteURLEntryField.setValue(initialValues.get(localeName));
            sailorsInfoWebsiteURLEntryField.setVisibleLength(50);
            result.put(localeName, sailorsInfoWebsiteURLEntryField);
        }
        return result;
    }
}
