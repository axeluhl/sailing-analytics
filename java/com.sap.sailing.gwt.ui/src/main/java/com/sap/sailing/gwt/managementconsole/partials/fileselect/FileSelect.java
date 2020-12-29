package com.sap.sailing.gwt.managementconsole.partials.fileselect;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * {@link Composite} wrapping a {@link FileUpload} mirrored by a styled input field and a styled button.
 */
public class FileSelect extends Composite implements HasChangeHandlers, HasName, HasEnabled {

    interface FileSelectUiBinder extends UiBinder<FlowPanel, FileSelect> {
    }

    private static FileSelectUiBinder uiBinder = GWT.create(FileSelectUiBinder.class);

    @UiField
    FileSelectResources local_res;
    @UiField
    StringMessages i18n;
    @UiField
    FileUpload fileUpload;
    @UiField
    TextBox selectedFilename;
    @UiField
    Button selectFileControl;

    public FileSelect() {
        initWidget(uiBinder.createAndBindUi(this));
        this.local_res.style().ensureInjected();
        this.updateFilenameDisplay();
    }

    @Override
    public final HandlerRegistration addChangeHandler(final ChangeHandler handler) {
        return this.fileUpload.addChangeHandler(handler);
    }

    @Override
    public final String getName() {
        return this.fileUpload.getName();
    }

    @Override
    public final void setName(final String name) {
        this.fileUpload.setName(name);
    }

    @Override
    public final boolean isEnabled() {
        return this.fileUpload.isEnabled();
    }

    @Override
    public final void setEnabled(final boolean enabled) {
        this.fileUpload.setEnabled(enabled);
    }

    @UiHandler("selectedFilename")
    void onSelectedFilenameFocus(final FocusEvent event) {
        this.selectedFilename.selectAll();
    }

    @UiHandler("selectFileControl")
    void onSelectFileControlClick(final ClickEvent event) {
        this.fileUpload.click();
    }

    @UiHandler("fileUpload")
    void onFileSelectionChange(final ChangeEvent event) {
        this.updateFilenameDisplay();
    }

    public final String getSelectedFilename() {
        return this.fileUpload.getFilename();
    }

    public final void setAccept(final String accept) {
        this.fileUpload.getElement().setAttribute("accept", accept);
    }

    private void updateFilenameDisplay() {
        final String filename = fileUpload.getFilename();
        this.selectedFilename.setText(filename == null || filename.isEmpty() ? "TODO: No file selected"
                : filename.substring(Math.min(filename.lastIndexOf("\\") + 1, filename.length() - 1)));
    }

}