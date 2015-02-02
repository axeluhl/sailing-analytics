package com.sap.sailing.gwt.home.client.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class LocaleSelection extends Composite {
    private static LocaleSelectionCompositeUiBinder uiBinder = GWT.create(LocaleSelectionCompositeUiBinder.class);

    interface LocaleSelectionCompositeUiBinder extends UiBinder<Widget, LocaleSelection> {
    }

    @UiField(provided = true)
    ListBox localeSelection;

    public LocaleSelection() {
        localeSelection = new ListBox();
        localeSelection.setMultipleSelect(false);

        localeSelection.addItem("Deutsch");
        localeSelection.addItem("English");
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("localeSelection")
    void onLocaleSelectionChange(ChangeEvent event) {
        Window.alert(localeSelection.getItemText(localeSelection.getSelectedIndex()));
    }
}
