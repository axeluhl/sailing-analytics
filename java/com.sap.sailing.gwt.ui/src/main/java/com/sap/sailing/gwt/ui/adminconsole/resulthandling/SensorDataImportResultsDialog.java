package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class SensorDataImportResultsDialog extends AbstractDataImportResultDialog {

    public static void showResults(SensorDataImportResponse response) {
        if (response.hasErrors()) {
            final SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder.appendHtmlConstant(DIV_MARGIN);
            if (response.didSucceedImportingAnyFile()) {
                appendParagraph(builder, "Succesful uploads");
                builder.appendHtmlConstant(UL);
                response.getUploads().forEach(uuid -> appendLineItem(builder, uuid));
                builder.appendHtmlConstant(UL_END);
            } else {
                appendParagraph(builder, "No succesful upload!");
            }
            appendErrorMessagesIfPresent(builder, response);

            builder.appendHtmlConstant(DIV_END);
            show(builder);
        }
    }

}
