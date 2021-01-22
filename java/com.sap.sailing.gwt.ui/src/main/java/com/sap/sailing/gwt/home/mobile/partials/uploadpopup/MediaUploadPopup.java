package com.sap.sailing.gwt.home.mobile.partials.uploadpopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.home.mobile.places.event.media.MediaViewResources;
import com.sap.sailing.gwt.ui.leaderboard.DialogBoxExt;

public class MediaUploadPopup extends DialogBoxExt {

    public MediaUploadPopup() {
        super(new FlowPanel(), "Manage Photos", true);
        MediaViewResources.INSTANCE.css().ensureInjected();
        addStyleName(MediaViewResources.INSTANCE.css().popup());

        final FormPanel form = new FormPanel();
        form.setAction("/myFormHandler");

        // Because we're going to add a FileUpload widget, we'll need to set the
        // form to use the POST method, and multipart MIME encoding.
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);

        // Create a panel to hold all of the form widgets.
        VerticalPanel panel = new VerticalPanel();
        form.setWidget(panel);

        // Create a TextBox, giving it a name so that it will be submitted.
        final TextBox tb = new TextBox();
        tb.setName("textBoxFormElement");
        panel.add(tb);

        // Create a ListBox, giving it a name and some values to be associated with
        // its options.
        ListBox lb = new ListBox();
        lb.setName("listBoxFormElement");
        lb.addItem("foo", "fooValue");
        lb.addItem("bar", "barValue");
        lb.addItem("baz", "bazValue");
        panel.add(lb);

        // Create a FileUpload widget.
        FileUpload upload = new FileUpload();
        upload.getElement().setAttribute("accept", "image/*;capture=camera");
        upload.setName("uploadFormElement");
        panel.add(upload);

        // Add a 'submit' button.
        panel.add(new Button("Submit", new ClickHandler() {
          public void onClick(ClickEvent event) {
            form.submit();
          }
        }));

        // Add an event handler to the form.
        form.addSubmitHandler(new FormPanel.SubmitHandler() {
          public void onSubmit(SubmitEvent event) {
            // This event is fired just before the form is submitted. We can take
            // this opportunity to perform validation.
            if (tb.getText().length() == 0) {
              Window.alert("The text box must not be empty");
              event.cancel();
            }
                if (!upload.getFilename().endsWith(".png") && !upload.getFilename().endsWith(".svg")
                        && !upload.getFilename().endsWith(".tiff") && !upload.getFilename().endsWith(".jpg")
                        && !upload.getFilename().endsWith(".jpeg")) {
                    Window.alert("File typ is not supported.");
                    event.cancel();
                }
          }
        });
        form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
          public void onSubmitComplete(SubmitCompleteEvent event) {
            // When the form submission is successfully completed, this event is
            // fired. Assuming the service returned a response of type text/html,
            // we can get the result text here (see the FormPanel documentation for
            // further explanation).
            Window.alert(event.getResults());
          }
        });
        add(form);
    }

}
