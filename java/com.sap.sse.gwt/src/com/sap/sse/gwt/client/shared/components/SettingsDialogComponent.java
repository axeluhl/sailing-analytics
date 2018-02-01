package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;

public interface SettingsDialogComponent<SettingsType extends Settings> {
    /**
     * Called by the framework to obtain the widget to show in the settings dialog. Called each time before the
     * settings dialog is shown, therefore also before {@link #getResult()} is called and before any method on
     * the validator returned by {@link #getValidator()} is called.
     * 
     * @param dialog
     *            can be used to {@link DataEntryDialog#createCheckbox(String) produce a checkbox}, a
     *            {@link DataEntryDialog#createTextBox(String)} or other data entry elements that participate in key
     *            handling for validation triggering and for canceling and confirming the dialog. The type argument
     *            is therefore not propagated. It's not needed to create a simple UI control.
     * @return <code>null</code> in case this component doesn't make any special contribution to the settings dialog, or
     *         a valid widget that will be displayed in the settings dialog
     */
    Widget getAdditionalWidget(DataEntryDialog<?> dialog);
    
    /**
     * Obtains the result from this settings dialog component; usually, the result object is contructed by reading data
     * from the elements of the widget constructed by {@link #getAdditionalWidget(DataEntryDialog)}.
     */
    SettingsType getResult();
    
    /**
     * Obtains an optional validator that is used to validate the {@link #getAdditionalWidget(DataEntryDialog)} contents upon
     * changes. If <code>null</code> is returned, no validation will be performed.
     */
    Validator<SettingsType> getValidator();
    
    /**
     * @return <code>null</code> if no initial focus on any widget is desired, or a widget from the
     *         {@link #getAdditionalWidget(DataEntryDialog)} result that will then receive the focus when the settings
     *         dialog is shown
     */
    FocusWidget getFocusWidget();
}
