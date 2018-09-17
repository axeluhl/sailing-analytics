package com.sap.sailing.gwt.home.shared.partials.editable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class InlineEditButton extends Composite implements HasClickHandlers {

    private static InlineEditTextfieldUiBinder uiBinder = GWT.create(InlineEditTextfieldUiBinder.class);

    interface InlineEditTextfieldUiBinder extends UiBinder<Widget, InlineEditButton> {
    }

    public static interface TextChangeHandler {
        void onTextChanged(String text);
    }

    @UiField
    EditableResources res;

    @UiField
    Button editButton;

    private boolean state = false;

    public InlineEditButton() {
        initWidget(uiBinder.createAndBindUi(this));
        res.css().ensureInjected();
        updateState(false);
    }

    @UiHandler("editButton")
    void onClick(ClickEvent e) {
        updateState(!state);
        e.getNativeEvent().stopPropagation();
        e.getNativeEvent().preventDefault();
    }

    public void updateState(boolean newState) {
        if (newState) {
            editButton.getElement().getStyle().setBackgroundImage("url('" + res.save().getSafeUri().asString() + "')");
        } else {
            editButton.getElement().getStyle()
                    .setBackgroundImage("url('" + res.editPencil().getSafeUri().asString() + "')");
        }
        this.state = newState;
        editButton.setFocus(false);
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return editButton.addClickHandler(handler);
    }
}
