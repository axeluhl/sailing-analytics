package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ExpeditionDataImportResultsDialog extends AbstractDataImportResultDialog {

    public static void showResults(ExpeditionDataImportResponse response) {
        if (response.hasErrors()) {
            final SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder.appendHtmlConstant(DIV_MARGIN);
            appendErrorMessagesIfPresent(builder, response);
            builder.appendHtmlConstant(DIV_END);
            show(builder);
        }
    }

}
