package com.sap.sailing.gwt.home.client.shared.footer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Footer extends Composite {
    private static FooterPanelUiBinder uiBinder = GWT.create(FooterPanelUiBinder.class);

    @UiField Anchor changeLanguageLink; 
    @UiField SpanElement currentLanguage;
    
    private LocaleInfo currentLocale;
    
    interface FooterPanelUiBinder extends UiBinder<Widget, Footer> {
    }

    public Footer() {
        FooterResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        currentLocale = LocaleInfo.getCurrentLocale();
        
        currentLanguage.setInnerHTML(currentLocale.getLocaleName());
    }
    
    @UiHandler("changeLanguageLink")
    public void changeLanguage(ClickEvent e) {
    }

}
