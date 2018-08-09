package com.sap.sailing.gwt.home.shared.partials.editable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class InlineEditLabel extends Composite implements HasText {

    private static InlineEditTextfieldUiBinder uiBinder = GWT.create(InlineEditTextfieldUiBinder.class);

    interface InlineEditTextfieldUiBinder extends UiBinder<Widget, InlineEditLabel> {
    }

    @UiField
    Button button;

    @UiField
    TextBox textBox;

    @UiField
    Label label;

    private boolean state = false;

    public InlineEditLabel() {
        initWidget(uiBinder.createAndBindUi(this));
        updateState(false);
    }

    @UiHandler("button")
    void onClick(ClickEvent e) {
        updateState(!state);
    }

    private void updateState(boolean newState) {
        if (newState) {
            textBox.setWidth((label.getOffsetWidth() + 7) + "px");
            label.setVisible(false);
            textBox.setVisible(true);
            textBox.setText(label.getText());
        } else {
            label.setVisible(true);
            textBox.setVisible(false);
            label.setText(textBox.getText());

        }
        this.state = newState;
    }

    @Override
    public void setText(String text) {
        label.setText(text);
    }

    @Override
    public String getText() {
        return label.getText();
    }
}
