package com.sap.sse.gwt.client.dialog;

import java.util.function.Consumer;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sse.gwt.client.dialog.DialogResources.DialogStyle;

/**
 * Used to show generic confirmation dialog to ask user for explicit confirmation of an action. Can be used to make sure
 * the user wants to delete something.
 */
public class ConfirmationDialog extends DialogBox {

    private final DialogResources resources = DialogResources.INSTANCE;
    private final DialogStyle style = resources.style();

    /**
     * Shows a centered dialog popup wich asks the user if the given action (<code>text</code>) should be confirmed.
     * 
     * @param title
     *            title of dialog popup
     * @param text
     *            question shown to user which needs confirmation
     * @param consumer
     *            returns <code>true</code> if user clicks the confirm-button, otherwise <code>false</code>.
     */
    public ConfirmationDialog(String title, String text, String confirmButtonText, String cancelButtonText, Consumer<Boolean> consumer) {
        final Panel mainPanel = new FlowPanel();
        mainPanel.setStyleName(style.confirmationDialogPanel());
        final Label label = new Label(text);
        label.getElement().getStyle().setMarginBottom(10, Unit.PX);
        final Panel buttonsPanel = new FlowPanel();
        buttonsPanel.setStyleName(style.buttonsPanel());
        final Button confirm = new Button(confirmButtonText);
        confirm.setStyleName(style.dialogButton());
        confirm.addStyleName("gwt-Button");
        confirm.addClickHandler(event -> {
            consumer.accept(true);
            hide();
        });
        buttonsPanel.add(confirm);
        final Button cancel = new Button(cancelButtonText);
        cancel.setStyleName(style.dialogButton());
        cancel.addStyleName("gwt-Button");
        cancel.addClickHandler(event -> {
            consumer.accept(false);
            hide();
        });
        buttonsPanel.add(cancel);
        mainPanel.add(label);
        mainPanel.add(buttonsPanel);
        setWidget(mainPanel);
        addStyleName(style.confirmationDialog());
        setGlassEnabled(true);
        setText(title);
        center();
    }
}
