package com.sap.sailing.gwt.ui.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * An abstract data entry dialog class, capturing data of type <code>T</code>, with generic OK/Cancel buttons, title and
 * message. Subclasses may override the {@link #show()} method to set the focus on their favorable initial entry field.
 * 
 * @author Axel Uhl (d043530)
 */
public abstract class DataEntryDialog<T> {
    private final DialogBox dateEntryDialog;
    private final Validator<T> validator;
    private final Button okButton;
    private final Button cancelButton;
    private final Label statusLabel;
    private final HorizontalPanel panelForAdditionalWidget;

    public static interface Validator<T> {
        /**
         * @return <code>null</code> in case the <code>valueToValidate</code> is valid; a user-readable error message otherwise
         */
        String getErrorMessage(T valueToValidate);
    }
    
    public DataEntryDialog(String title, String message, String okButtonName, String cancelButtonName,
            Validator<T> validator, final AsyncCallback<T> callback) {
        dateEntryDialog = new DialogBox();
        dateEntryDialog.setText(title);
        dateEntryDialog.setAnimationEnabled(true);
        this.validator = validator;
        okButton = new Button(okButtonName);
        VerticalPanel dialogVPanel = new VerticalPanel();
        dialogVPanel.setSpacing(10);
        statusLabel = new Label();
        dialogVPanel.add(statusLabel);
        Label messageLabel = new Label(message);
        messageLabel.addStyleName("dialogMessageLabel");
        dialogVPanel.add(messageLabel);
        
        dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
        panelForAdditionalWidget = new HorizontalPanel();
        dialogVPanel.add(panelForAdditionalWidget);
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        dialogVPanel.add(buttonPanel);
        buttonPanel.add(okButton);
        cancelButton = new Button(cancelButtonName);
        buttonPanel.add(cancelButton);
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dateEntryDialog.hide();
                callback.onFailure(null);
            }
        });
        dateEntryDialog.setWidget(dialogVPanel);
        okButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dateEntryDialog.hide();
                callback.onSuccess(getResult());
            }
        });
    }
    
    protected boolean validate() {
        String errorMessage = null;
        if (validator != null) {
            errorMessage = validator.getErrorMessage(getResult());
        }
        if (errorMessage == null) {
            getStatusLabel().setText("");
            getOkButton().setEnabled(true);
        } else {
            getStatusLabel().setText(errorMessage);
            getStatusLabel().setStyleName("errorLabel");
            getOkButton().setEnabled(false);
        }
        return errorMessage == null;
    }
    
    protected abstract T getResult();
    
    /**
     * Creates a text box with a key-up listener attached which ensures the value is updated after each
     * key-up event and the entire dialog is {@link #validate() validated} in this case.
     * 
     * @param initialValue initial value to show in text box; <code>null</code> is permissible
     */
    protected TextBox createTextBox(String initialValue) {
        TextBox textBox = new TextBox();
        textBox.setText(initialValue == null ? "" : initialValue);
        AbstractEntryPoint.addFocusUponKeyUpToggler(textBox);
        textBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        AbstractEntryPoint.linkEnterToButton(getOkButton(), textBox);
        AbstractEntryPoint.linkEscapeToButton(getCancelButton(), textBox);
        return textBox;
    }
    
    /**
     * Creates a text box with a key-up listener attached which ensures the value is updated after each
     * key-up event and the entire dialog is {@link #validate() validated} in this case.
     * 
     * @param initialValue initial value to show in text box; <code>null</code> is permissible
     */
    protected IntegerBox createIntegerBox(int initialValue, int visibleLength) {
        IntegerBox integerBox = new IntegerBox();
        integerBox.setVisibleLength(visibleLength);
        integerBox.setValue(initialValue);
        AbstractEntryPoint.addFocusUponKeyUpToggler(integerBox);
        integerBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        AbstractEntryPoint.linkEnterToButton(getOkButton(), integerBox);
        AbstractEntryPoint.linkEscapeToButton(getCancelButton(), integerBox);
        return integerBox;
    }
    
    protected CheckBox createCheckbox(String checkboxLabel) {
        CheckBox result = new CheckBox(checkboxLabel);
        result.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                validate();
            }
        });
        AbstractEntryPoint.linkEnterToButton(getOkButton(), result);
        AbstractEntryPoint.linkEscapeToButton(getCancelButton(), result);
        return result;
    }

    /**
     * Can contribute an additional widget to be displayed underneath the text entry field. If <code>null</code> is
     * returned, no additional widget will be displayed. This is the default behavior of this default implementation.
     */
    protected Widget getAdditionalWidget() {
        return null;
    }
    
    protected Button getOkButton() {
        return okButton;
    }

    protected Button getCancelButton() {
        return cancelButton;
    }

    /**
     * status label can be used to display status information within the dialog to the user
     */
    protected Label getStatusLabel() {
        return statusLabel;
    }

    public void show() {
        Widget additionalWidget = getAdditionalWidget();
        if (additionalWidget != null) {
            panelForAdditionalWidget.add(additionalWidget);
        }
        validate();
        dateEntryDialog.center();
    }

}
