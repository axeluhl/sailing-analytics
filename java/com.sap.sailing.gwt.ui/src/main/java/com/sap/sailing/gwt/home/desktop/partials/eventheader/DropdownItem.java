package com.sap.sailing.gwt.home.desktop.partials.eventheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class DropdownItem extends Widget {

    private static DropdownItemUiBinder uiBinder = GWT.create(DropdownItemUiBinder.class);

    interface DropdownItemUiBinder extends UiBinder<Element, DropdownItem> {
    }
    
    @UiField EventHeaderResources local_res;

    @UiField
    AnchorElement link;
    
    @UiField
    DivElement title;

    public DropdownItem(String text, SafeUri link, boolean active) {
        setElement(uiBinder.createAndBindUi(this));
        
        if(active) {
            getElement().addClassName(local_res.css().dropdown_content_linkactive());
        }
        
        this.link.setHref(link);
        this.title.setInnerText(text);
    }

}
