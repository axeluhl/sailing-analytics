package com.sap.sailing.gwt.home.desktop.places.user.confirmation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.mvp.ErrorView;

public class MessageViewImpl extends Composite implements ErrorView {
    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    interface WidgetUiBinder extends UiBinder<Widget, MessageViewImpl> {
    }

    @UiField
    protected DivElement messageTitleUi;
    @UiField
    protected DivElement messageUi;
    @UiField
    protected Anchor actionUi;
    
    public MessageViewImpl(String messageTitle, String message, String actionTitle, final Command actionCommand) {
        initWidget(uiBinder.createAndBindUi(this));
        messageTitleUi.setInnerText(messageTitle);
        messageUi.setInnerText(message);
        if (actionCommand == null) {
            actionUi.setVisible(false);
        } else {
            actionUi.setText(actionTitle);
            actionUi.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    actionCommand.execute();
                }
            });
        }
    }
}
