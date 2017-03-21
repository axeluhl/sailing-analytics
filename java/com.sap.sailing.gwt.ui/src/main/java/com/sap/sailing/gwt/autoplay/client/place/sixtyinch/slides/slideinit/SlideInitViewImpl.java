package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;

public class SlideInitViewImpl extends ResizeComposite implements SlideInitView {
    private static SlideInitViewImplUiBinder uiBinder = GWT.create(SlideInitViewImplUiBinder.class);

    interface SlideInitViewImplUiBinder extends UiBinder<Widget, SlideInitViewImpl> {
    }

    @UiField
    SimplePanel content;

    @UiField
    DivElement errorBoxUi;
    @UiField
    DivElement errorMessageUi;
    @UiField
    Anchor errorContinueUi;
    @UiField
    Anchor errorResetUi;

    public SlideInitViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));

    }

    @Override
    public void startingWith(SlideInitPresenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);


    }

    @Override
    public void setImage(String string) {
        content.getElement().getStyle().setBackgroundImage(string);
    }

    @Override
    public void showFailure(FailureEvent failureEvent, final Command onContinue, final Command onReset) {
        errorBoxUi.getStyle().setDisplay(Display.BLOCK);
        errorMessageUi.setInnerText(failureEvent.getMessage());
        if (onContinue != null) {
            errorContinueUi.setVisible(true);
            errorContinueUi.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    errorContinueUi.setEnabled(false);
                    errorResetUi.setEnabled(false);
                    onContinue.execute();
                }
            });
        } else {
            errorContinueUi.setVisible(false);
        }
        if (errorResetUi != null) {
            errorResetUi.setVisible(true);
            errorResetUi.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    errorContinueUi.setEnabled(false);
                    errorResetUi.setEnabled(false);
                    onReset.execute();
                }
            });
        } else {
            errorResetUi.setVisible(false);
        }
    }
}
