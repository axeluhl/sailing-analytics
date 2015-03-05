package com.sap.sse.gwt.theme.client.showcase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class Section extends Composite {
    private static ShowCaseSectionUiBinder uiBinder = GWT.create(ShowCaseSectionUiBinder.class);

    interface ShowCaseSectionUiBinder extends UiBinder<Widget, Section> {
    }

    private AcceptsOneWidget target;

    @UiField
    FlowPanel panelUi;

    public Section() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setTargetContentArea(AcceptsOneWidget target) {
        this.target = target;
    }

    @UiChild
    public void addItem(final Widget widget, String name) {
        SimplePanel p = new SimplePanel();
        Anchor showCaseItem = new Anchor(name);
        p.setWidget(showCaseItem);
        showCaseItem.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                target.setWidget(widget);
            }
        });

        panelUi.add(p);
    }
}
