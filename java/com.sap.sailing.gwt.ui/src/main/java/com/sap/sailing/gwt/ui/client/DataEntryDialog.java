package com.sap.sailing.gwt.ui.client;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
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
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * An abstract data entry dialog class, capturing data of type <code>T</code>, with generic OK/Cancel buttons, title and
 * message. Subclasses may override the {@link #show()} method to set the focus on their favorable initial entry field.
 * Subclasses can specify a widget to show in the dialog to capture properties specific to the result type <code>T</code> by
 * overriding the {@link #getAdditionalWidget()} method.
 * 
 * @author Axel Uhl (d043530)
 */
public abstract class DataEntryDialog<T> {
    private final DialogBox dateEntryDialog;
    private final Validator<T> validator;
    private final Button okButton;
    private final Button cancelButton;
    private final Label statusLabel;
    private final FlowPanel panelForAdditionalWidget;

    public static interface Validator<T> {
        /**
         * @return <code>null</code> in case the <code>valueToValidate</code> is valid; a user-readable error message otherwise
         */
        String getErrorMessage(T valueToValidate);
    }
    
    /**
     * @param validator
     *            an optional validator; if <code>null</code>, no validation of data entered is performed; otherwise,
     *            data validation is triggered upon any noticeable change in any of the elements constructed by
     *            {@link #createCheckbox(String)}, {@link #createTextBox(String)}, etc.
     * @param callback
     *            will be called when the dialog if {@link AsyncCallback#onFailure(Throwable) cancelled} or
     *            {@link AsyncCallback#onSuccess(Object) confirmed}
     */
    public DataEntryDialog(String title, String message, String okButtonName, String cancelButtonName,
            Validator<T> validator, final AsyncCallback<T> callback) {
        dateEntryDialog = new DialogBox();
        dateEntryDialog.setText(title);
        dateEntryDialog.setAnimationEnabled(true);
        this.validator = validator;
        okButton = new Button(okButtonName);
        FlowPanel dialogFPanel = new FlowPanel();
        statusLabel = new Label();
        dialogFPanel.add(statusLabel);
        if (message != null) {
            Label messageLabel = new Label(message);
            messageLabel.addStyleName("dialogMessageLabel");
            dialogFPanel.add(messageLabel);
        }
        
        panelForAdditionalWidget = new FlowPanel();
        dialogFPanel.add(panelForAdditionalWidget);
        FlowPanel buttonPanel = new FlowPanel();
        dialogFPanel.add(buttonPanel);
        buttonPanel.setStyleName("additionalWidgets");
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
        dateEntryDialog.setWidget(dialogFPanel);
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
    public TextBox createTextBox(String initialValue) {
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
     * Creates a text area with a key-up listener attached which ensures the value is updated after each
     * key-up event and the entire dialog is {@link #validate() validated} in this case.
     * 
     * @param initialValue Initial value to show in text area; <code>null</code> is permissible
     */
    public TextArea createTextArea(String initialValue) {
        TextArea textArea = new TextArea();
        textArea.setText(initialValue == null ? "" : initialValue);
        AbstractEntryPoint.addFocusUponKeyUpToggler(textArea);
        textArea.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent arg0) {
                validate();
            }
        });
        AbstractEntryPoint.linkEscapeToButton(getCancelButton(), textArea);
        return textArea;
    }
    
    /**
     * Creates a box for a long value with a key-up listener attached which ensures the value is updated after each
     * key-up event and the entire dialog is {@link #validate() validated} in this case.
     * 
     * @param initialValue initial value to show in the long box; <code>null</code> is permissible
     */
    public LongBox createLongBox(long initialValue, int visibleLength) {
        LongBox longBox = new LongBox();
        longBox.setVisibleLength(visibleLength);
        longBox.setValue(initialValue);
        AbstractEntryPoint.addFocusUponKeyUpToggler(longBox);
        longBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        AbstractEntryPoint.linkEnterToButton(getOkButton(), longBox);
        AbstractEntryPoint.linkEscapeToButton(getCancelButton(), longBox);
        return longBox;
    }

    public DoubleBox createDoubleBox(double initialValue, int visibleLength) {
        DoubleBox doubleBox = new DoubleBox();
        doubleBox.setVisibleLength(visibleLength);
        doubleBox.setValue(initialValue);
        AbstractEntryPoint.addFocusUponKeyUpToggler(doubleBox);
        doubleBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        AbstractEntryPoint.linkEnterToButton(getOkButton(), doubleBox);
        AbstractEntryPoint.linkEscapeToButton(getCancelButton(), doubleBox);
        return doubleBox;
    }

    /**
     * Creates a box for a integer value with a key-up listener attached which ensures the value is updated after each
     * key-up event and the entire dialog is {@link #validate() validated} in this case.
     * 
     * @param initialValue initial value to show in the integer box; <code>null</code> is permissible
     */
    public IntegerBox createIntegerBox(int initialValue, int visibleLength) {
        IntegerBox intBox = new IntegerBox();
        intBox.setVisibleLength(visibleLength);
        intBox.setValue(initialValue);
        AbstractEntryPoint.addFocusUponKeyUpToggler(intBox);
        intBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        AbstractEntryPoint.linkEnterToButton(getOkButton(), intBox);
        AbstractEntryPoint.linkEscapeToButton(getCancelButton(), intBox);
        return intBox;
    }

    public LongBox createLongBoxWithOptionalValue(Long initialValue, int visibleLength) {
        LongBox longBox = new LongBox();
        longBox.setVisibleLength(visibleLength);
        longBox.setValue(initialValue);
        AbstractEntryPoint.addFocusUponKeyUpToggler(longBox);
        longBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        AbstractEntryPoint.linkEnterToButton(getOkButton(), longBox);
        AbstractEntryPoint.linkEscapeToButton(getCancelButton(), longBox);
        return longBox;
    }

    public Label createHeadlineLabel(String headlineText) {
        Label headlineLabel = new Label(headlineText);
        headlineLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        headlineLabel.getElement().getStyle().setPaddingTop(1, Unit.EM);
        return headlineLabel;
    }
    
    public FlowPanel createHeadline(String headlineText) {
    	FlowPanel headlinePanel = new FlowPanel();
    	
        Label headlineLabel = new Label(headlineText);
        
        headlinePanel.addStyleName("dialogHeadline");
        headlinePanel.add(headlineLabel);
        return headlinePanel;
    }

    public CheckBox createCheckbox(String checkboxLabel) {
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

    public ListBox createListBox(boolean isMultipleSelect) {
        ListBox result = new ListBox(isMultipleSelect);
        result.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
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
