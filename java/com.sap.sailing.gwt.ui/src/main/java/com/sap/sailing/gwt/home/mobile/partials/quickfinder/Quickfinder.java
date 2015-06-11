package com.sap.sailing.gwt.home.mobile.partials.quickfinder;

import java.util.LinkedHashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.OptGroupElement;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class Quickfinder extends Widget {
    private static QuickfinderUiBinder uiBinder = GWT.create(QuickfinderUiBinder.class);

    interface QuickfinderUiBinder extends UiBinder<Element, Quickfinder> {
    }

    private final LinkedHashMap<String, OptGroupElement> groups = new LinkedHashMap<String, OptGroupElement>();

    @UiField
    protected SelectElement selectUi;

    public Quickfinder() {
        setElement(uiBinder.createAndBindUi(this));
    }


    public void addItem(String label, String value) {
        OptionElement oe = Document.get().createOptionElement();
        oe.setLabel(label);
        oe.setValue(value);
        selectUi.appendChild(oe);
    }

    public void addItemToGroup(String groupLabel, String itemLabel, String value) {

        OptGroupElement oge = createorFindGroup(groupLabel);
        OptionElement oe = Document.get().createOptionElement();
        oe.setLabel(itemLabel);
        oe.setValue(value);
        oge.appendChild(oe);
    }

    public void addGroup(String label) {
        if (groups.containsKey(label)) {
            return;
        }
        createorFindGroup(label);
    }

    private OptGroupElement createorFindGroup(String label) {
        OptGroupElement oge = groups.get(label);
        if (oge == null) {
            oge = Document.get().createOptGroupElement();
            oge.setLabel(label);
            groups.put(label, oge);
            selectUi.appendChild(oge);
        }
        return oge;
    }

}
