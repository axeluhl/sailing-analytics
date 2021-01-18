package com.sap.sailing.landscape.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.landscape.ui.shared.SSHKeyPairDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.fileupload.FileUploadWithLocalFileContent;

import elemental2.dom.File;

/**
 * Supports adding an SSH key, either by pasting the public and private key in Base64-encoded
 * form into text fields, or by providing the public and private key files.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class AddSshKeyDialog extends DataEntryDialog<SSHKeyPairDTO> {
    public AddSshKeyDialog(String title, String message, String okButtonName, String cancelButtonName,
            Validator<SSHKeyPairDTO> validator, DialogCallback<SSHKeyPairDTO> callback) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel result = new VerticalPanel();
        final FormPanel formPanel = new FormPanel();
        result.add(formPanel);
        final FileUpload fileUpload = new FileUpload();
        fileUpload.getElement().setPropertyString("mutliple", "true");
        formPanel.add(fileUpload);
        final FileUploadWithLocalFileContent fuwlfc = new FileUploadWithLocalFileContent(fileUpload);
        fileUpload.addChangeHandler(e->{
            GWT.log("Number of selected files: "+fuwlfc.getFileList().length);
            int i=0;
            for (final File file : fuwlfc.getFileList().asList()) {
                GWT.log("File: "+file.name);
                fuwlfc.getFileContents(i, contents->GWT.log("Contents of "+file.name+": "+contents));
            }
        });
        return result;
    }

    @Override
    protected SSHKeyPairDTO getResult() {
        // TODO Implement DataEntryDialog<SSHKeyPairDTO>.getResult(...)
        return null;
    }

}
