package com.sap.sailing.gwt.home.client.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class HeaderPanel extends Composite {
    @UiField
    LocaleSelection localeSelection;

    @UiField(provided=true)
    MainMenu mainMenu;

    interface HeaderPanelUiBinder extends UiBinder<Widget, HeaderPanel> {
    }
    
    private static HeaderPanelUiBinder uiBinder = GWT.create(HeaderPanelUiBinder.class);

    public HeaderPanel(MainMenuNavigator navigator) {
        HeaderPanelResources.INSTANCE.css().ensureInjected();
        mainMenu = new MainMenu(navigator);
        initWidget(uiBinder.createAndBindUi(this));
    }

}
