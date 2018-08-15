package com.sap.sailing.gwt.home.shared.partials.editable;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class InlineEditLabel extends Composite implements HasText {

    private static InlineEditTextfieldUiBinder uiBinder = GWT.create(InlineEditTextfieldUiBinder.class);

    interface InlineEditTextfieldUiBinder extends UiBinder<Widget, InlineEditLabel> {
    }

    public static interface TextChangeHandler {
        void onTextChanged(String text);
    }

    @UiField
    EditableResources res;

    @UiField
    TextBox textBoxUi;

    @UiField
    Label labelUi;

    @UiField
    InlineEditButton editButtonUi;

    private boolean state = false;

    private final List<TextChangeHandler> changeHandlers = new ArrayList<>();

    public InlineEditLabel() {
        EditableResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        editButtonUi.getElement().getStyle()
                .setBackgroundImage("url('" + res.editPencil().getSafeUri().asString() + "')");
        textBoxUi.addKeyUpHandler((event) -> {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                updateState(false);
                editButtonUi.updateState(false);
            }
        });
        labelUi.addClickHandler((event) -> {
            updateState(true);
            editButtonUi.updateState(true);
        });
        updateState(false, true);
    }

    @UiHandler("editButtonUi")
    void onClick(ClickEvent e) {
        updateState(!state);
    }

    private void updateState(boolean newState) {
        updateState(newState, false);
    }

    private void updateState(boolean newState, boolean suppressEvents) {
        if (newState) {
            textBoxUi.setWidth((labelUi.getOffsetWidth() + 7) + "px");
            labelUi.setVisible(false);
            textBoxUi.setVisible(true);
            textBoxUi.setText(labelUi.getText());
            textBoxUi.setFocus(true);
        } else {
            boolean unchanged = textBoxUi.getText().equals(labelUi.getText());

            labelUi.setVisible(true);
            textBoxUi.setVisible(false);
            labelUi.setText(textBoxUi.getText());

            if (!suppressEvents && !unchanged) {
                changeHandlers.forEach(c -> c.onTextChanged(labelUi.getText()));
            }
        }
        this.state = newState;
    }

    @Override
    public void setText(String text) {
        labelUi.setText(text);
    }

    @Override
    public String getText() {
        return labelUi.getText();
    }

    public void addTextChangeHandler(TextChangeHandler handler) {
        changeHandlers.add(handler);
    }
}
