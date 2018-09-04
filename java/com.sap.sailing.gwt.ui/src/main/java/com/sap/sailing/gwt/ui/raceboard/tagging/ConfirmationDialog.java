package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.function.Consumer;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;

/**
 * Used to show generic confirmation dialog to ask user for explicit confirmation of an action. Can be used to make sure
 * the user wants to delete something.
 */
public class ConfirmationDialog extends DialogBox {

    private final TagPanelResources resources = TagPanelResources.INSTANCE;
    private final TagPanelStyle style = resources.style();

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
    public ConfirmationDialog(StringMessages stringMessages, String title, String text, Consumer<Boolean> consumer) {
        Panel mainPanel = new FlowPanel();
        mainPanel.setStyleName(style.confirmationDialogPanel());

        Label label = new Label(text);
        label.getElement().getStyle().setMarginBottom(10, Unit.PX);

        Panel buttonsPanel = new FlowPanel();
        buttonsPanel.setStyleName(style.buttonsPanel());

        Button confirm = new Button(stringMessages.confirm());
        confirm.setStyleName(style.tagDialogButton());
        confirm.addStyleName("gwt-Button");
        confirm.addClickHandler(event -> {
            consumer.accept(true);
            hide();
        });
        buttonsPanel.add(confirm);

        Button cancel = new Button(stringMessages.cancel());
        cancel.setStyleName(style.tagDialogButton());
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
