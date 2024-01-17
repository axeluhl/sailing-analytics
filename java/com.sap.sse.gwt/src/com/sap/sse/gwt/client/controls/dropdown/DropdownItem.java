package com.sap.sse.gwt.client.controls.dropdown;

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
    
    @UiField DropdownResources local_res;

    @UiField
    AnchorElement link;
    
    @UiField
    DivElement title;
    
    private final String text;

    public DropdownItem(String text, SafeUri link, boolean active) {
        this.text = text;
        setElement(uiBinder.createAndBindUi(this));
        local_res.css().ensureInjected();
        if (active) {
            getElement().addClassName(local_res.css().dropdown_content_linkactive());
        }
        if (link != null) {
            this.link.setHref(link);
        }
        this.title.setInnerText(text);
    }
    
    /**
     * @return the text passed to the constructor of this item
     */
    public String getText() {
        return text;
    }
}
