package com.sap.sse.gwt.client.dialog;

import java.util.function.Consumer;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sse.gwt.client.dialog.DialogResources.DialogStyle;

/**
 * Used to show generic confirmation dialog to ask user for explicit confirmation of an action, e.g. can be used to make
 * sure the user really wants to delete something.
 */
public class ConfirmationDialog extends DialogBox {

    /**
     * Creates a centered dialog pop-up which asks the user if the given action (<code>text</code>) should be confirmed.
     *
     * @param title
     *            {@link String title} of dialog pop-up
     * @param text
     *            {@link String question} shown to user which needs confirmation
     * @param confirmButtonText
     *            {@link String text} for confirm option button
     * @param cancelButtonText
     *            {@link String text} for cancel option button
     * @param callback
     *            {@link Consumer callback} receiving <code>true</code> if the user confirms, <code>false</code>
     *            otherwise
     * @return the newly created {@link ConfirmationDialog} instance
     */
    public static ConfirmationDialog create(final String title, final String text, final String confirmButtonText,
            final String cancelButtonText, final Consumer<Boolean> callback) {
        return new ConfirmationDialog(title, text)
                .addButton(confirmButtonText, event -> callback.accept(true))
                .addButton(cancelButtonText, event -> callback.accept(false));
    }

    /**
     * Creates a centered dialog pop-up which asks the user if the given action (<code>text</code>) should be confirmed.
     *
     * @param title
     *            {@link String title} of dialog pop-up
     * @param text
     *            {@link String question} shown to user which needs confirmation
     * @param confirmButtonText
     *            {@link String text} for confirm option button
     * @param cancelButtonText
     *            {@link String text} for cancel option button
     * @param confirmCallback
     *            {@link Runnable callback} which is called if the user confirms only
     * @return the newly created {@link ConfirmationDialog} instance
     */
    public static ConfirmationDialog create(final String title, final String text, final String confirmButtonText,
            final String cancelButtonText, final Runnable confirmCallback) {
        return new ConfirmationDialog(title, text)
                .addButton(confirmButtonText, event -> confirmCallback.run())
                .addButton(cancelButtonText, event -> {});
    }

    private static DialogResources resources = DialogResources.INSTANCE;
    private static DialogStyle style = resources.style();

    private final Panel mainPanel;
    private final Panel buttonsPanel;

    private ConfirmationDialog(final String title, final String text) {
        style.ensureInjected();

        this.mainPanel = createPanel(style.confirmationDialog());
        this.buttonsPanel = createPanel(style.buttonsPanel());
        this.addMessageText(text);
        this.mainPanel.add(buttonsPanel);

        setWidget(mainPanel);
        setText(title);
        addStyleName(style.confirmationDialog());
        setGlassEnabled(true);
    }

    private Panel createPanel(final String styleName) {
        final Panel panel = new FlowPanel();
        panel.setStyleName(styleName);
        return panel;
    }

    private void addMessageText(final String text) {
        final Label label = new Label(text);
        label.addStyleName(style.infoText());
        this.mainPanel.add(label);
    }

    private ConfirmationDialog addButton(final String text, final ClickHandler clickHandler) {
        final Button button = new Button(text);
        button.addClickHandler(event -> {
            clickHandler.onClick(event);
            ConfirmationDialog.this.hide();
        });
        button.addStyleName(style.dialogButton());
        buttonsPanel.add(button);
        return this;
    }
}
