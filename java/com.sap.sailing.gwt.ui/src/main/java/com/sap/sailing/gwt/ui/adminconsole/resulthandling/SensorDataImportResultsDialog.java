package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SensorDataImportResultsDialog {
    public static void showResults(SensorDataImportResponse response) {
        if (!response.hasErrors()) {
            return;
        }
        List<ErrorMessage> errorMessages = response.getErrors();
        SafeHtmlBuilder shb = new SafeHtmlBuilder();
        shb.appendHtmlConstant("<div style='margin:40px;'>");
        if (response.didSucceedImportingAnyFile()) {
            shb.appendHtmlConstant("<p>Succesful uploads</p>");
            shb.appendHtmlConstant("<ul>");
            for (String uuid : response.getUploads()) {
                shb.appendHtmlConstant(" <li >").appendEscaped(uuid).appendHtmlConstant("</li>");
            }
            shb.appendHtmlConstant("</ul>");
        } else {
            shb.appendHtmlConstant("<p>No succesful upload</p>");
        }
        if (response.hasErrors()) {
            shb.appendHtmlConstant("<p>Error messages</p>");
            for (ErrorMessage errorMsg : errorMessages) {
                shb.appendHtmlConstant("<div style='font-weight:bold;'>").appendEscaped(errorMsg.getMessage())
                        .appendHtmlConstant("</div>");
                shb.appendHtmlConstant("<ul>");
                if (!isNullOrEmpty(errorMsg.getExUUID())) {
                    shb.appendHtmlConstant(" <li>ServerLog UUID: ").appendEscaped(errorMsg.getExUUID())
                            .appendHtmlConstant("</li>");
                }
                if (!isNullOrEmpty(errorMsg.getClassName())) {
                    shb.appendHtmlConstant(" <li>Exception Classname: ").appendEscaped(errorMsg.getClassName())
                            .appendHtmlConstant("</li>");
                }
                if (!isNullOrEmpty(errorMsg.getFilename())) {
                    shb.appendHtmlConstant(" <li>Filename: ").appendEscaped(errorMsg.getFilename())
                            .appendHtmlConstant("</li>");
                }
                if (!isNullOrEmpty(errorMsg.getRequestedImporter())) {
                    shb.appendHtmlConstant(" <li>Requested Importer: ").appendEscaped(errorMsg.getRequestedImporter())
                            .appendHtmlConstant("</li>");
                }
                shb.appendHtmlConstant("</ul>");
            }
        }
        shb.appendHtmlConstant("</div>");
        final DialogBox errorBox = new DialogBox(false, true);
        VerticalPanel vp = new VerticalPanel();
        vp.add(new HTML(shb.toSafeHtml()));
        Button close = new Button("Close");
        close.addClickHandler(e -> errorBox.hide());
        vp.add(close);
        vp.setSpacing(10);
        errorBox.setGlassEnabled(true);
        errorBox.setWidget(vp);
        errorBox.center();
    }

    private static boolean isNullOrEmpty(String text) {
        return text == null || text.isEmpty();
    }
}
