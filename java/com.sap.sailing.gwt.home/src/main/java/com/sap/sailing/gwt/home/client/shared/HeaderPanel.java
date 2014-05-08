package com.sap.sailing.gwt.home.client.shared;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class HeaderPanel extends Composite {
//    @UiField Label titleLabel;
//    
    @UiField LocaleSelection localeSelection;

    @UiField(provided = true) MainMenu mainMenu;
    
//    private static HeaderPanelUiBinder uiBinder = GWT.create(HeaderPanelUiBinder.class);

    interface HeaderPanelUiBinder extends UiBinder<Widget, HeaderPanel> {
    }

    @Inject
    HeaderPanel(HeaderPanelUiBinder uiBinder, MainMenu mainMenu) {
    	this.mainMenu = mainMenu;
   
        initWidget(uiBinder.createAndBindUi(this));
        
//        titleLabel.setText("SAP Sailing Analytics");
    }

    public void setTitle(String title) {
//        titleLabel.setText(title);
    }
    
}
