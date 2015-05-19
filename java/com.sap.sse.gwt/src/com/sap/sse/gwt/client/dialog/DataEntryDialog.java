package com.sap.sse.gwt.client.dialog;

import java.util.Date;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
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
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.sap.sse.gwt.client.controls.GenericListBox;
import com.sap.sse.gwt.client.controls.GenericListBox.ValueBuilder;
import com.sap.sse.gwt.client.controls.IntegerBox;

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
    
    public static interface DialogCallback<T> {
        void ok(T editedObject);
        void cancel();
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
            Validator<T> validator, final DialogCallback<T> callback) {
        this(title, message, okButtonName, cancelButtonName, validator, /* animationEnabled */ true, callback);
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
            Validator<T> validator, boolean animationEnabled, final DialogCallback<T> callback) {
        dateEntryDialog = new DialogBox();

        dateEntryDialog.setText(title);
        // dateEntryDialog.setAnimationEnabled(animationEnabled);
        dateEntryDialog.setGlassEnabled(true);

        this.validator = validator;
        okButton = new Button(okButtonName);
        okButton.getElement().getStyle().setMargin(3, Unit.PX);
        okButton.ensureDebugId("OkButton");
        FlowPanel dialogFPanel = new FlowPanel();
        dialogFPanel.setWidth("100%");
        statusLabel = new Label();
        statusLabel.ensureDebugId("StatusLabel");
        dialogFPanel.add(statusLabel);
        if (message != null) {
            Label messageLabel = new Label(message);
            messageLabel.addStyleName("dialogMessageLabel");
            dialogFPanel.add(messageLabel);
        }
        
        panelForAdditionalWidget = new FlowPanel();
        panelForAdditionalWidget.setWidth("100%");
        dialogFPanel.add(panelForAdditionalWidget);
        FlowPanel buttonPanel = new FlowPanel();
        dialogFPanel.add(buttonPanel);
        buttonPanel.setStyleName("additionalWidgets");
        buttonPanel.add(okButton);
        cancelButton = new Button(cancelButtonName);
        cancelButton.getElement().getStyle().setMargin(3, Unit.PX);
        cancelButton.ensureDebugId("CancelButton");
        buttonPanel.add(cancelButton);
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dateEntryDialog.hide();
                callback.cancel();
            }
        });
        dateEntryDialog.setWidget(dialogFPanel);
        okButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dateEntryDialog.hide();
                callback.ok(getResult());
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
        return createTextBoxInternal(initialValue, 30);
    }

    /**
     * Creates a text box with a key-up listener attached which ensures the value is updated after each
     * key-up event and the entire dialog is {@link #validate() validated} in this case.
     * 
     * @param initialValue initial value to show in text box; <code>null</code> is permissible
     * @param visibleLength the visible length of the text box
     */
    public TextBox createTextBox(String initialValue, int visibleLength) {
        return createTextBoxInternal(initialValue, visibleLength);
    }
        
    private TextBox createTextBoxInternal(String initialValue, int visibleLength) {
        TextBox textBox = new TextBox();
        textBox.setVisibleLength(visibleLength);
        textBox.setText(initialValue == null ? "" : initialValue);
        DialogUtils.addFocusUponKeyUpToggler(textBox);
        textBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        DialogUtils.linkEnterToButton(getOkButton(), textBox);
        DialogUtils.linkEscapeToButton(getCancelButton(), textBox);
        return textBox;
    }
    
    /**
     * Creates a password text box with a key-up listener attached which ensures the value is updated after each
     * key-up event and the entire dialog is {@link #validate() validated} in this case.
     * 
     * @param initialValue initial value to show in text box; <code>null</code> is permissible
     */
    public PasswordTextBox createPasswordTextBox(String initialValue) {
        return createPasswordTextBoxInternal(initialValue, 30);
    }

    /**
     * Creates a password text box with a key-up listener attached which ensures the value is updated after each
     * key-up event and the entire dialog is {@link #validate() validated} in this case.
     * 
     * @param initialValue initial value to show in text box; <code>null</code> is permissible
     * @param visibleLength the visible length of the text box
     */
    public PasswordTextBox createPasswordTextBox(String initialValue, int visibleLength) {
        return createPasswordTextBoxInternal(initialValue, visibleLength);
    }
        
    private PasswordTextBox createPasswordTextBoxInternal(String initialValue, int visibleLength) {
        PasswordTextBox parrwordTextBox = new PasswordTextBox();
        parrwordTextBox.setVisibleLength(visibleLength);
        parrwordTextBox.setText(initialValue == null ? "" : initialValue);
        DialogUtils.addFocusUponKeyUpToggler(parrwordTextBox);
        parrwordTextBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        DialogUtils.linkEnterToButton(getOkButton(), parrwordTextBox);
        DialogUtils.linkEscapeToButton(getCancelButton(), parrwordTextBox);
        return parrwordTextBox;
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
        DialogUtils.addFocusUponKeyUpToggler(textArea);
        textArea.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent arg0) {
                validate();
            }
        });
        DialogUtils.linkEscapeToButton(getCancelButton(), textArea);
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
        DialogUtils.addFocusUponKeyUpToggler(longBox);
        longBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        DialogUtils.linkEnterToButton(getOkButton(), longBox);
        DialogUtils.linkEscapeToButton(getCancelButton(), longBox);
        return longBox;
    }

    public DoubleBox createDoubleBox(double initialValue, int visibleLength) {
        return createDoubleBoxInternal(initialValue, visibleLength);
    }

    public DoubleBox createDoubleBox(int visibleLength) {
        return createDoubleBoxInternal(null, visibleLength);
    }
    
    private DoubleBox createDoubleBoxInternal(Double initialValue, int visibleLength) {
        DoubleBox doubleBox = new DoubleBox();
        doubleBox.setVisibleLength(visibleLength);
        doubleBox.setValue(initialValue);
        DialogUtils.addFocusUponKeyUpToggler(doubleBox);
        doubleBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        DialogUtils.linkEnterToButton(getOkButton(), doubleBox);
        DialogUtils.linkEscapeToButton(getCancelButton(), doubleBox);
        return doubleBox;
    }

    public DateBox createDateBox(Date initialDate, int visibleLength) {
        return createDateBoxInternal(initialDate, visibleLength);
    }

    public DateBox createDateBox(long initialTimeInMs, int visibleLength) {
        return createDateBoxInternal(new Date(initialTimeInMs), visibleLength);
    }

    public DateBox createDateBox(int visibleLength) {
        return createDateBoxInternal(null, visibleLength);
    }
    
    private DateBox createDateBoxInternal(Date initialDate, int visibleLength) {
        DateBox dateBox = new DateBox();
        dateBox.getTextBox().setVisibleLength(visibleLength);
        dateBox.setFireNullValues(true);
        dateBox.setValue(initialDate);
        DialogUtils.addFocusUponKeyUpToggler(dateBox.getTextBox());
        dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                validate();
            }
        });
        DialogUtils.linkEnterToButton(getOkButton(), dateBox.getTextBox());
        DialogUtils.linkEscapeToButton(getCancelButton(), dateBox.getTextBox());
        return dateBox;
    }

    /**
     * Creates a box for a integer value with a key-up listener attached which ensures the value is updated after each
     * key-up event and the entire dialog is {@link #validate() validated} in this case.
     * 
     * @param initialValue initial value to show in the integer box; <code>null</code> is permissible
     */
    public IntegerBox createIntegerBox(Integer initialValue, int visibleLength) {
        IntegerBox intBox = new IntegerBox();
        intBox.setVisibleLength(visibleLength);
        intBox.setValue(initialValue);
        DialogUtils.addFocusUponKeyUpToggler(intBox);
        intBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        DialogUtils.linkEnterToButton(getOkButton(), intBox);
        DialogUtils.linkEscapeToButton(getCancelButton(), intBox);
        return intBox;
    }

    public LongBox createLongBoxWithOptionalValue(Long initialValue, int visibleLength) {
        LongBox longBox = new LongBox();
        longBox.setVisibleLength(visibleLength);
        longBox.setValue(initialValue);
        DialogUtils.addFocusUponKeyUpToggler(longBox);
        longBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        DialogUtils.linkEnterToButton(getOkButton(), longBox);
        DialogUtils.linkEscapeToButton(getCancelButton(), longBox);
        return longBox;
    }

    public Label createHeadlineLabel(String headlineText) {
        Label headlineLabel = new Label(headlineText);
        headlineLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        headlineLabel.getElement().getStyle().setPaddingTop(1, Unit.EM);
        return headlineLabel;
    }
    
    public FlowPanel createHeadline(String headlineText, boolean regularHeadline) {
    	FlowPanel headlinePanel = new FlowPanel();
        Label headlineLabel = new Label(headlineText);
        if (regularHeadline) {
            headlinePanel.addStyleName("dialogInnerHeadline");
        } else {
            headlinePanel.addStyleName("dialogInnerHeadlineOther");
        }
        headlinePanel.add(headlineLabel);
        return headlinePanel;
    }

    public CheckBox createCheckbox(String checkboxLabel) {
        CheckBox result = new CheckBox(checkboxLabel);
        result.setWordWrap(false);
        result.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                validate();
            }
        });
        DialogUtils.linkEnterToButton(getOkButton(), result);
        DialogUtils.linkEscapeToButton(getCancelButton(), result);
        return result;
    }
    
    public void addTooltip(IsWidget widget, String tooltip) {
        widget.asWidget().setTitle(tooltip);
    }

    public RadioButton createRadioButton(String radioButtonGroupName, String radioButtonLabel) {
        RadioButton result = new RadioButton(radioButtonGroupName, radioButtonLabel);
        result.setWordWrap(false);
        result.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                validate();
            }
        });
        DialogUtils.linkEnterToButton(getOkButton(), result);
        DialogUtils.linkEscapeToButton(getCancelButton(), result);
        return result;
    }

    /**
     * Creates a standard label for input fields.
     * The label has some default formatting like "no wrap" and a colon right after the label text 
     * @param name
     * @return
     */
    public Label createLabel(String name) {
        Label result = new Label(name + ":");
        result.setWordWrap(false);
        return result;
    }

    public <ListItemT> GenericListBox<ListItemT> createGenericListBox(ValueBuilder<ListItemT> valueBuilder,
            boolean isMultipleSelect) {
        GenericListBox<ListItemT> result = new GenericListBox<>(valueBuilder);
        result.setMultipleSelect(isMultipleSelect);
        result.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        DialogUtils.linkEnterToButton(getOkButton(), result);
        DialogUtils.linkEscapeToButton(getCancelButton(), result);
        return result;
    }
    
    public ListBox createListBox(boolean isMultipleSelect) {
        ListBox result = new ListBox();
        result.setMultipleSelect(isMultipleSelect);
        result.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validate();
            }
        });
        DialogUtils.linkEnterToButton(getOkButton(), result);
        DialogUtils.linkEscapeToButton(getCancelButton(), result);
        return result;
    }

    public void alignAllPanelWidgetsVertically(HorizontalPanel panel, HasVerticalAlignment.VerticalAlignmentConstant alignment) {
        for (int i = 0; i < panel.getWidgetCount(); i++) {
            panel.setCellVerticalAlignment(panel.getWidget(i), alignment);
        }
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

    protected void setCursor(Style.Cursor cursor) {
        dateEntryDialog.getElement().getStyle().setCursor(cursor);
    }

    public void show() {
        Widget additionalWidget = getAdditionalWidget();
        if (additionalWidget != null) {
            panelForAdditionalWidget.add(additionalWidget);
        }
        validate();
        dateEntryDialog.center();
    }

    protected DialogBox getDialogBox() {
        return dateEntryDialog;
    }
    
    public void ensureDebugId(String debugId) {
        dateEntryDialog.ensureDebugId(debugId);
    }
    
    protected void addAutoHidePartner(Element element) {
        dateEntryDialog.addAutoHidePartner(element);
    }
}
