package com.sap.sailing.gwt.home.client.shared.sponsoring;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Sponsoring extends Composite {

    interface SolutionsUiBinder extends UiBinder<Widget, Sponsoring> {
    }
    
    private static SolutionsUiBinder uiBinder = GWT.create(SolutionsUiBinder.class);

    public Sponsoring() {
        GlobalResources.INSTANCE.globalCss().ensureInjected();
        SponsoringResources.INSTANCE.css().ensureInjected();
        
        StyleInjector.injectAtEnd("@media (min-width: 25em) { "+SponsoringResources.INSTANCE.mediumCss().getText()+"}");
        StyleInjector.injectAtEnd("@media (min-width: 50em) { "+SponsoringResources.INSTANCE.largeCss().getText()+"}");

        initWidget(uiBinder.createAndBindUi(this));
    }

}
