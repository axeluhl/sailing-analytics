package com.sap.sse.gwt.client.controls.dropdown;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.LinkUtil;

public class Dropdown extends Composite {
    private static DropdownUiBinder uiBinder = GWT.create(DropdownUiBinder.class);

    interface DropdownUiBinder extends UiBinder<Widget, Dropdown> {
    }
    
    @UiField FlowPanel dropdownContent;
    @UiField AnchorElement dropdownTrigger;
    @UiField SpanElement dropdownDisplayedText;
    @UiField DivElement dropdownTitle;
    @UiField DivElement dropdownHeadTitleButton;
    @UiField SpanElement dropdownHeadTitle;
    private final DropdownResources local_res;
    final DropdownHandler dropdownHandler; // final to force initialization; default scope to avoid unused warning

    public Dropdown() {
        this(GWT.create(DropdownResources.class));
    }
    
    public Dropdown(DropdownResources resources) {
        initWidget(uiBinder.createAndBindUi(this));
        local_res = resources;
        resources.css().ensureInjected();
        dropdownHandler = createDropdownHandler();
        dropdownTitle.addClassName(local_res.css().dropdown());
        dropdownTitle.addClassName(local_res.css().jsdropdown());
        dropdownTrigger.addClassName(local_res.css().dropdown_head());
        dropdownTrigger.addClassName(local_res.css().jsdropdown_head());
        dropdownContent.addStyleName(local_res.css().dropdown_content());
        dropdownContent.addStyleName(local_res.css().jsdropdown_content());
        dropdownHeadTitle.addClassName(local_res.css().dropdown_head_title());
        dropdownHeadTitleButton.addClassName(local_res.css().dropdown_head_title_button());
    }

    /**
     * Must be invoked after <tt>uiBinder.createAndBindUi(this)</tt> has been called
     */
    private DropdownHandler createDropdownHandler() {
        return new DropdownHandler(dropdownTrigger, dropdownContent.getElement()) {
            @Override
            protected void dropdownStateChanged(boolean dropdownShown) {
                if (dropdownShown) {
                    dropdownTitle.addClassName(local_res.css().jsdropdownactive());
                } else {
                    dropdownTitle.removeClassName(local_res.css().jsdropdownactive());
                }
            }
        };
    }
    
    public void setDisplayedText(String displayedText) {
        dropdownDisplayedText.setInnerText(displayedText);
    }
    
    public void addItem(String itemText, SafeUri link, boolean selected, Runnable callback) {
        final DropdownItem dropdownItem = new DropdownItem(local_res, itemText, link, selected);
        dropdownItem.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (LinkUtil.handleLinkClick((Event) event.getNativeEvent())) {
                    event.preventDefault();
                    if (callback != null) {
                        callback.run();
                    }
                }
            }
        }, ClickEvent.getType());
        dropdownContent.add(dropdownItem);
        if (selected) {
            setDisplayedText(itemText);
        }
    }
}