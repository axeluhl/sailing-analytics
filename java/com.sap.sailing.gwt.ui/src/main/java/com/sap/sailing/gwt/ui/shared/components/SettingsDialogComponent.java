package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;

public interface SettingsDialogComponent<SettingsType> {
    /**
     * @param dialog
     *            can be used to {@link DataEntryDialog#createCheckbox(String) produce a checkbox}, a
     *            {@link DataEntryDialog#createTextBox(String)} or other data entry elements that participate in key
     *            handling for validation triggering and for canceling and confirming the dialog
     * @return <code>null</code> in case this component doesn't make any special contribution to the settings dialog, or
     *         a valid widget that will be displayed in the settings dialog
     */
    Widget getAdditionalWidget(DataEntryDialog<SettingsType> dialog);
    
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
